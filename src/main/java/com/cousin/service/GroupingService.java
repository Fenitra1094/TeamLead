package com.cousin.service;

import com.cousin.model.Reservation;
import com.cousin.util.GroupeVol;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupingService {

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
