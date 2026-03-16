package com.cousin.repository;

import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DistanceRepository {
    public int getDistance(int idHotelA, int idHotelB) throws SQLException {
        String sql = "SELECT km FROM Distance WHERE (from_hotel = ? AND to_hotel = ?) OR (from_hotel = ? AND to_hotel = ?) LIMIT 1";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idHotelA);
            statement.setInt(2, idHotelB);
            statement.setInt(3, idHotelB);
            statement.setInt(4, idHotelA);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("km");
                }
            }
        }

        throw new SQLException("Distance introuvable entre les hotels " + idHotelA + " et " + idHotelB);
    }
}
