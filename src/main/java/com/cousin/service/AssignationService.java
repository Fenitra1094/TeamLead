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
     * Point d'entree principal pour Sprint 6.
     * Algorithme : considère la charge du jour et le retour des véhicules
     */
    public AssignationResult assignerPourDate(LocalDate date, int tempsAttenteMinutes) throws SQLException {
        List<Assignation> assignations = new ArrayList<>();
        List<Reservation> reservationsNonAssignees = new ArrayList<>();
        List<Trajet> trajets = new ArrayList<>();
        List<GroupeTemps> groupes = new ArrayList<>();
        List<Reservation> reservationsReportees = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false); // BEGIN TRANSACTION

            try {
                // Re-traitement de date: suppression puis recalcul.
                assignationRepository.deleteAssignationsForDate(date, connection);
                trajetRepository.deleteTrajetsByDate(date, connection);

                List<Reservation> reservations = reservationRepository.getReservationsByDate(date, connection);
                groupes = groupingService.grouperParTempsAttente(reservations, tempsAttenteMinutes);

                // Traitement de chaque groupe (possiblement plusieurs groupes par jour)
                for (int idxGroupe = 0; idxGroupe < groupes.size(); idxGroupe++) {
                    GroupeTemps groupe = groupes.get(idxGroupe);
                    boolean isLastGroupe = (idxGroupe == groupes.size() - 1);

                    // ===== ALGORITHME BRANCHE 2 =====
                    // reservationsRestantes = groupe.reservations + reservations_reportees_groupes_precedents
                    List<Reservation> reservationsRestantes = new ArrayList<>(groupe.getReservations());
                    reservationsRestantes.addAll(reservationsReportees);
                    reservationsReportees.clear(); // Réinitialiser pour le groupe suivant

                    LocalDateTime dateDepart = groupe.getHeureDepartGroupe();
                    
                    // fin_groupe = premiere_reservation_du_groupe + tempsAttenteMinutes
                    LocalDateTime finGroupe = dateDepart.plusMinutes(tempsAttenteMinutes);

                    // TANT QUE reservationsRestantes non vide
                    while (!reservationsRestantes.isEmpty()) {
                        // R = reservationsRestantes (plus grand nbPassager)
                        Reservation reservationPivot = extraireReservationMaxPassagers(reservationsRestantes);

                        // candidats = getVehiculesDisponible(R.nbPassager, date, dateDepart, finGroupe, conn)
                        List<Vehicule> candidats = vehiculeRepository.getVehiculesDisponible(
                                reservationPivot.getNbPassager(),
                                date,
                                dateDepart,
                                finGroupe,
                                connection);

                        // SI candidats vide
                        if (candidats.isEmpty()) {
                            // marquer R comme "a reporter" => passer a la suivante
                            reservationsRestantes.remove(reservationPivot);
                            reservationsReportees.add(reservationPivot);
                            continue;
                        }

                        // SINON :
                        // vehicule = candidats.get(0)  // best candidate
                        Vehicule vehiculeChoisi = candidats.get(0);

                        // dateDispoVehicule = MAX(dateDepart, vehicule.dernierRetour)
                        LocalDateTime dateDispoVehicule = dateDepart;
                        if (vehiculeChoisi.getDernierRetour() != null &&
                            vehiculeChoisi.getDernierRetour().isAfter(dateDepart)) {
                            dateDispoVehicule = vehiculeChoisi.getDernierRetour();
                        }

                        // IMPORTANT: dateHeureDepart est mise a jour seulement si le vehicule
                        // est effectivement assigne a une reservation
                        // dateDepart = MAX(dateDepart, dateDispoVehicule)
                        dateDepart = dateDispoVehicule;

                        // Remplir vehicule : R + autres reservations compatibles
                        int placesRestantes = vehiculeChoisi.getNbPlace() - reservationPivot.getNbPassager();
                        List<Reservation> reservationsDansVehicule = new ArrayList<>();
                        reservationsDansVehicule.add(reservationPivot);

                        Set<Integer> hotelsDansVehiculeSet = new LinkedHashSet<>();
                        if (reservationPivot.getHotel() != null) {
                            hotelsDansVehiculeSet.add(reservationPivot.getHotel().getIdHotel());
                        }

                        for (int i = 0; i < reservationsRestantes.size();) {
                            Reservation reservationCandidate = reservationsRestantes.get(i);
                            if (reservationCandidate.getNbPassager() <= placesRestantes) {
                                reservationsDansVehicule.add(reservationCandidate);
                                placesRestantes -= reservationCandidate.getNbPassager();

                                if (reservationCandidate.getHotel() != null) {
                                    hotelsDansVehiculeSet.add(reservationCandidate.getHotel().getIdHotel());
                                }
                                reservationsRestantes.remove(i);
                            } else {
                                i++;
                            }
                        }

                        // Creer Trajet et Assignations
                        List<Integer> hotelsDansVehicule = new ArrayList<>(hotelsDansVehiculeSet);
                        List<TrajetEtape> etapes = routeService.calculerRoute(hotelsDansVehicule);
                        DureeResult dureeResult = dureeService.calculerDureeMultiStop(dateDepart, etapes);

                        // Regle metier conservee: distance 0 => non assignee.
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

                    // reservations_reportees = reservationsRestantes (portees au groupe suivant)
                    // SI dernier groupe : elles deviennent reservationsNonAssignees
                    if (isLastGroupe) {
                        reservationsNonAssignees.addAll(reservationsReportees);
                        reservationsReportees.clear();
                    }
                }

                connection.commit(); // COMMIT
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw e;
            } catch (RuntimeException e) {
                rollbackQuietly(connection);
                throw new SQLException("Erreur lors de l'assignation Sprint 6: " + e.getMessage(), e);
            }
        }

        AssignationResult result = new AssignationResult(assignations, reservationsNonAssignees, trajets);
        result.setGroupes(groupes);
        return result;
    }

    /**
     * Extrait la réservation avec le plus grand nombre de passagers.
     */
    private Reservation extraireReservationMaxPassagers(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return null;
        }
        Reservation max = reservations.get(0);
        for (Reservation r : reservations) {
            if (r.getNbPassager() > max.getNbPassager()) {
                max = r;
            }
        }
        reservations.remove(max);
        return max;
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