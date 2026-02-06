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
            statement.setInt(4, reservation.getIdHotel());
            statement.executeUpdate();
        }
    }
}
