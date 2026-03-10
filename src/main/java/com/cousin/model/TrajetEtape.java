package com.cousin.model;

public class TrajetEtape {
    private int idTrajetEtape;
    private int idTrajet;
    private int idHotel;
    private int ordre;
    private int distanceDepuisPrecedent;

    // pour affichage
    private Hotel hotel;

    public TrajetEtape() {
    }

    public TrajetEtape(int idTrajetEtape, int idTrajet, int idHotel, int ordre, int distanceDepuisPrecedent) {
        this.idTrajetEtape = idTrajetEtape;
        this.idTrajet = idTrajet;
        this.idHotel = idHotel;
        this.ordre = ordre;
        this.distanceDepuisPrecedent = distanceDepuisPrecedent;
    }

    public int getIdTrajetEtape() {
        return idTrajetEtape;
    }

    public void setIdTrajetEtape(int idTrajetEtape) {
        this.idTrajetEtape = idTrajetEtape;
    }

    public int getIdTrajet() {
        return idTrajet;
    }

    public void setIdTrajet(int idTrajet) {
        this.idTrajet = idTrajet;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public int getDistanceDepuisPrecedent() {
        return distanceDepuisPrecedent;
    }

    public void setDistanceDepuisPrecedent(int distanceDepuisPrecedent) {
        this.distanceDepuisPrecedent = distanceDepuisPrecedent;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}
