package com.cousin.util;

import java.time.LocalDateTime;

public class DureeResult {
    private LocalDateTime dateDepart;
    private LocalDateTime dateRetour;
    private int dureeMinutes;

    public DureeResult() {
    }

    public DureeResult(LocalDateTime dateDepart, LocalDateTime dateRetour, int dureeMinutes) {
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.dureeMinutes = dureeMinutes;
    }

    public LocalDateTime getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDateTime dateDepart) {
        this.dateDepart = dateDepart;
    }

    public LocalDateTime getDateRetour() {
        return dateRetour;
    }

    public void setDateRetour(LocalDateTime dateRetour) {
        this.dateRetour = dateRetour;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }
}
