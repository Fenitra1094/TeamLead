package com.cousin.service;

import com.cousin.model.Hotel;
import com.cousin.model.Reservation;
import com.cousin.repository.HotelRepository;
import com.cousin.repository.ReservationRepository;
import java.sql.SQLException;
import java.util.List;

public class ReservationService {
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService() {
        this.hotelRepository = new HotelRepository();
        this.reservationRepository = new ReservationRepository();
    }

    public ReservationService(HotelRepository hotelRepository, ReservationRepository reservationRepository) {
        this.hotelRepository = hotelRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Hotel> listHotels() throws SQLException {
        return hotelRepository.findAll();
    }

    public void createReservation(Reservation reservation) throws SQLException {
        reservationRepository.insert(reservation);
    }
}
