package com.cousin.service;

import com.cousin.model.Reservation;
import com.cousin.util.GroupeTemps;
import com.cousin.util.GroupeVol;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupingService {

    public List<GroupeTemps> grouperParTempsAttente(List<Reservation> reservations, int tempsAttenteMinutes) {
        List<GroupeTemps> groupes = new ArrayList<>();
        if (reservations == null || reservations.isEmpty()) {
            return groupes;
        }

        List<Reservation> triees = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation != null && reservation.getDateHeureArrive() != null) {
                triees.add(reservation);
            }
        }

        if (triees.isEmpty()) {
            return groupes;
        }

        triees.sort(Comparator.comparing(Reservation::getDateHeureArrive));

        Set<Integer> traites = new HashSet<>();
        for (int i = 0; i < triees.size(); i++) {
            Reservation pivot = triees.get(i);
            int idPivot = pivot.getIdReservation();
            if (traites.contains(idPivot)) {
                continue;
            }

            LocalDateTime datePivot = pivot.getDateHeureArrive();
            LocalDateTime fenetreFin = datePivot.plusMinutes(tempsAttenteMinutes);

            List<Reservation> reservationsGroupe = new ArrayList<>();
            reservationsGroupe.add(pivot);
            traites.add(idPivot);

            for (int j = i + 1; j < triees.size(); j++) {
                Reservation candidate = triees.get(j);
                int idCandidate = candidate.getIdReservation();
                if (traites.contains(idCandidate)) {
                    continue;
                }

                LocalDateTime heureCandidate = candidate.getDateHeureArrive();
                if (!heureCandidate.isAfter(fenetreFin)) {
                    reservationsGroupe.add(candidate);
                    traites.add(idCandidate);
                } else {
                    // Liste triee ASC, aucune reservation suivante ne peut entrer dans la fenetre.
                    break;
                }
            }

            reservationsGroupe.sort(Comparator.comparingInt(Reservation::getNbPassager).reversed());

            GroupeTemps groupe = new GroupeTemps();
            // La reference du groupe est l'heure minimale (premiere reservation de la fenetre).
            groupe.setHeureDepartGroupe(datePivot);
            groupe.setTempsAttenteMinutes(tempsAttenteMinutes);
            groupe.setReservations(reservationsGroupe);
            groupes.add(groupe);
        }

        return groupes;
    }

    public List<GroupeVol> grouperParVol(List<Reservation> reservations) {
        List<GroupeVol> groupes = new ArrayList<>();
        if (reservations == null || reservations.isEmpty()) {
            return groupes;
        }

        List<Reservation> triees = new ArrayList<>(reservations);
        triees.sort(Comparator.comparing(Reservation::getDateHeureArrive));

        Map<LocalDateTime, GroupeVol> mapGroupes = new LinkedHashMap<>();
        for (Reservation reservation : triees) {
            if (reservation.getDateHeureArrive() == null) {
                continue;
            }

            LocalDateTime cle = reservation.getDateHeureArrive().truncatedTo(ChronoUnit.MINUTES);
            GroupeVol groupe = mapGroupes.computeIfAbsent(cle, GroupeVol::new);
            groupe.getReservations().add(reservation);
        }

        groupes.addAll(mapGroupes.values());
        for (GroupeVol groupe : groupes) {
            groupe.getReservations().sort(Comparator.comparingInt(Reservation::getNbPassager).reversed());
        }

        return groupes;
    }
}
