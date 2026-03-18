package com.cousin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cousin.model.Vehicule;
import com.cousin.util.DbConnection;

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
     * Retourne les véhicules disponibles pour une date et un créneau de groupe.
     * Prend en compte :
     * 1) Le nombre de trajets déjà faits dans la journée
     * 2) Les véhicules qui reviennent pendant l'intervalle du groupe
     * 
     * @param nbPassagers Nombre de passagers à transporter
     * @param date Date d'assignation demandée
     * @param debutGroupe Heure actuelle de départ du groupe
     * @param finGroupe Heure de fin du groupe (première réservation + temps attente)
     * @return Liste des véhicules candidats avec trajet_count et dernierRetour, triés par trajet_count ASC
     */
    public List<Vehicule> getVehiculesDisponible(int nbPassagers, 
            java.time.LocalDate date, java.time.LocalDateTime debutGroupe, 
            java.time.LocalDateTime finGroupe) throws SQLException {
        try (Connection connection = DbConnection.getConnection()) {
            return getVehiculesDisponible(nbPassagers, date, debutGroupe, finGroupe, connection);
        }
    }

    /**
     * Version avec connexion existante (pour la transaction).
     */
    public List<Vehicule> getVehiculesDisponible(int nbPassagers, 
            java.time.LocalDate date, java.time.LocalDateTime debutGroupe, 
            java.time.LocalDateTime finGroupe, Connection connection) throws SQLException {
        
        // ============================================
        // ETAPE 1 : Sous-requête "charge du jour"
        // ============================================
        // SELECT t.Id_Vehicule,
        //        COUNT(t.Id_Trajet) AS trajet_count,
        //        MAX(t.date_heure_retour) AS dernier_retour
        // FROM dev.Trajet t
        // WHERE DATE(t.date_assignation) = ?
        // GROUP BY t.Id_Vehicule
        
        // ============================================
        // ETAPE 2 : Requête principale
        // ============================================
        // SELECT v.*,
        //        COALESCE(tc.trajet_count, 0) AS trajet_count,
        //        COALESCE(tc.dernier_retour, ?) AS dernier_retour
        // FROM dev.Vehicule v
        // LEFT JOIN (Etape 1) tc ON tc.Id_Vehicule = v.Id_Vehicule
        // WHERE v.nbPlace >= ?
        //   AND (
        //     Cas A : véhicule déjà libre au début du groupe
        //     COALESCE(tc.dernier_retour, ?) <= ?
        //     OR
        //     Cas B : véhicule qui revient pendant le groupe
        //     (COALESCE(tc.dernier_retour, ?) > ? AND COALESCE(tc.dernier_retour, ?) <= ?)
        //   )
        // ORDER BY trajet_count ASC,
        //          CASE WHEN v.TypeVehicule = 'D' THEN 0 ELSE 1 END ASC,
        //          RANDOM()
        
        String sql = "SELECT v.Id_Vehicule, v.Reference, v.nbPlace, v.TypeVehicule, " +
                "COALESCE(tc.trajet_count, 0) AS trajet_count, " +
                "COALESCE(tc.dernier_retour, ?) AS dernier_retour " +
                "FROM dev.Vehicule v " +
                "LEFT JOIN (" +
                "    SELECT t.Id_Vehicule, " +
                "           COUNT(t.Id_Trajet) AS trajet_count, " +
                "           MAX(t.date_heure_retour) AS dernier_retour " +
                "    FROM dev.Trajet t " +
                "    WHERE DATE(t.date_assignation) = ? " +
                "    GROUP BY t.Id_Vehicule" +
                ") tc ON tc.Id_Vehicule = v.Id_Vehicule " +
                "WHERE v.nbPlace >= ? " +
                "AND (" +
                "    COALESCE(tc.dernier_retour, ?) <= ? " +
                "    OR " +
                "    (COALESCE(tc.dernier_retour, ?) > ? AND COALESCE(tc.dernier_retour, ?) <= ?)" +
                ") " +
                "ORDER BY trajet_count ASC, " +
                "CASE WHEN v.TypeVehicule = 'D' THEN 0 ELSE 1 END ASC, " +
                "RANDOM()";

        return executeVehiculeAvailabilityQuery(connection, sql, nbPassagers, date, debutGroupe, finGroupe);
    }

    private List<Vehicule> executeVehiculeAvailabilityQuery(Connection connection, String sql,
                                                            int nbPassagers, java.time.LocalDate date,
                                                            java.time.LocalDateTime debutGroupe,
                                                            java.time.LocalDateTime finGroupe) throws SQLException {
        
        // ============================================
        // ETAPE 3 : Mapping des lignes SQL vers List<Vehicule>
        // ============================================
        List<Vehicule> vehicules = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Pour les COALESCE par défaut (si aucun trajet ce jour) = minuit du jour
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            
            // Dans la sous-requête
            statement.setDate(2, java.sql.Date.valueOf(date));
            
            // WHERE nbPlace >= ?
            statement.setInt(3, nbPassagers);
            
            // Cas A : COALESCE(tc.dernier_retour, ?) <= debutGroupe
            statement.setTimestamp(4, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(debutGroupe));
            
            // Cas B : première partie (> debutGroupe)
            statement.setTimestamp(6, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            statement.setTimestamp(7, java.sql.Timestamp.valueOf(debutGroupe));
            
            // Cas B : deuxième partie (<= finGroupe)
            statement.setTimestamp(8, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            statement.setTimestamp(9, java.sql.Timestamp.valueOf(finGroupe));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setIdVehicule(resultSet.getInt("Id_Vehicule"));
                    vehicule.setReference(resultSet.getString("Reference"));
                    vehicule.setNbPlace(resultSet.getInt("nbPlace"));
                    vehicule.setTypeVehicule(resultSet.getString("TypeVehicule"));
                    vehicule.setTrajetCount(resultSet.getInt("trajet_count"));
                    vehicule.setDernierRetour(resultSet.getTimestamp("dernier_retour").toLocalDateTime());
                    vehicules.add(vehicule);
                }
            }
        }

        return vehicules;
    }

    /**
     * Retourne les véhicules candidats pour un split de réservation.
     * ÉTAPE A : Récupère et trie les véhicules en DESC par capacité, sans filtrage strict.
     * L'ÉTAPE B (sélection "plus proche") se fera dans AssignationService.
     * 
     * Contrairement à getVehiculesDisponible, cette méthode retourne de possibles
     * candidats même si leur capacité < passagersRestants (pour supporter le split).
     * 
     * @param passagersRestants Nombre de passagers restants à assigner
     * @param date Date d'assignation demandée
     * @param debutGroupe Heure actuelle de départ du groupe
     * @param finGroupe Heure de fin du groupe (première réservation + temps attente)
     * @param connection Connexion existante (pour la transaction)
     * @return Liste des véhicules candidats triés par capacité DESC, 
     *         puis par trajet_count ASC, diesel avant essence, et random
     */
    public List<Vehicule> getVehiculesCandidatsPourSplit(int passagersRestants, 
            java.time.LocalDate date, java.time.LocalDateTime debutGroupe, 
            java.time.LocalDateTime finGroupe, Connection connection) throws SQLException {
        
        // ============================================
        // Même logique que getVehiculesDisponible
        // MAIS sans le filtre "nbPlace >= passagersRestants"
        // et tri différent : capacité DESC en priorité
        // ============================================
        
        String sql = "SELECT v.Id_Vehicule, v.Reference, v.nbPlace, v.TypeVehicule, " +
                "COALESCE(tc.trajet_count, 0) AS trajet_count, " +
                "COALESCE(tc.dernier_retour, ?) AS dernier_retour " +
                "FROM dev.Vehicule v " +
                "LEFT JOIN (" +
                "    SELECT t.Id_Vehicule, " +
                "           COUNT(t.Id_Trajet) AS trajet_count, " +
                "           MAX(t.date_heure_retour) AS dernier_retour " +
                "    FROM dev.Trajet t " +
                "    WHERE DATE(t.date_assignation) = ? " +
                "    GROUP BY t.Id_Vehicule" +
                ") tc ON tc.Id_Vehicule = v.Id_Vehicule " +
                "WHERE " +
                "    COALESCE(tc.dernier_retour, ?) <= ? " +
                "    OR " +
                "    (COALESCE(tc.dernier_retour, ?) > ? AND COALESCE(tc.dernier_retour, ?) <= ?)" +
                " " +
                "ORDER BY v.nbPlace DESC, " +
                "trajet_count ASC, " +
                "CASE WHEN v.TypeVehicule = 'D' THEN 0 ELSE 1 END ASC, " +
                "RANDOM()";

        return executeVehiculeAvailabilityQueryForSplit(connection, sql, date, debutGroupe, finGroupe);
    }

    /**
     * Exécute la requête pour la récupération des candidats de split.
     * Similaire à executeVehiculeAvailabilityQuery mais sans paramètre nbPassagers.
     */
    private List<Vehicule> executeVehiculeAvailabilityQueryForSplit(Connection connection, String sql,
                                                                      java.time.LocalDate date,
                                                                      java.time.LocalDateTime debutGroupe,
                                                                      java.time.LocalDateTime finGroupe) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Pour les COALESCE par défaut (si aucun trajet ce jour) = minuit du jour
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            
            // Dans la sous-requête
            statement.setDate(2, java.sql.Date.valueOf(date));
            
            // Cas A : COALESCE(tc.dernier_retour, ?) <= debutGroupe
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            statement.setTimestamp(4, java.sql.Timestamp.valueOf(debutGroupe));
            
            // Cas B : première partie (> debutGroupe)
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            statement.setTimestamp(6, java.sql.Timestamp.valueOf(debutGroupe));
            
            // Cas B : deuxième partie (<= finGroupe)
            statement.setTimestamp(7, java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT)));
            statement.setTimestamp(8, java.sql.Timestamp.valueOf(finGroupe));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setIdVehicule(resultSet.getInt("Id_Vehicule"));
                    vehicule.setReference(resultSet.getString("Reference"));
                    vehicule.setNbPlace(resultSet.getInt("nbPlace"));
                    vehicule.setTypeVehicule(resultSet.getString("TypeVehicule"));
                    vehicule.setTrajetCount(resultSet.getInt("trajet_count"));
                    vehicule.setDernierRetour(resultSet.getTimestamp("dernier_retour").toLocalDateTime());
                    vehicules.add(vehicule);
                }
            }
        }

        return vehicules;
    }
}
