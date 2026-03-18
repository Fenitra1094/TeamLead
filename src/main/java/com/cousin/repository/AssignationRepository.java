package com.cousin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cousin.model.Assignation;

public class AssignationRepository {

    /**
     * Insère une assignation en utilisant une connexion existante (pour la transaction).
     * SPRINT 7: Inclut le champ quantitePassagersAssignes pour supporter les assignations partielles.
     */
    public void insertAssignation(Assignation assignation, Connection connection) throws SQLException {
        // Note: Le champ quantitePassagersAssignes doit exister dans la table Assignation
        // Migration SQL required: ALTER TABLE Assignation ADD COLUMN quantitePassagersAssignes INT DEFAULT 0;
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

            // SPRINT 7: Ajouter la quantité de passagers assignés
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
}