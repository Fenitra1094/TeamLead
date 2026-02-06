package com.cousin.repository;

import com.cousin.model.Hotel;
import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HotelRepository {
    public List<Hotel> findAll() throws SQLException {
        String sql = "SELECT Id_Hotel, nom FROM Hotel ORDER BY Id_Hotel";
        List<Hotel> hotels = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Hotel hotel = new Hotel();
                hotel.setIdHotel(resultSet.getInt("Id_Hotel"));
                hotel.setNom(resultSet.getString("nom"));
                hotels.add(hotel);
            }
        }

        return hotels;
    }
}
