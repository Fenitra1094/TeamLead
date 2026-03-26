package com.cousin.util;

import com.cousin.model.Reservation;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour fenêtre de retour véhicule (Sprint 8 DEV1 Branche 2).
 * Contient la fenêtre [début, fin] et les réservations qui y arrivent.
 */
public class FenetreRetourVehicule {
    private LocalDateTime debut;
    private LocalDateTime fin;
    private List<Reservation> reservationsDansFenetre;

    public FenetreRetourVehicule(LocalDateTime debut, LocalDateTime fin, List<Reservation> reservations) {
        this.debut = debut;
        this.fin = fin;
        this.reservationsDansFenetre = reservations;
    }

    public LocalDateTime getDebut() { return debut; }
    public void setDebut(LocalDateTime debut) { this.debut = debut; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }

    public List<Reservation> getReservationsDansFenetre() { return reservationsDansFenetre; }
    public void setReservationsDansFenetre(List<Reservation> reservations) { this.reservationsDansFenetre = reservations; }
}
