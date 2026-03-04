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

        /**
     * Retourne les véhicules disponibles pour un créneau donné.
     * Conditions : nbPlace >= nbPassager ET pas de chevauchement avec des assignations existantes.
     * Trié par : nbPlace ASC, TypeVehicule (D en premier), puis RANDOM.
     */
    public List<Vehicule> getVehiculesDisponible(int nbPassager, 
            java.time.LocalDateTime dateDepart, java.time.LocalDateTime dateRetour) throws SQLException {
        String sql = "SELECT v.Id_Vehicule, v.Reference, v.nbPlace, v.TypeVehicule " +
                     "FROM Vehicule v " +
                     "WHERE v.nbPlace >= ? " +
                     "AND NOT EXISTS (" +
                     "    SELECT 1 FROM Assignation a " +
                     "    WHERE a.Id_Vehicule = v.Id_Vehicule " +
                     "    AND NOT (a.date_heure_retour <= ? OR a.date_heure_depart >= ?)" +
                     ") " +
                     "ORDER BY v.nbPlace ASC, " +
                     "CASE WHEN v.TypeVehicule = 'D' THEN 0 ELSE 1 END, " +
                     "RANDOM()";

        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nbPassager);
            statement.setTimestamp(2, java.sql.Timestamp.valueOf(dateDepart));
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(dateRetour));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setIdVehicule(resultSet.getInt("Id_Vehicule"));
                    vehicule.setReference(resultSet.getString("Reference"));
                    vehicule.setNbPlace(resultSet.getInt("nbPlace"));
                    vehicule.setTypeVehicule(resultSet.getString("TypeVehicule"));
                    vehicules.add(vehicule);
                }
            }
        }
        return vehicules;
    }

    /**
     * Version avec connexion existante (pour la transaction).
     */
    public List<Vehicule> getVehiculesDisponible(int nbPassager, 
            java.time.LocalDateTime dateDepart, java.time.LocalDateTime dateRetour, 
            Connection connection) throws SQLException {
        String sql = "SELECT v.Id_Vehicule, v.Reference, v.nbPlace, v.TypeVehicule " +
                     "FROM Vehicule v " +
                     "WHERE v.nbPlace >= ? " +
                     "AND NOT EXISTS (" +
                     "    SELECT 1 FROM Assignation a " +
                     "    WHERE a.Id_Vehicule = v.Id_Vehicule " +
                     "    AND NOT (a.date_heure_retour <= ? OR a.date_heure_depart >= ?)" +
                     ") " +
                     "ORDER BY v.nbPlace ASC, " +
                     "CASE WHEN v.TypeVehicule = 'D' THEN 0 ELSE 1 END, " +
                     "RANDOM()";

        List<Vehicule> vehicules = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nbPassager);
            statement.setTimestamp(2, java.sql.Timestamp.valueOf(dateDepart));
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(dateRetour));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setIdVehicule(resultSet.getInt("Id_Vehicule"));
                    vehicule.setReference(resultSet.getString("Reference"));
                    vehicule.setNbPlace(resultSet.getInt("nbPlace"));
                    vehicule.setTypeVehicule(resultSet.getString("TypeVehicule"));
                    vehicules.add(vehicule);
                }
            }
        }
        return vehicules;
    }
}
