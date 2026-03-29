package com.cousin.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cousin.model.Reservation;

public class GroupeTemps {
    private LocalDateTime heureDepartGroupe;
    private int tempsAttenteMinutes;
    private List<Reservation> reservations;
    private boolean groupeCreeParRetourVehicule;
    private Integer idVehiculePivot;
    private String referenceVehiculePivot;

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

    public boolean isGroupeCreeParRetourVehicule() {
        return groupeCreeParRetourVehicule;
    }

    public void setGroupeCreeParRetourVehicule(boolean groupeCreeParRetourVehicule) {
        this.groupeCreeParRetourVehicule = groupeCreeParRetourVehicule;
    }

    public Integer getIdVehiculePivot() {
        return idVehiculePivot;
    }

    public void setIdVehiculePivot(Integer idVehiculePivot) {
        this.idVehiculePivot = idVehiculePivot;
    }

    public String getReferenceVehiculePivot() {
        return referenceVehiculePivot;
    }

    public void setReferenceVehiculePivot(String referenceVehiculePivot) {
        this.referenceVehiculePivot = referenceVehiculePivot;
    }
}