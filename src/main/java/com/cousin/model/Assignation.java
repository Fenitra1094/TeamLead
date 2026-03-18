package com.cousin.model;

import java.time.LocalDateTime;

public class Assignation {
    private int idAssignation;
    private int idReservation;
    private int idVehicule;
    private Integer idTrajet;
    private LocalDateTime dateHeureDepart;
    private LocalDateTime dateHeureRetour;
    private int quantitePassagersAssignes; // SPRINT 7: Supporte les assignations partielles

    // Objets liés pour l'affichage
    private Reservation reservation;
    private Vehicule vehicule;

    public Assignation() {
        this.quantitePassagersAssignes = 0;
    }

    public Assignation(int idAssignation, int idReservation, int idVehicule, Integer idTrajet,
                       LocalDateTime dateHeureDepart, LocalDateTime dateHeureRetour) {
        this.idAssignation = idAssignation;
        this.idReservation = idReservation;
        this.idVehicule = idVehicule;
        this.idTrajet = idTrajet;
        this.dateHeureDepart = dateHeureDepart;
        this.dateHeureRetour = dateHeureRetour;
        this.quantitePassagersAssignes = 0;
    }

    public Assignation(int idAssignation, int idReservation, int idVehicule, Integer idTrajet,
                       LocalDateTime dateHeureDepart, LocalDateTime dateHeureRetour, int quantitePassagersAssignes) {
        this.idAssignation = idAssignation;
        this.idReservation = idReservation;
        this.idVehicule = idVehicule;
        this.idTrajet = idTrajet;
        this.dateHeureDepart = dateHeureDepart;
        this.dateHeureRetour = dateHeureRetour;
        this.quantitePassagersAssignes = quantitePassagersAssignes;
    }

    public int getIdAssignation() {
        return idAssignation;
    }

    public void setIdAssignation(int idAssignation) {
        this.idAssignation = idAssignation;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public Integer getIdTrajet() {
        return idTrajet;
    }

    public void setIdTrajet(Integer idTrajet) {
        this.idTrajet = idTrajet;
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

    public int getQuantitePassagersAssignes() {
        return quantitePassagersAssignes;
    }

    public void setQuantitePassagersAssignes(int quantitePassagersAssignes) {
        this.quantitePassagersAssignes = quantitePassagersAssignes;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
}