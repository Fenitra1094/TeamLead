package com.cousin.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cousin.model.Reservation;
import com.cousin.util.GroupeTemps;
import com.cousin.util.GroupeVol;

public class GroupingService {

    public FenetreRetourVehicule construireFenetreRetourVehicule(
            LocalDateTime heureRetourVehicule,
            int tempsAttenteMinutes,
            List<Reservation> reservationsNouvelles) {

        LocalDateTime debut = heureRetourVehicule;
        LocalDateTime fin = heureRetourVehicule.plusMinutes(Math.max(0, tempsAttenteMinutes));

        List<Reservation> reservationsDansFenetre = new ArrayList<>();
        if (reservationsNouvelles != null) {
            for (Reservation reservation : reservationsNouvelles) {
                if (reservation == null || reservation.getDateHeureArrive() == null) {
                    continue;
                }

                LocalDateTime arrivee = reservation.getDateHeureArrive();
                if (!arrivee.isBefore(debut) && !arrivee.isAfter(fin)) {
                    reservationsDansFenetre.add(reservation);
                }
            }
        }

        reservationsDansFenetre.sort(Comparator.comparing(Reservation::getDateHeureArrive));
        return new FenetreRetourVehicule(debut, fin, reservationsDansFenetre);
    }

    public List<GroupeTemps> grouperParTempsAttente(List<Reservation> reservations, int tempsAttenteMinutes) {
        List<GroupeTemps> groupes = new ArrayList<>();
        if (reservations == null || reservations.isEmpty()) {
            return groupes;
        }

        List<Reservation> triees = reservations.stream()
                .filter(reservation -> reservation != null && reservation.getDateHeureArrive() != null)
                .collect(Collectors.toList());

        if (triees.isEmpty()) {
            return groupes;
        }

        triees.sort(Comparator.comparing(Reservation::getDateHeureArrive));

        Set<Integer> traites = new HashSet<>();
        for (int i = 0; i < triees.size(); i++) {
            Reservation pivot = triees.get(i);
            if (traites.contains(pivot.getIdReservation())) {
                continue;
            }

            LocalDateTime datePivot = pivot.getDateHeureArrive();
            LocalDateTime fenetreFin = datePivot.plusMinutes(tempsAttenteMinutes);
            LocalDateTime maxHeureGroupe = datePivot;

            List<Reservation> reservationsGroupe = new ArrayList<>();
            reservationsGroupe.add(pivot);
            traites.add(pivot.getIdReservation());

            for (int j = i + 1; j < triees.size(); j++) {
                Reservation candidate = triees.get(j);
                if (traites.contains(candidate.getIdReservation())) {
                    continue;
                }

                LocalDateTime heureCandidate = candidate.getDateHeureArrive();
                if (!heureCandidate.isAfter(fenetreFin)) {
                    reservationsGroupe.add(candidate);
                    traites.add(candidate.getIdReservation());
                    if (heureCandidate.isAfter(maxHeureGroupe)) {
                        maxHeureGroupe = heureCandidate;
                    }
                } else {
                    // Liste triee ASC, aucune reservation suivante ne peut entrer dans la fenetre.
                    break;
                }
            }

            reservationsGroupe.sort(Comparator.comparingInt(Reservation::getNbPassager).reversed());

            GroupeTemps groupe = new GroupeTemps();
            // Sprint 5/7/8 : départ groupe = max(DateHeureArrive) du groupe.
            groupe.setHeureDepartGroupe(maxHeureGroupe);
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
