package com.cousin.controller;

import com.cousin.model.Hotel;
import com.cousin.model.Reservation;
import com.cousin.service.ReservationService;
import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Json;
import com.framework.annotation.Param;
import com.framework.annotation.PostMapping;
import com.framework.model.ModelView;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReservationController {
    private final ReservationService reservationService = new ReservationService();

    @GetMapping("/reservation/form")
    public ModelView showForm() throws SQLException {
        List<Hotel> hotels = reservationService.listHotels();
        ModelView mv = new ModelView("/WEB-INF/views/reservation.jsp");
        mv.addAttribute("hotels", hotels);
        return mv;
    }

    @PostMapping("/reservation/create")
    public ModelView createReservation(
            @Param("dateHeureArrive") String dateHeureArrive,
            @Param("idClient") String idClient,
            @Param("nbPassager") int nbPassager,
            @Param("hotel.idHotel") int hotelId) throws SQLException {
        
        // Validation: idClient doit contenir exactement 4 chiffres
        if (idClient == null || !idClient.matches("\\d{4}")) {
            ModelView mv = new ModelView("/WEB-INF/views/reservation.jsp");
            mv.addAttribute("hotels", reservationService.listHotels());
            mv.addAttribute("error", "ID Client doit contenir exactement 4 chiffres");
            return mv;
        }
        
        // Validation: nbPassager doit être > 0
        if (nbPassager <= 0) {
            ModelView mv = new ModelView("/WEB-INF/views/reservation.jsp");
            mv.addAttribute("hotels", reservationService.listHotels());
            mv.addAttribute("error", "Le nombre de passagers doit être supérieur à 0");
            return mv;
        }
        
        // Validation: dateHeureArrive ne doit pas être vide
        if (dateHeureArrive == null || dateHeureArrive.isBlank()) {
            ModelView mv = new ModelView("/WEB-INF/views/reservation.jsp");
            mv.addAttribute("hotels", reservationService.listHotels());
            mv.addAttribute("error", "La date et l'heure d'arrivée sont requises");
            return mv;
        }
        
        Reservation reservation = new Reservation();
        reservation.setIdClient(idClient);
        reservation.setNbPassager(nbPassager);
        reservation.setDateHeureArrive(LocalDateTime.parse(dateHeureArrive));

        Hotel hotel = new Hotel();
        hotel.setIdHotel(hotelId);
        reservation.setHotel(hotel);

        reservationService.createReservation(reservation);

        ModelView mv = new ModelView("/WEB-INF/views/reservation.jsp");
        mv.addAttribute("hotels", reservationService.listHotels());
        mv.addAttribute("message", "Reservation enregistree avec succes!");
        return mv;
    }

    @GetMapping("/api/reservation/list")
    @Json
    public List<Reservation> listReservations() throws SQLException {
        return reservationService.listReservations();
    }
}
