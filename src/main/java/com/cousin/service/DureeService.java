package com.cousin.service;

import com.cousin.model.Hotel;
import com.cousin.repository.DistanceRepository;
import com.cousin.repository.HotelRepository;
import com.cousin.util.DureeResult;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DureeService {
    private final DistanceRepository distanceRepository;
    private final ParametreService parametreService;
    private final HotelRepository hotelRepository;

    public DureeService() {
        this.distanceRepository = new DistanceRepository();
        this.parametreService = new ParametreService();
        this.hotelRepository = new HotelRepository();
    }

    public DureeService(DistanceRepository distanceRepository, ParametreService parametreService, HotelRepository hotelRepository) {
        this.distanceRepository = distanceRepository;
        this.parametreService = parametreService;
        this.hotelRepository = hotelRepository;
    }

    public DureeResult calculerDuree(LocalDateTime dateArrivee, int idHotel) throws SQLException {
        if (dateArrivee == null) {
            throw new IllegalArgumentException("dateArrivee est obligatoire");
        }

        int idAeroport = getIdHotelAeroport();
        int distance = distanceRepository.getDistance(idAeroport, idHotel);
        int vm = parametreService.getVm();

        if (vm <= 0) {
            throw new IllegalStateException("Vm doit etre superieur a 0");
        }

        int dureeMinutes = (distance / vm) * 60 * 2;
        LocalDateTime dateDepart = dateArrivee;
        LocalDateTime dateRetour = dateDepart.plusMinutes(dureeMinutes);

        return new DureeResult(dateDepart, dateRetour, dureeMinutes);
    }

    private int getIdHotelAeroport() throws SQLException {
        List<Hotel> hotels = hotelRepository.findAll();

        for (Hotel hotel : hotels) {
            if (hotel.getNom() != null && hotel.getNom().equalsIgnoreCase("Aeroport")) {
                return hotel.getIdHotel();
            }
        }

        throw new SQLException("Hotel Aeroport introuvable");
    }
}
