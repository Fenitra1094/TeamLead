package com.cousin.model;

public class Distance {
    private int idDistance;
    private int fromHotel;
    private int toHotel;
    private int km;

    public Distance() {
    }

    public Distance(int idDistance, int fromHotel, int toHotel, int km) {
        this.idDistance = idDistance;
        this.fromHotel = fromHotel;
        this.toHotel = toHotel;
        this.km = km;
    }

    public int getIdDistance() {
        return idDistance;
    }

    public void setIdDistance(int idDistance) {
        this.idDistance = idDistance;
    }

    public int getFromHotel() {
        return fromHotel;
    }

    public void setFromHotel(int fromHotel) {
        this.fromHotel = fromHotel;
    }

    public int getToHotel() {
        return toHotel;
    }

    public void setToHotel(int toHotel) {
        this.toHotel = toHotel;
    }

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }
}
