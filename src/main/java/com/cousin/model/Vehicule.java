package com.cousin.model;

import java.time.LocalTime;

public class Vehicule {
    private int idVehicule;
    private String reference;
    private int nbPlace;
    private String typeVehicule;
    private LocalTime heureDisponibilite;
    private int trajetCount;  // Nombre de trajets du jour (données contextuelles)
    private java.time.LocalDateTime dernierRetour;  // Dernier retour du jour (données contextuelles)

    public Vehicule() {
    }

    public Vehicule(int idVehicule, String reference, int nbPlace, String typeVehicule) {
        this.idVehicule = idVehicule;
        this.reference = reference;
        this.nbPlace = nbPlace;
        this.typeVehicule = typeVehicule;
        this.heureDisponibilite = LocalTime.MIDNIGHT;
    }

    public Vehicule(int idVehicule, String reference, int nbPlace, String typeVehicule, LocalTime heureDisponibilite) {
        this.idVehicule = idVehicule;
        this.reference = reference;
        this.nbPlace = nbPlace;
        this.typeVehicule = typeVehicule;
        this.heureDisponibilite = heureDisponibilite;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getNbPlace() {
        return nbPlace;
    }

    public void setNbPlace(int nbPlace) {
        this.nbPlace = nbPlace;
    }

    public String getTypeVehicule() {
        return typeVehicule;
    }

    public void setTypeVehicule(String typeVehicule) {
        this.typeVehicule = typeVehicule;
    }

    public LocalTime getHeureDisponibilite() {
        return heureDisponibilite;
    }

    public void setHeureDisponibilite(LocalTime heureDisponibilite) {
        this.heureDisponibilite = heureDisponibilite;
    }

    public int getTrajetCount() {
        return trajetCount;
    }

    public void setTrajetCount(int trajetCount) {
        this.trajetCount = trajetCount;
    }

    public java.time.LocalDateTime getDernierRetour() {
        return dernierRetour;
    }

    public void setDernierRetour(java.time.LocalDateTime dernierRetour) {
        this.dernierRetour = dernierRetour;
    }
}
