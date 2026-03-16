package com.cousin.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cousin.model.Reservation;

public class GroupeTemps {
    private LocalDateTime heureDepartGroupe;
    private int tempsAttenteMinutes;
    private List<Reservation> reservations;

    public GroupeTemps() {
        this.reservations = new ArrayList<>();
    }

    public GroupeTemps(LocalDateTime heureDepartGroupe, int tempsAttenteMinutes, List<Reservation> reservations) {
        this.heureDepartGroupe = heureDepartGroupe;
        this.tempsAttenteMinutes = tempsAttenteMinutes;
        this.reservations = reservations;
    }

    public LocalDateTime getHeureDepartGroupe() {
        return heureDepartGroupe;
    }

    public void setHeureDepartGroupe(LocalDateTime heureDepartGroupe) {
        this.heureDepartGroupe = heureDepartGroupe;
    }

    public int getTempsAttenteMinutes() {
        return tempsAttenteMinutes;
    }

    public void setTempsAttenteMinutes(int tempsAttenteMinutes) {
        this.tempsAttenteMinutes = tempsAttenteMinutes;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}