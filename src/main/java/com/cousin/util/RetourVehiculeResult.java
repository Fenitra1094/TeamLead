package com.cousin.util;

import java.time.LocalDateTime;

public class RetourVehiculeResult {
    private String modeDepart;
    private LocalDateTime dateDepartEffective;
    private LocalDateTime fenetreDebut;
    private LocalDateTime fenetreFin;
    private int totalEmbarquesPrecedents;
    private int totalEmbarquesNouveaux;
    private int totalRestantsPrecedents;
    private int totalRestantsNouveaux;

    public static RetourVehiculeResult empty() {
        return new RetourVehiculeResult();
    }

    public String getModeDepart() {
        return modeDepart;
    }

    public void setModeDepart(String modeDepart) {
        this.modeDepart = modeDepart;
    }

    public LocalDateTime getDateDepartEffective() {
        return dateDepartEffective;
    }

    public void setDateDepartEffective(LocalDateTime dateDepartEffective) {
        this.dateDepartEffective = dateDepartEffective;
    }

    public LocalDateTime getFenetreDebut() {
        return fenetreDebut;
    }

    public void setFenetreDebut(LocalDateTime fenetreDebut) {
        this.fenetreDebut = fenetreDebut;
    }

    public LocalDateTime getFenetreFin() {
        return fenetreFin;
    }

    public void setFenetreFin(LocalDateTime fenetreFin) {
        this.fenetreFin = fenetreFin;
    }

    public int getTotalEmbarquesPrecedents() {
        return totalEmbarquesPrecedents;
    }

    public void setTotalEmbarquesPrecedents(int totalEmbarquesPrecedents) {
        this.totalEmbarquesPrecedents = totalEmbarquesPrecedents;
    }

    public int getTotalEmbarquesNouveaux() {
        return totalEmbarquesNouveaux;
    }

    public void setTotalEmbarquesNouveaux(int totalEmbarquesNouveaux) {
        this.totalEmbarquesNouveaux = totalEmbarquesNouveaux;
    }

    public int getTotalRestantsPrecedents() {
        return totalRestantsPrecedents;
    }

    public void setTotalRestantsPrecedents(int totalRestantsPrecedents) {
        this.totalRestantsPrecedents = totalRestantsPrecedents;
    }

    public int getTotalRestantsNouveaux() {
        return totalRestantsNouveaux;
    }

    public void setTotalRestantsNouveaux(int totalRestantsNouveaux) {
        this.totalRestantsNouveaux = totalRestantsNouveaux;
    }
}
