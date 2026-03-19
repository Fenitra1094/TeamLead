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
     * Point d'entree principal pour Sprint 7.
     * Algorithme : division/split des réservations avec remplissage intelligent des véhicules.
     * 
     * Règles métier Sprint 7 :
     * - Une réservation devient divisible si aucun véhicule unique ne peut l'absorber entièrement
     * - Traitement par réservation (DESC par nbPassager), avec TOUTES ses divisions avant la suivante
     * - Tri véhicule : 2 phases = DESC en récupération, puis "plus proche" au choix
     * - Si un véhicule est entamé, tentative de remplissage avec autres réservations compatibles
     * - Passagers restants non assignables sont reportés au groupe suivant
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

                // Traitement de chaque groupe
                for (int idxGroupe = 0; idxGroupe < groupes.size(); idxGroupe++) {
                    GroupeTemps groupe = groupes.get(idxGroupe);
                    boolean isLastGroupe = (idxGroupe == groupes.size() - 1);

                    // ===== ALGORITHME SPRINT 7 : SPLIT + REMPLISSAGE INTELLIGENT =====
                    // reservationsRestantes = groupe.reservations + reservations_reportees_groupes_precedents
                    List<Reservation> reservationsRestantes = new ArrayList<>(groupe.getReservations());
                    reservationsRestantes.addAll(reservationsReportees);
                    reservationsReportees.clear();

                    // Trier par nbPassager DESC
                    reservationsRestantes.sort((r1, r2) -> Integer.compare(r2.getNbPassager(), r1.getNbPassager()));

                    LocalDateTime dateDepart = groupe.getHeureDepartGroupe();
                    LocalDateTime finGroupe = dateDepart.plusMinutes(tempsAttenteMinutes);

                    // Suivi des véhicules et de leur capacité pour ce groupe
                    java.util.Map<Integer, Integer> capaciteParVehicule = new java.util.HashMap<>();
                    java.util.Map<Integer, Trajet> trajetParVehicule = new java.util.HashMap<>(); // Track des trajets créés

                    // POUR chaque reservation R (dans l'ordre DESC)
                    for (int idxRes = 0; idxRes < reservationsRestantes.size(); idxRes++) {
                        Reservation reservationCourante = reservationsRestantes.get(idxRes);
                        int passagersRestants = reservationCourante.getNbPassager();

                        // TANT QUE passagersRestants > 0
                        while (passagersRestants > 0) {
                            // candidats = getVehiculesCandidatsPourSplit(...)
                            List<Vehicule> candidats = vehiculeRepository.getVehiculesCandidatsPourSplit(
                                    passagersRestants, date, dateDepart, finGroupe, connection);

                            // SI candidats vide : créer assignation "non assignée" avec la quantité exacte
                            if (candidats.isEmpty()) {
                                Assignation assignationNonAssignee = new Assignation();
                                assignationNonAssignee.setIdReservation(reservationCourante.getIdReservation());
                                assignationNonAssignee.setIdVehicule(0);  // 0 = non assigné
                                assignationNonAssignee.setIdTrajet(null);
                                assignationNonAssignee.setDateHeureDepart(dateDepart);
                                assignationNonAssignee.setDateHeureRetour(finGroupe);
                                assignationNonAssignee.setQuantitePassagersAssignes(passagersRestants);  // Persister le reste exact
                                assignationNonAssignee.setReservation(reservationCourante);

                                assignationRepository.insertAssignation(assignationNonAssignee, connection);
                                assignations.add(assignationNonAssignee);
                                
                                // Marquer comme non assignée et quitter la boucle
                                if (!reservationsNonAssignees.contains(reservationCourante)) {
                                    reservationsNonAssignees.add(reservationCourante);
                                }
                                passagersRestants = 0;
                                break;
                            }

                            // vehicule = meilleur candidat (ÉTAPE B : sélection "plus proche")
                            Vehicule vehiculeChoisi = selectionnerVehiculePlusProche(
                                    candidats, passagersRestants, capaciteParVehicule);

                            // capaciteDisponibleVehicule = places restantes du véhicule
                            int capaciteDisponible = vehiculeChoisi.getNbPlace() - 
                                    capaciteParVehicule.getOrDefault(vehiculeChoisi.getIdVehicule(), 0);

                            // SI capaciteDisponible <= 0 : passer au candidat suivant
                            if (capaciteDisponible <= 0) {
                                candidats.remove(vehiculeChoisi);
                                continue;
                            }

                            // personnesAEmbarquer = MIN(passagersRestants, capaciteDisponible)
                            int personnesAEmbarquer = Math.min(passagersRestants, capaciteDisponible);

                            // Créer/mettre à jour assignation partielle
                            LocalDateTime dateDispoVehicule = dateDepart;
                            if (vehiculeChoisi.getDernierRetour() != null &&
                                vehiculeChoisi.getDernierRetour().isAfter(dateDepart)) {
                                dateDispoVehicule = vehiculeChoisi.getDernierRetour();
                            }

                            // Création du trajet si premiere assignation du véhicule dans ce groupe
                            Integer idTrajet = null;
                            if (!capaciteParVehicule.containsKey(vehiculeChoisi.getIdVehicule())) {
                                // Créer trajet
                                List<Integer> hotelsDansVehicule = new ArrayList<>();
                                hotelsDansVehicule.add(reservationCourante.getHotel().getIdHotel());

                                List<TrajetEtape> etapes = routeService.calculerRoute(hotelsDansVehicule);
                                DureeResult dureeResult = dureeService.calculerDureeMultiStop(dateDispoVehicule, etapes);

                                if (dureeResult.getDureeMinutes() <= 0) {
                                    // Véhicule non valide, passer au suivant
                                    candidats.remove(vehiculeChoisi);
                                    continue;
                                }

                                Trajet trajet = new Trajet(0, vehiculeChoisi.getIdVehicule(),
                                        dateDispoVehicule,
                                        dureeResult.getDateRetour(),
                                        date);
                                trajet.setVehicule(vehiculeChoisi);
                                trajet.setEtapes(new ArrayList<>());

                                idTrajet = trajetRepository.insertTrajet(trajet, connection);
                                trajet.setIdTrajet(idTrajet);

                                for (TrajetEtape etape : etapes) {
                                    etape.setIdTrajet(idTrajet);
                                    trajetRepository.insertTrajetEtape(etape, connection);
                                    trajet.getEtapes().add(etape);
                                }
                                trajets.add(trajet);

                                capaciteParVehicule.put(vehiculeChoisi.getIdVehicule(), 0);
                                trajetParVehicule.put(vehiculeChoisi.getIdVehicule(), trajet); // Track du trajet
                                dateDepart = dateDispoVehicule;
                            } else {
                                // Trajet déjà créé pour ce véhicule - récupérer du cache
                                Trajet trajetExistant = trajetParVehicule.get(vehiculeChoisi.getIdVehicule());
                                if (trajetExistant != null) {
                                    idTrajet = trajetExistant.getIdTrajet();
                                } else {
                                    // Fallback si le trajet n'est pas en cache (ne devrait pas arriver)
                                    idTrajet = trajetRepository.getTrajetIdParVehiculeEtDate(
                                            vehiculeChoisi.getIdVehicule(), date, connection);
                                }
                            }

                            // Créer assignation partielle
                            Assignation assignation = new Assignation();
                            assignation.setIdReservation(reservationCourante.getIdReservation());
                            assignation.setIdVehicule(vehiculeChoisi.getIdVehicule());
                            assignation.setIdTrajet(idTrajet);
                            assignation.setDateHeureDepart(dateDepart);
                            // Récupérer dateHeureRetour du trajet
                            if (trajetParVehicule.containsKey(vehiculeChoisi.getIdVehicule())) {
                                assignation.setDateHeureRetour(trajetParVehicule.get(vehiculeChoisi.getIdVehicule()).getDateHeureRetour());
                            }
                            assignation.setQuantitePassagersAssignes(personnesAEmbarquer);
                            assignation.setReservation(reservationCourante);
                            assignation.setVehicule(vehiculeChoisi);

                            assignationRepository.insertAssignation(assignation, connection);
                            assignations.add(assignation);

                            // Mettre à jour la capacité du véhicule
                            int nouvelleCapacite = capaciteParVehicule.get(vehiculeChoisi.getIdVehicule()) + personnesAEmbarquer;
                            capaciteParVehicule.put(vehiculeChoisi.getIdVehicule(), nouvelleCapacite);

                            // Réduire les passagers restants
                            passagersRestants -= personnesAEmbarquer;

                            // SI vehicule encore non plein : chercher à remplir avec autres réservations
                            int capaciteRestante = vehiculeChoisi.getNbPlace() - nouvelleCapacite;
                            if (capaciteRestante > 0) {
                                // Parcourir en bas de la liste (réservations pas encore traitées)
                                for (int idxFiller = idxRes + 1; idxFiller < reservationsRestantes.size() && capaciteRestante > 0;) {
                                    Reservation reservationFiller = reservationsRestantes.get(idxFiller);
                                    if (reservationFiller.getNbPassager() <= capaciteRestante) {
                                        // Ajouter cette réservation au véhicule
                                        Assignation assignationFiller = new Assignation();
                                        assignationFiller.setIdReservation(reservationFiller.getIdReservation());
                                        assignationFiller.setIdVehicule(vehiculeChoisi.getIdVehicule());
                                        assignationFiller.setIdTrajet(idTrajet);
                                        assignationFiller.setDateHeureDepart(dateDepart);
                                        if (trajetParVehicule.containsKey(vehiculeChoisi.getIdVehicule())) {
                                            assignationFiller.setDateHeureRetour(trajetParVehicule.get(vehiculeChoisi.getIdVehicule()).getDateHeureRetour());
                                        }
                                        assignationFiller.setQuantitePassagersAssignes(reservationFiller.getNbPassager());
                                        assignationFiller.setReservation(reservationFiller);
                                        assignationFiller.setVehicule(vehiculeChoisi);

                                        assignationRepository.insertAssignation(assignationFiller, connection);
                                        assignations.add(assignationFiller);

                                        capaciteRestante -= reservationFiller.getNbPassager();
                                        nouvelleCapacite += reservationFiller.getNbPassager();
                                        capaciteParVehicule.put(vehiculeChoisi.getIdVehicule(), nouvelleCapacite);

                                        reservationsRestantes.remove(idxFiller);
                                    } else {
                                        idxFiller++;
                                    }
                                }
                            }
                        }
                    }

                    // reservations_reportees = restes non assignés du groupe courant
                    // SI dernier groupe : les restes deviennent definitives non assignees
                    if (isLastGroupe) {
                        reservationsNonAssignees.addAll(reservationsRestantes);
                    } else {
                        reservationsReportees.addAll(reservationsRestantes);
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw e;
            } catch (RuntimeException e) {
                rollbackQuietly(connection);
                throw new SQLException("Erreur lors de l'assignation Sprint 7 (split): " + e.getMessage(), e);
            }
        }

        AssignationResult result = new AssignationResult(assignations, reservationsNonAssignees, trajets);
        result.setGroupes(groupes);
        return result;
    }

    /**
     * Sélectionne le véhicule "plus proche" du besoin (ÉTAPE B du tri Sprint 7).
     * 
     * Ordre de sélection :
     * 1) Capacité >= passagersRestants en priorité
     * 2) écart(capacité - passagersRestants) ASC (plus proche)
     * 3) nombre de trajets du jour ASC
     * 4) diesel avant essence (TypeVehicule = 'D')
     * 5) random (peut être ajouté si nécessaire)
     * 
     * @param candidats Liste triée DESC par capacité (ÉTAPE A)
     * @param passagersRestants Nombre de passagers à traiter
     * @param capaciteParVehicule Map de la capacité utilisée par véhicule dans ce groupe
     * @return Véhicule le plus proche du besoin
     */
    private Vehicule selectionnerVehiculePlusProche(List<Vehicule> candidats, int passagersRestants,
                                                     java.util.Map<Integer, Integer> capaciteParVehicule) {
        if (candidats.isEmpty()) {
            return null;
        }
        if (candidats.size() == 1) {
            return candidats.get(0);
        }

        // Partitionner : capacité suffisante vs insufficient
        List<Vehicule> avecCapaciteSuffisante = new ArrayList<>();
        List<Vehicule> sansCapaciteSuffisante = new ArrayList<>();

        for (Vehicule v : candidats) {
            int capaciteDisponible = v.getNbPlace() - capaciteParVehicule.getOrDefault(v.getIdVehicule(), 0);
            if (capaciteDisponible >= passagersRestants) {
                avecCapaciteSuffisante.add(v);
            } else {
                sansCapaciteSuffisante.add(v);
            }
        }

        // Priorité : capacité suffisante
        List<Vehicule> selection = avecCapaciteSuffisante.isEmpty() ? sansCapaciteSuffisante : avecCapaciteSuffisante;

        if (selection.isEmpty()) {
            return candidats.get(0);
        }

        // Trier par écart(capacité - passagersRestants) ASC
        selection.sort((v1, v2) -> {
            int ecart1 = Math.abs(v1.getNbPlace() - passagersRestants);
            int ecart2 = Math.abs(v2.getNbPlace() - passagersRestants);
            if (ecart1 != ecart2) {
                return Integer.compare(ecart1, ecart2);
            }

            // Tie-breaker 2 : nb trajets du jour ASC
            if (v1.getTrajetCount() != v2.getTrajetCount()) {
                return Integer.compare(v1.getTrajetCount(), v2.getTrajetCount());
            }

            // Tie-breaker 3 : diesel avant essence
            boolean isDiesel1 = "D".equalsIgnoreCase(v1.getTypeVehicule());
            boolean isDiesel2 = "D".equalsIgnoreCase(v2.getTypeVehicule());
            if (isDiesel1 != isDiesel2) {
                return isDiesel1 ? -1 : 1;
            }

            // Tie-breaker 4 : random (maintenant on retourne juste le premier)
            return 0;
        });

        return selection.get(0);
    }

    /**
     * @deprecated Méthode obsolète Sprint 6. Remplacée par le tri DESC + sélection "plus proche".
     */
    @Deprecated
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