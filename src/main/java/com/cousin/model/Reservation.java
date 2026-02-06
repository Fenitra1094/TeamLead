package com.cousin.model;

import java.time.LocalDateTime;

public class Reservation {
    private int idReservation;
    private LocalDateTime dateHeureArrive;
    private String idClient;
    private int nbPassager;
    private int idHotel;

    public Reservation() {
    }

    public Reservation(int idReservation, LocalDateTime dateHeureArrive, String idClient, int nbPassager, int idHotel) {
        this.idReservation = idReservation;
        this.dateHeureArrive = dateHeureArrive;
        this.idClient = idClient;
        this.nbPassager = nbPassager;
        this.idHotel = idHotel;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public LocalDateTime getDateHeureArrive() {
        return dateHeureArrive;
    }

    public void setDateHeureArrive(LocalDateTime dateHeureArrive) {
        this.dateHeureArrive = dateHeureArrive;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public int getNbPassager() {
        return nbPassager;
    }

    public void setNbPassager(int nbPassager) {
        this.nbPassager = nbPassager;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }
}
