package com.cousin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cousin.model.Reservation;

public class FenetreRetourVehicule {
    private LocalDateTime debut;
    private LocalDateTime fin;
    private List<Reservation> reservations;

    public FenetreRetourVehicule(LocalDateTime debut, LocalDateTime fin, List<Reservation> reservations) {
        this.debut = debut;
        this.fin = fin;
        this.reservations = reservations != null ? reservations : new ArrayList<>();
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public void setDebut(LocalDateTime debut) {
        this.debut = debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
