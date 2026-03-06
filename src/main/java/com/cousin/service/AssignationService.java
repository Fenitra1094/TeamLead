package com.cousin.service;

import com.cousin.model.Assignation;
import com.cousin.model.Reservation;
import com.cousin.model.Vehicule;
import com.cousin.repository.AssignationRepository;
import com.cousin.repository.ReservationRepository;
import com.cousin.repository.VehiculeRepository;
import com.cousin.util.AssignationResult;
import com.cousin.util.DbConnection;
import com.cousin.util.DureeResult;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssignationService {
    private final AssignationRepository assignationRepository;
    private final ReservationRepository reservationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final DureeService dureeService;

    public AssignationService() {
        this.assignationRepository = new AssignationRepository();
        this.reservationRepository = new ReservationRepository();
        this.vehiculeRepository = new VehiculeRepository();
        this.dureeService = new DureeService();
    }

    public AssignationResult assignerPourDate(LocalDate date) throws SQLException {
        List<Assignation> assignations = new ArrayList<>();
        List<Reservation> reservationsNonAssignees = new ArrayList<>();

        Connection connection = null;
        try {
            connection = DbConnection.getConnection();
            connection.setAutoCommit(false); // BEGIN TRANSACTION

            // Supprimer les anciennes assignations pour cette date (permet de re-traiter)
            assignationRepository.deleteAssignationsForDate(date, connection);

            // Récupérer les réservations du jour
            List<Reservation> reservations = reservationRepository.getReservationsByDate(date, connection);

            for (Reservation r : reservations) {
                // Calculer la durée aller-retour aéroport <-> hôtel
                DureeResult dureeResult = dureeService.calculerDuree(
                        r.getDateHeureArrive(), r.getHotel().getIdHotel());

                // Si la durée est <= 0 (distance = 0, hôtel = aéroport), pas besoin de véhicule
                if (dureeResult.getDureeMinutes() <= 0) {
                    reservationsNonAssignees.add(r);
                    continue;
                }

                // Chercher les véhicules disponibles
                // La requête SQL applique déjà les RG1-RG4 :
                //   ORDER BY nbPlace ASC (RG2), TypeVehicule D first (RG3), RANDOM() (RG4)
                //   WHERE nbPlace >= nbPassager (RG1)
                List<Vehicule> vehiculesDisponibles = vehiculeRepository.getVehiculesDisponible(
                        r.getNbPassager(),
                        dureeResult.getDateDepart(),
                        dureeResult.getDateRetour(),
                        connection);

                if (vehiculesDisponibles.isEmpty()) {
                    // Aucun véhicule disponible pour cette réservation
                    reservationsNonAssignees.add(r);
                    continue;
                }

                // Prendre le premier véhicule (déjà trié par les règles de priorisation)
                Vehicule vehiculeChoisi = vehiculesDisponibles.get(0);

                // Créer l'assignation
                Assignation assignation = new Assignation();
                assignation.setIdReservation(r.getIdReservation());
                assignation.setIdVehicule(vehiculeChoisi.getIdVehicule());
                assignation.setDateHeureDepart(dureeResult.getDateDepart());
                assignation.setDateHeureRetour(dureeResult.getDateRetour());

                // Pour l'affichage
                assignation.setReservation(r);
                assignation.setVehicule(vehiculeChoisi);

                assignationRepository.insertAssignation(assignation, connection);
                assignations.add(assignation);
            }

            connection.commit(); // COMMIT

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback(); // ROLLBACK si erreur
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("Erreur lors de l'assignation: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }

        return new AssignationResult(assignations, reservationsNonAssignees);
    }
}