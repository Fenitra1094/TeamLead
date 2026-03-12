package com.cousin.service;

import com.cousin.model.TrajetEtape;
import com.cousin.model.Hotel;
import com.cousin.repository.DistanceRepository;
import com.cousin.repository.HotelRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteService {
    private final DistanceRepository distanceRepository;
    private final HotelRepository hotelRepository;

    public RouteService() {
        this.distanceRepository = new DistanceRepository();
        this.hotelRepository = new HotelRepository();
    }

    /**
     * Nearest-first with lexicographic tie-break on hotel name.
     * Returns an ordered list of TrajetEtape including a final step for the return to airport.
     */
    public List<TrajetEtape> calculerRoute(List<Integer> idHotels) throws SQLException {
        if (idHotels == null) return Collections.emptyList();

        List<TrajetEtape> etapes = new ArrayList<>();

        int idAeroport = hotelRepository.findIdByCode("AER");

        // Build hotel map for name lookup
        Map<Integer, Hotel> hotelMap = new HashMap<>();
        for (Hotel h : hotelRepository.findAll()) {
            hotelMap.put(h.getIdHotel(), h);
        }

        List<Integer> remaining = new ArrayList<>(idHotels);
        int pointCourant = idAeroport;
        int ordre = 1;

        while (!remaining.isEmpty()) {
            int bestId = -1;
            int bestDist = Integer.MAX_VALUE;
            String bestName = null;

            for (Integer candidate : new ArrayList<>(remaining)) {
                int dist = distanceRepository.getDistance(pointCourant, candidate);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestId = candidate;
                    Hotel h = hotelMap.get(candidate);
                    bestName = (h != null ? h.getNom() : null);
                } else if (dist == bestDist) {
                    Hotel h = hotelMap.get(candidate);
                    String name = (h != null ? h.getNom() : null);
                    if (bestName == null) {
                        bestId = candidate;
                        bestName = name;
                    } else if (name != null && name.compareTo(bestName) < 0) {
                        bestId = candidate;
                        bestName = name;
                    }
                }
            }

            if (bestId == -1) break;

            TrajetEtape e = new TrajetEtape();
            e.setIdHotel(bestId);
            e.setOrdre(ordre++);
            e.setDistanceDepuisPrecedent(bestDist);
            e.setHotel(hotelMap.get(bestId));
            etapes.add(e);

            remaining.remove(Integer.valueOf(bestId));
            pointCourant = bestId;
        }

        // add return to airport distance as final step
        if (!etapes.isEmpty()) {
            int lastHotel = etapes.get(etapes.size() - 1).getIdHotel();
            int distRetour = distanceRepository.getDistance(lastHotel, idAeroport);
            TrajetEtape retour = new TrajetEtape();
            retour.setIdHotel(idAeroport);
            retour.setOrdre(ordre);
            retour.setDistanceDepuisPrecedent(distRetour);
            retour.setHotel(hotelMap.get(idAeroport));
            etapes.add(retour);
        }

        return etapes;
    }

    public int calculerDistanceTotale(List<TrajetEtape> etapes) {
        if (etapes == null) return 0;
        int sum = 0;
        for (TrajetEtape e : etapes) {
            sum += e.getDistanceDepuisPrecedent();
        }
        return sum;
    }
}
