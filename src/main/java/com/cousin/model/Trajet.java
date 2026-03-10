package com.cousin.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Trajet {
    private int idTrajet;
    private int idVehicule;
    private LocalDateTime dateHeureDepart;
    private LocalDateTime dateHeureRetour;
    private LocalDate dateAssignation;

    // objets pour affichage
    private List<TrajetEtape> etapes;
    private Vehicule vehicule;

    public Trajet() {
    }

    public Trajet(int idTrajet, int idVehicule, LocalDateTime dateHeureDepart,
                  LocalDateTime dateHeureRetour, LocalDate dateAssignation) {
        this.idTrajet = idTrajet;
        this.idVehicule = idVehicule;
        this.dateHeureDepart = dateHeureDepart;
        this.dateHeureRetour = dateHeureRetour;
        this.dateAssignation = dateAssignation;
    }

    public int getIdTrajet() {
        return idTrajet;
    }

    public void setIdTrajet(int idTrajet) {
        this.idTrajet = idTrajet;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public LocalDateTime getDateHeureDepart() {
        return dateHeureDepart;
    }

    public void setDateHeureDepart(LocalDateTime dateHeureDepart) {
        this.dateHeureDepart = dateHeureDepart;
    }

    public LocalDateTime getDateHeureRetour() {
        return dateHeureRetour;
    }

    public void setDateHeureRetour(LocalDateTime dateHeureRetour) {
        this.dateHeureRetour = dateHeureRetour;
    }

    public LocalDate getDateAssignation() {
        return dateAssignation;
    }

    public void setDateAssignation(LocalDate dateAssignation) {
        this.dateAssignation = dateAssignation;
    }

    public List<TrajetEtape> getEtapes() {
        return etapes;
    }

    public void setEtapes(List<TrajetEtape> etapes) {
        this.etapes = etapes;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
}
