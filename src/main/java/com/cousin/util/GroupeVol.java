package com.cousin.util;

import com.cousin.model.Reservation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupeVol {
    private LocalDateTime heureArrivee;
    private List<Reservation> reservations;

    public GroupeVol() {
        this.reservations = new ArrayList<>();
    }

    public GroupeVol(LocalDateTime heureArrivee) {
        this.heureArrivee = heureArrivee;
        this.reservations = new ArrayList<>();
    }

    public GroupeVol(LocalDateTime heureArrivee, List<Reservation> reservations) {
        this.heureArrivee = heureArrivee;
        this.reservations = reservations;
    }

    public LocalDateTime getHeureArrivee() {
        return heureArrivee;
    }

    public void setHeureArrivee(LocalDateTime heureArrivee) {
        this.heureArrivee = heureArrivee;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
