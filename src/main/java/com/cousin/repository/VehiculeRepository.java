package com.cousin.repository;

import com.cousin.model.Vehicule;
import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehiculeRepository {
    public void insert(Vehicule vehicule) throws SQLException {
        String sql = "INSERT INTO dev.Vehicule(Reference, nbPlace, TypeVehicule) VALUES (?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, vehicule.getReference());
            statement.setInt(2, vehicule.getNbPlace());
            statement.setString(3, vehicule.getTypeVehicule());
            statement.executeUpdate();
        }
    }

    public void update(Vehicule vehicule) throws SQLException {
        String sql = "UPDATE dev.Vehicule SET Reference = ?, nbPlace = ?, TypeVehicule = ? WHERE Id_Vehicule = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, vehicule.getReference());
            statement.setInt(2, vehicule.getNbPlace());
            statement.setString(3, vehicule.getTypeVehicule());
            statement.setInt(4, vehicule.getIdVehicule());
            statement.executeUpdate();
        }
    }

    public void deleteById(int idVehicule) throws SQLException {
        String sql = "DELETE FROM dev.Vehicule WHERE Id_Vehicule = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idVehicule);
            statement.executeUpdate();
        }
    }

    public Vehicule findById(int idVehicule) throws SQLException {
        String sql = "SELECT Id_Vehicule, Reference, nbPlace, TypeVehicule FROM dev.Vehicule WHERE Id_Vehicule = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idVehicule);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setIdVehicule(resultSet.getInt("Id_Vehicule"));
                    vehicule.setReference(resultSet.getString("Reference"));
                    vehicule.setNbPlace(resultSet.getInt("nbPlace"));
                    vehicule.setTypeVehicule(resultSet.getString("TypeVehicule"));
                    return vehicule;
                }
            }
        }

        return null;
    }

    public List<Vehicule> findAll() throws SQLException {
        String sql = "SELECT Id_Vehicule, Reference, nbPlace, TypeVehicule FROM dev.Vehicule ORDER BY Id_Vehicule";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setIdVehicule(resultSet.getInt("Id_Vehicule"));
                vehicule.setReference(resultSet.getString("Reference"));
                vehicule.setNbPlace(resultSet.getInt("nbPlace"));
                vehicule.setTypeVehicule(resultSet.getString("TypeVehicule"));
                vehicules.add(vehicule);
            }
        }

        return vehicules;
    }
}
