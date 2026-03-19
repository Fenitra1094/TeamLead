package com.cousin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cousin.model.Assignation;

public class AssignationRepository {

    /**
     * Insère une assignation (potentiellement partielle) en utilisant une connexion existante (pour la transaction).
     * Sprint 7 : Supporte quantitePassagersAssignes pour les assignations partielles.
     */
    public void insertAssignation(Assignation assignation, Connection connection) throws SQLException {
        String sql = "INSERT INTO Assignation(Id_Reservation, Id_Vehicule, date_heure_depart, date_heure_retour, Id_Trajet, quantitePassagersAssignes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, assignation.getIdReservation());
            statement.setInt(2, assignation.getIdVehicule());
            statement.setTimestamp(3, Timestamp.valueOf(assignation.getDateHeureDepart()));
            statement.setTimestamp(4, Timestamp.valueOf(assignation.getDateHeureRetour()));

            if (assignation.getIdTrajet() != null) {
                statement.setInt(5, assignation.getIdTrajet());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
           
            // Sprint 7 : Quantité de passagers assignés (support des splits)
            statement.setInt(6, assignation.getQuantitePassagersAssignes());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    assignation.setIdAssignation(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Supprime toutes les assignations existantes pour une date donnée (dans une transaction).
     */
    public void deleteAssignationsForDate(LocalDate date, Connection connection) throws SQLException {
        String sql = "DELETE FROM Assignation " +
                     "WHERE date_heure_depart >= ? AND date_heure_depart < ?";

        LocalDateTime debut = date.atStartOfDay();
        LocalDateTime fin = date.plusDays(1).atStartOfDay();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(debut));
            statement.setTimestamp(2, Timestamp.valueOf(fin));
            statement.executeUpdate();
        }
    }

    /**
     * Récupère une assignation par son ID.
     * Sprint 7 : Charge aussi quantitePassagersAssignes.
     */
    public Assignation findById(int idAssignation, Connection connection) throws SQLException {
        String sql = "SELECT Id_Assignation, Id_Reservation, Id_Vehicule, Id_Trajet, " +
                    "       date_heure_depart, date_heure_retour, " +
                    "       COALESCE(quantitePassagersAssignes, 0) AS quantitePassagersAssignes " +
                    "FROM Assignation " +
                    "WHERE Id_Assignation = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idAssignation);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToAssignation(resultSet);
                }
            }
        }
        return null;
    }

    /**
     * Récupère toutes les assignations pour une date donnée.
     * Sprint 7 : Charge aussi quantitePassagersAssignes.
     */
    public List<Assignation> findByDate(LocalDate date, Connection connection) throws SQLException {
        String sql = "SELECT Id_Assignation, Id_Reservation, Id_Vehicule, Id_Trajet, " +
                    "       date_heure_depart, date_heure_retour, " +
                    "       COALESCE(quantitePassagersAssignes, 0) AS quantitePassagersAssignes " +
                    "FROM Assignation " +
                    "WHERE DATE(date_heure_depart) = ? " +
                    "ORDER BY Id_Assignation";

        List<Assignation> assignations = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignations.add(mapResultSetToAssignation(resultSet));
                }
            }
        }

        return assignations;
    }

    /**
     * Utilitaire interne pour mapper un ResultSet vers un objet Assignation.
     * Sprint 7 : Inclut le mapping de quantitePassagersAssignes.
     */
    private Assignation mapResultSetToAssignation(ResultSet resultSet) throws SQLException {
        Assignation assignation = new Assignation();
        assignation.setIdAssignation(resultSet.getInt("Id_Assignation"));
        assignation.setIdReservation(resultSet.getInt("Id_Reservation"));
        assignation.setIdVehicule(resultSet.getInt("Id_Vehicule"));

        int idTrajet = resultSet.getInt("Id_Trajet");
        if (resultSet.wasNull()) {
            assignation.setIdTrajet(null);
        } else {
            assignation.setIdTrajet(idTrajet);
        }

        assignation.setDateHeureDepart(resultSet.getTimestamp("date_heure_depart").toLocalDateTime());
        assignation.setDateHeureRetour(resultSet.getTimestamp("date_heure_retour").toLocalDateTime());
        
        // Sprint 7 : Charger la quantité de passagers assignés
        assignation.setQuantitePassagersAssignes(resultSet.getInt("quantitePassagersAssignes"));

        return assignation;
    }

}