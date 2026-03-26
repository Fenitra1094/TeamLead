package com.cousin.util;

import java.time.LocalDateTime;

/**
 * DTO contenant le résultat du traitement d'un véhicule à son retour.
 * Utilisé par AssignationService lors du traitement des fenêtres d'attente.
 * 
 * Attributs :
 * - modeDepart : "IMMEDIAT" ou "ATTENTE"
 * - dateDepartEffective : heure réelle de départ du véhicule
 * - fenetreDebut/fenetreFin : limites chronologiques de la fenêtre d'attente
 * - totalEmbarques* : passagers embarqués (précédents vs nouveaux)
 * - totalRestants* : passagers non assignables (précédents vs nouveaux)
 */
public class RetourVehiculeResult {
    private String modeDepart;
    private LocalDateTime dateDepartEffective;
    private LocalDateTime fenetreDebut;
    private LocalDateTime fenetreFin;
    private int totalEmbarquesPrecedents;
    private int totalEmbarquesNouveaux;
    private int totalRestantsPrecedents;
    private int totalRestantsNouveaux;

    public RetourVehiculeResult() {
    }

    public static RetourVehiculeResult empty() {
        return new RetourVehiculeResult();
    }

    // Getters & Setters (optimisés)
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
