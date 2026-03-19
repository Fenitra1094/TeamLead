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
            connection.setAutoCommit(false);

            try {
                assignationRepository.deleteAssignationsForDate(date, connection);
                trajetRepository.deleteTrajetsByDate(date, connection);

                List<Reservation> reservations = reservationRepository.getReservationsByDate(date, connection);
                groupes = groupingService.grouperParTempsAttente(reservations, tempsAttenteMinutes);

                for (int idxGroupe = 0; idxGroupe < groupes.size(); idxGroupe++) {
                    GroupeTemps groupe = groupes.get(idxGroupe);
                    boolean isLastGroupe = (idxGroupe == groupes.size() - 1);

                    List<Reservation> reservationsATraiter = new ArrayList<>(groupe.getReservations());
                    reservationsATraiter.addAll(reservationsReportees);
                    reservationsReportees.clear();
                    reservationsATraiter.sort((r1, r2) -> Integer.compare(r2.getNbPassager(), r1.getNbPassager()));

                    LocalDateTime dateDepartGroupe = groupe.getHeureDepartGroupe();
                    LocalDateTime finGroupe = dateDepartGroupe.plusMinutes(tempsAttenteMinutes);

                    java.util.Map<Integer, Integer> capaciteUtiliseeParVehicule = new java.util.HashMap<>();
                    java.util.Map<Integer, Trajet> trajetParVehicule = new java.util.HashMap<>();
                    java.util.Set<Integer> reservationsTraitees = new java.util.HashSet<>();
                    List<Reservation> reportsDuGroupe = new ArrayList<>();

                    for (int idxRes = 0; idxRes < reservationsATraiter.size(); idxRes++) {
                        Reservation reservationCourante = reservationsATraiter.get(idxRes);

                        if (reservationsTraitees.contains(reservationCourante.getIdReservation())) {
                            continue;
                        }

                        int passagersRestants = reservationCourante.getNbPassager();
                        boolean dejaReportee = false;
                        java.util.Set<Integer> vehiculesExclus = new java.util.HashSet<>();

                        while (passagersRestants > 0) {
                            List<Vehicule> candidats = vehiculeRepository.getVehiculesCandidatsPourSplit(
                                    passagersRestants, date, dateDepartGroupe, finGroupe, connection);

                            Vehicule vehiculeChoisi = selectionnerVehiculePlusProche(
                                    candidats, passagersRestants, capaciteUtiliseeParVehicule, vehiculesExclus);

                            if (vehiculeChoisi == null) {
                                reportsDuGroupe.add(copierReservationAvecReste(reservationCourante, passagersRestants));
                                dejaReportee = true;
                                break;
                            }

                            int capaciteDisponible = getCapaciteDisponible(vehiculeChoisi, capaciteUtiliseeParVehicule);
                            if (capaciteDisponible <= 0) {
                                vehiculesExclus.add(vehiculeChoisi.getIdVehicule());
                                continue;
                            }

                            Trajet trajet = trajetParVehicule.get(vehiculeChoisi.getIdVehicule());
                            LocalDateTime dateDepartTrajet = dateDepartGroupe;
                            if (vehiculeChoisi.getDernierRetour() != null &&
                                    vehiculeChoisi.getDernierRetour().isAfter(dateDepartTrajet)) {
                                dateDepartTrajet = vehiculeChoisi.getDernierRetour();
                            }

                            if (trajet == null) {
                                List<Integer> hotelsDansVehicule = new ArrayList<>();
                                if (reservationCourante.getHotel() != null) {
                                    hotelsDansVehicule.add(reservationCourante.getHotel().getIdHotel());
                                }

                                List<TrajetEtape> etapes = routeService.calculerRoute(hotelsDansVehicule);
                                DureeResult dureeResult = dureeService.calculerDureeMultiStop(dateDepartTrajet, etapes);

                                if (dureeResult.getDureeMinutes() <= 0) {
                                    vehiculesExclus.add(vehiculeChoisi.getIdVehicule());
                                    continue;
                                }

                                trajet = new Trajet(0, vehiculeChoisi.getIdVehicule(),
                                        dateDepartTrajet, dureeResult.getDateRetour(), date);
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
                                trajetParVehicule.put(vehiculeChoisi.getIdVehicule(), trajet);
                                capaciteUtiliseeParVehicule.putIfAbsent(vehiculeChoisi.getIdVehicule(), 0);
                            }

                            int personnesAEmbarquer = Math.min(passagersRestants, capaciteDisponible);

                            Assignation assignation = new Assignation();
                            assignation.setIdReservation(reservationCourante.getIdReservation());
                            assignation.setIdVehicule(vehiculeChoisi.getIdVehicule());
                            assignation.setIdTrajet(trajet.getIdTrajet());
                            assignation.setDateHeureDepart(dateDepartTrajet);
                            assignation.setDateHeureRetour(trajet.getDateHeureRetour());
                            assignation.setQuantitePassagersAssignes(personnesAEmbarquer);
                            assignation.setReservation(reservationCourante);
                            assignation.setVehicule(vehiculeChoisi);

                            assignationRepository.insertAssignation(assignation, connection);
                            assignations.add(assignation);

                            int nouvelleUtilisation = capaciteUtiliseeParVehicule
                                    .getOrDefault(vehiculeChoisi.getIdVehicule(), 0)
                                    + personnesAEmbarquer;
                            capaciteUtiliseeParVehicule.put(vehiculeChoisi.getIdVehicule(), nouvelleUtilisation);

                            passagersRestants -= personnesAEmbarquer;

                            if (passagersRestants == 0) {
                                reservationsTraitees.add(reservationCourante.getIdReservation());
                            }

                            int capaciteRestante = getCapaciteDisponible(vehiculeChoisi, capaciteUtiliseeParVehicule);
                            if (capaciteRestante > 0) {
                                for (int idxFiller = idxRes + 1; idxFiller < reservationsATraiter.size()
                                        && capaciteRestante > 0; idxFiller++) {

                                    Reservation reservationFiller = reservationsATraiter.get(idxFiller);
                                    if (reservationsTraitees.contains(reservationFiller.getIdReservation())) {
                                        continue;
                                    }

                                    if (reservationFiller.getNbPassager() <= capaciteRestante) {
                                        Assignation assignationFiller = new Assignation();
                                        assignationFiller.setIdReservation(reservationFiller.getIdReservation());
                                        assignationFiller.setIdVehicule(vehiculeChoisi.getIdVehicule());
                                        assignationFiller.setIdTrajet(trajet.getIdTrajet());
                                        assignationFiller.setDateHeureDepart(dateDepartTrajet);
                                        assignationFiller.setDateHeureRetour(trajet.getDateHeureRetour());
                                        assignationFiller.setQuantitePassagersAssignes(reservationFiller.getNbPassager());
                                        assignationFiller.setReservation(reservationFiller);
                                        assignationFiller.setVehicule(vehiculeChoisi);

                                        assignationRepository.insertAssignation(assignationFiller, connection);
                                        assignations.add(assignationFiller);

                                        int utilisationMaj = capaciteUtiliseeParVehicule.getOrDefault(
                                                vehiculeChoisi.getIdVehicule(), 0) + reservationFiller.getNbPassager();
                                        capaciteUtiliseeParVehicule.put(vehiculeChoisi.getIdVehicule(), utilisationMaj);

                                        capaciteRestante -= reservationFiller.getNbPassager();
                                        reservationsTraitees.add(reservationFiller.getIdReservation());
                                    }
                                }
                            }
                        }

                        if (passagersRestants > 0 && !dejaReportee) {
                            reportsDuGroupe.add(copierReservationAvecReste(reservationCourante, passagersRestants));
                        }
                    }

                    if (isLastGroupe) {
                        reservationsNonAssignees.addAll(reportsDuGroupe);
                    } else {
                        reservationsReportees.addAll(reportsDuGroupe);
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
    
    private Vehicule selectionnerVehiculePlusProche(
            List<Vehicule> candidats,
            int passagersRestants,
            java.util.Map<Integer, Integer> capaciteParVehicule,
            java.util.Set<Integer> vehiculesExclus) {

        if (candidats == null || candidats.isEmpty()) {
            return null;
        }

        List<Vehicule> eligibles = new ArrayList<>();
        for (Vehicule v : candidats) {
            if (vehiculesExclus != null && vehiculesExclus.contains(v.getIdVehicule())) {
                continue;
            }
            if (getCapaciteDisponible(v, capaciteParVehicule) > 0) {
                eligibles.add(v);
            }
        }

        if (eligibles.isEmpty()) {
            return null;
        }

        List<Vehicule> avecCapaciteSuffisante = new ArrayList<>();
        List<Vehicule> sansCapaciteSuffisante = new ArrayList<>();

        for (Vehicule v : eligibles) {
            int capaciteDisponible = getCapaciteDisponible(v, capaciteParVehicule);
            if (capaciteDisponible >= passagersRestants) {
                avecCapaciteSuffisante.add(v);
            } else {
                sansCapaciteSuffisante.add(v);
            }
        }

        List<Vehicule> pool = avecCapaciteSuffisante.isEmpty() ? sansCapaciteSuffisante : avecCapaciteSuffisante;
        if (pool.isEmpty()) {
            return null;
        }

        pool.sort((v1, v2) -> {
            int c1 = getCapaciteDisponible(v1, capaciteParVehicule);
            int c2 = getCapaciteDisponible(v2, capaciteParVehicule);

            int ecart1 = Math.abs(c1 - passagersRestants);
            int ecart2 = Math.abs(c2 - passagersRestants);
            if (ecart1 != ecart2) {
                return Integer.compare(ecart1, ecart2);
            }

            if (v1.getTrajetCount() != v2.getTrajetCount()) {
                return Integer.compare(v1.getTrajetCount(), v2.getTrajetCount());
            }

            boolean d1 = "D".equalsIgnoreCase(v1.getTypeVehicule());
            boolean d2 = "D".equalsIgnoreCase(v2.getTypeVehicule());
            if (d1 != d2) {
                return d1 ? -1 : 1;
            }

            return Integer.compare(v1.getIdVehicule(), v2.getIdVehicule());
        });

        Vehicule premier = pool.get(0);
        int cRef = getCapaciteDisponible(premier, capaciteParVehicule);
        int eRef = Math.abs(cRef - passagersRestants);
        int tRef = premier.getTrajetCount();
        boolean dRef = "D".equalsIgnoreCase(premier.getTypeVehicule());

        List<Vehicule> ties = new ArrayList<>();
        for (Vehicule v : pool) {
            int c = getCapaciteDisponible(v, capaciteParVehicule);
            int e = Math.abs(c - passagersRestants);
            int t = v.getTrajetCount();
            boolean d = "D".equalsIgnoreCase(v.getTypeVehicule());

            if (e == eRef && t == tRef && d == dRef) {
                ties.add(v);
            } else {
                break;
            }
        }

        if (ties.size() == 1) {
            return premier;
        }

        int idxRandom = java.util.concurrent.ThreadLocalRandom.current().nextInt(ties.size());
        return ties.get(idxRandom);
    }

    private int getCapaciteDisponible(Vehicule vehicule, java.util.Map<Integer, Integer> capaciteParVehicule) {
        return vehicule.getNbPlace() - capaciteParVehicule.getOrDefault(vehicule.getIdVehicule(), 0);
    }

    private Reservation copierReservationAvecReste(Reservation source, int nbPassagerReste) {
        Reservation reste = new Reservation();
        reste.setIdReservation(source.getIdReservation());
        reste.setDateHeureArrive(source.getDateHeureArrive());
        reste.setIdClient(source.getIdClient());
        reste.setHotel(source.getHotel());
        reste.setNbPassager(nbPassagerReste);
        return reste;
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