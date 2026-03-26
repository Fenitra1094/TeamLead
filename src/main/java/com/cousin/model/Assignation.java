package com.cousin.model;

import java.time.LocalDateTime;

public class Assignation {
    private int idAssignation;
    private int idReservation;
    private int idVehicule;
    private Integer idTrajet;
    private LocalDateTime dateHeureDepart;
    private LocalDateTime dateHeureRetour;
    private Integer quantitePassagersAssignes;  // Sprint 7: support des assignations partielles
    private Boolean fromNonAssigneePrecedent;  // Sprint 8: origine des passagers embarques
    private LocalDateTime dateHeureDepartEffective; // Sprint 8: depart effectif retenu

    // Objets liés pour l'affichage
    private Reservation reservation;
    private Vehicule vehicule;

    public Assignation() {
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
        this.fromNonAssigneePrecedent = false;
        this.dateHeureDepartEffective = dateHeureDepart;
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

    public Integer getQuantitePassagersAssignes() {
        return quantitePassagersAssignes;
    }

    public void setQuantitePassagersAssignes(Integer quantitePassagersAssignes) {
        this.quantitePassagersAssignes = quantitePassagersAssignes;
    }

    public Boolean getFromNonAssigneePrecedent() {
        return fromNonAssigneePrecedent;
    }

    public void setFromNonAssigneePrecedent(Boolean fromNonAssigneePrecedent) {
        this.fromNonAssigneePrecedent = fromNonAssigneePrecedent;
    }

    public LocalDateTime getDateHeureDepartEffective() {
        return dateHeureDepartEffective;
    }

    public void setDateHeureDepartEffective(LocalDateTime dateHeureDepartEffective) {
        this.dateHeureDepartEffective = dateHeureDepartEffective;
    }
}