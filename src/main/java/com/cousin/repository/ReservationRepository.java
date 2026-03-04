package com.cousin.repository;

import com.cousin.model.Reservation;
import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ReservationRepository {
    public void insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation(DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES (?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (reservation.getDateHeureArrive() != null) {
                statement.setTimestamp(1, Timestamp.valueOf(reservation.getDateHeureArrive()));
            } else {
                statement.setTimestamp(1, null);
            }
            statement.setString(2, reservation.getIdClient());
            statement.setInt(3, reservation.getNbPassager());
            if (reservation.getHotel() == null) {
                throw new SQLException("Hotel is required for reservation");
            }
            statement.setInt(4, reservation.getHotel().getIdHotel());
            statement.executeUpdate();
        }
    }

    public java.util.List<Reservation> findAllWithHotel() throws SQLException {
        String sql = "SELECT r.Id_reservation, r.DateHeureArrive, r.idClient, r.nbPassager, " +
                     "h.Id_Hotel, h.nom " +
                     "FROM reservation r " +
                     "JOIN Hotel h ON r.Id_Hotel = h.Id_Hotel " +
                     "ORDER BY r.Id_reservation";
        java.util.List<Reservation> reservations = new java.util.ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             java.sql.ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                com.cousin.model.Hotel hotel = new com.cousin.model.Hotel();
                hotel.setIdHotel(resultSet.getInt("Id_Hotel"));
                hotel.setNom(resultSet.getString("nom"));

                Reservation reservation = new Reservation();
                reservation.setIdReservation(resultSet.getInt("Id_reservation"));
                java.sql.Timestamp ts = resultSet.getTimestamp("DateHeureArrive");
                if (ts != null) {
                    reservation.setDateHeureArrive(ts.toLocalDateTime());
                }
                reservation.setIdClient(resultSet.getString("idClient"));
                reservation.setNbPassager(resultSet.getInt("nbPassager"));
                reservation.setHotel(hotel);
                reservations.add(reservation);
            }
        }

        return reservations;
    }

        /**
     * Retourne les réservations pour une date donnée, ordonnées par heure d'arrivée.
     */
    public java.util.List<Reservation> getReservationsByDate(java.time.LocalDate date) throws SQLException {
        java.time.LocalDateTime debut = date.atStartOfDay();
        java.time.LocalDateTime fin = date.atTime(23, 59, 59);

        String sql = "SELECT r.Id_reservation, r.DateHeureArrive, r.idClient, r.nbPassager, " +
                     "h.Id_Hotel, h.nom " +
                     "FROM reservation r " +
                     "JOIN Hotel h ON r.Id_Hotel = h.Id_Hotel " +
                     "WHERE r.DateHeureArrive BETWEEN ? AND ? " +
                     "ORDER BY r.DateHeureArrive ASC";

        java.util.List<Reservation> reservations = new java.util.ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(debut));
            statement.setTimestamp(2, Timestamp.valueOf(fin));

            try (java.sql.ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    com.cousin.model.Hotel hotel = new com.cousin.model.Hotel();
                    hotel.setIdHotel(resultSet.getInt("Id_Hotel"));
                    hotel.setNom(resultSet.getString("nom"));

                    Reservation reservation = new Reservation();
                    reservation.setIdReservation(resultSet.getInt("Id_reservation"));
                    java.sql.Timestamp ts = resultSet.getTimestamp("DateHeureArrive");
                    if (ts != null) {
                        reservation.setDateHeureArrive(ts.toLocalDateTime());
                    }
                    reservation.setIdClient(resultSet.getString("idClient"));
                    reservation.setNbPassager(resultSet.getInt("nbPassager"));
                    reservation.setHotel(hotel);
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }

    /**
     * Version avec connexion existante (pour la transaction).
     */
    public java.util.List<Reservation> getReservationsByDate(java.time.LocalDate date, Connection connection) throws SQLException {
        java.time.LocalDateTime debut = date.atStartOfDay();
        java.time.LocalDateTime fin = date.atTime(23, 59, 59);

        String sql = "SELECT r.Id_reservation, r.DateHeureArrive, r.idClient, r.nbPassager, " +
                     "h.Id_Hotel, h.nom " +
                     "FROM reservation r " +
                     "JOIN Hotel h ON r.Id_Hotel = h.Id_Hotel " +
                     "WHERE r.DateHeureArrive BETWEEN ? AND ? " +
                     "ORDER BY r.DateHeureArrive ASC";

        java.util.List<Reservation> reservations = new java.util.ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(debut));
            statement.setTimestamp(2, Timestamp.valueOf(fin));

            try (java.sql.ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    com.cousin.model.Hotel hotel = new com.cousin.model.Hotel();
                    hotel.setIdHotel(resultSet.getInt("Id_Hotel"));
                    hotel.setNom(resultSet.getString("nom"));

                    Reservation reservation = new Reservation();
                    reservation.setIdReservation(resultSet.getInt("Id_reservation"));
                    java.sql.Timestamp ts = resultSet.getTimestamp("DateHeureArrive");
                    if (ts != null) {
                        reservation.setDateHeureArrive(ts.toLocalDateTime());
                    }
                    reservation.setIdClient(resultSet.getString("idClient"));
                    reservation.setNbPassager(resultSet.getInt("nbPassager"));
                    reservation.setHotel(hotel);
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }
}
