package com.cousin.repository;

import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametreRepository {
    public String getValeurByCode(String code) throws SQLException {
        String sql = "SELECT valeur FROM Parametre WHERE code = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("valeur");
                }
            }
        }

        return null;
    }
}
