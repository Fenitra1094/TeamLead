package com.cousin.model;

public class Vehicule {
    private int idVehicule;
    private String reference;
    private int nbPlace;
    private String typeVehicule;
    private int trajetCount;  // Nombre de trajets du jour (données contextuelles)
    private java.time.LocalDateTime dernierRetour;  // Dernier retour du jour (données contextuelles)

    public Vehicule() {
    }

    public Vehicule(int idVehicule, String reference, int nbPlace, String typeVehicule) {
        this.idVehicule = idVehicule;
        this.reference = reference;
        this.nbPlace = nbPlace;
        this.typeVehicule = typeVehicule;
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
