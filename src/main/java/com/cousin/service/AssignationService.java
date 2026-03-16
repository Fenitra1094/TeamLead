package com.cousin.service;

import com.cousin.model.Assignation;
import com.cousin.model.Reservation;
import com.cousin.model.Trajet;
import com.cousin.model.TrajetEtape;
import com.cousin.model.Vehicule;
import com.cousin.repository.AssignationRepository;
import com.cousin.repository.ReservationRepository;
import com.cousin.repository.TrajetRepository;
import com.cousin.repository.VehiculeRepository;
import com.cousin.util.AssignationResult;
import com.cousin.util.DbConnection;
import com.cousin.util.DureeResult;
import com.cousin.util.GroupeTemps;
import com.cousin.util.GroupeVol;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AssignationService {
    private final AssignationRepository assignationRepository;
    private final ReservationRepository reservationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetRepository trajetRepository;
    private final GroupingService groupingService;
    private final ParametreService parametreService;
    private final RouteService routeService;
    private final DureeService dureeService;

    public AssignationService() {
        this.assignationRepository = new AssignationRepository();
        this.reservationRepository = new ReservationRepository();
        this.vehiculeRepository = new VehiculeRepository();
        this.trajetRepository = new TrajetRepository();
        this.groupingService = new GroupingService();
        this.parametreService = new ParametreService();
        this.routeService = new RouteService();
        this.dureeService = new DureeService();
    }

    /**
     * Point d'entree principal en mode Sprint 4 only.
     */
    public AssignationResult assignerPourDate(LocalDate date, int tempsAttenteMinutes) throws SQLException {
        List<Assignation> assignations = new ArrayList<>();
        List<Reservation> reservationsNonAssignees = new ArrayList<>();
        List<Trajet> trajets = new ArrayList<>();
        List<GroupeTemps> groupes = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false); // BEGIN TRANSACTION

            try {
                // Re-traitement de date: suppression puis recalcul.
                assignationRepository.deleteAssignationsForDate(date, connection);
                trajetRepository.deleteTrajetsByDate(date, connection);

                List<Reservation> reservations = reservationRepository.getReservationsByDate(date, connection);
                groupes = groupingService.grouperParTempsAttente(reservations, tempsAttenteMinutes);

                for (GroupeTemps groupe : groupes) {
                    LocalDateTime dateDepart = groupe.getHeureDepartGroupe();
                    List<Reservation> restantes = new ArrayList<>(groupe.getReservations());

                    while (!restantes.isEmpty()) {
                        Reservation reservationPivot = restantes.remove(0);
                        LocalDateTime dateRetourEstime = estimerDateRetour(dateDepart);

                        List<Vehicule> vehiculesDisponibles = vehiculeRepository.getVehiculesDisponible(
                                reservationPivot.getNbPassager(),
                                dateDepart,
                                dateRetourEstime,
                                connection);

                        if (vehiculesDisponibles.isEmpty()) {
                            reservationsNonAssignees.add(reservationPivot);
                            continue;
                        }

                        Vehicule vehiculeChoisi = vehiculesDisponibles.get(0);
                        int placesRestantes = vehiculeChoisi.getNbPlace() - reservationPivot.getNbPassager();

                        List<Reservation> reservationsDansVehicule = new ArrayList<>();
                        reservationsDansVehicule.add(reservationPivot);

                        Set<Integer> hotelsDansVehiculeSet = new LinkedHashSet<>();
                        if (reservationPivot.getHotel() != null) {
                            hotelsDansVehiculeSet.add(reservationPivot.getHotel().getIdHotel());
                        }

                        for (int i = 0; i < restantes.size();) {
                            Reservation reservationCandidate = restantes.get(i);
                            if (reservationCandidate.getNbPassager() <= placesRestantes) {
                                reservationsDansVehicule.add(reservationCandidate);
                                placesRestantes -= reservationCandidate.getNbPassager();

                                if (reservationCandidate.getHotel() != null) {
                                    hotelsDansVehiculeSet.add(reservationCandidate.getHotel().getIdHotel());
                                }
                                restantes.remove(i);
                            } else {
                                i++;
                            }
                        }

                        List<Integer> hotelsDansVehicule = new ArrayList<>(hotelsDansVehiculeSet);
                        List<TrajetEtape> etapes = routeService.calculerRoute(hotelsDansVehicule);
                        DureeResult dureeResult = dureeService.calculerDureeMultiStop(dateDepart, etapes);

                        // Regle metier Sprint 3 conservee: distance 0 => non assignee.
                        if (dureeResult.getDureeMinutes() <= 0) {
                            reservationsNonAssignees.addAll(reservationsDansVehicule);
                            continue;
                        }

                        Trajet trajet = new Trajet(0, vehiculeChoisi.getIdVehicule(),
                                dateDepart,
                                dureeResult.getDateRetour(),
                                date);
                        trajet.setVehicule(vehiculeChoisi);
                        trajet.setEtapes(new ArrayList<>());

                        int idTrajet = trajetRepository.insertTrajet(trajet, connection);
                        trajet.setIdTrajet(idTrajet);

                        for (TrajetEtape etape : etapes) {
                            etape.setIdTrajet(idTrajet);
                            trajetRepository.insertTrajetEtape(etape, connection);
                            trajet.getEtapes().add(etape);
                        }
                        trajets.add(trajet);

                        for (Reservation reservationAssignee : reservationsDansVehicule) {
                            Assignation assignation = new Assignation();
                            assignation.setIdReservation(reservationAssignee.getIdReservation());
                            assignation.setIdVehicule(vehiculeChoisi.getIdVehicule());
                            assignation.setIdTrajet(idTrajet);
                            assignation.setDateHeureDepart(dateDepart);
                            assignation.setDateHeureRetour(dureeResult.getDateRetour());
                            assignation.setReservation(reservationAssignee);
                            assignation.setVehicule(vehiculeChoisi);

                            assignationRepository.insertAssignation(assignation, connection);
                            assignations.add(assignation);
                        }
                    }
                }

                connection.commit(); // COMMIT
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw e;
            } catch (RuntimeException e) {
                rollbackQuietly(connection);
                throw new SQLException("Erreur lors de l'assignation multi-client: " + e.getMessage(), e);
            }
        }

        AssignationResult result = new AssignationResult(assignations, reservationsNonAssignees, trajets);
        result.setGroupes(groupes);
        return result;
    }

    private LocalDateTime estimerDateRetour(LocalDateTime dateDepart) {
        return dateDepart.toLocalDate().plusDays(1).atStartOfDay();
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Nothing to do.
        }
    }
}