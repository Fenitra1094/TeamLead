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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
                    java.util.Map<Integer, java.util.LinkedHashSet<Integer>> hotelsParVehicule = new java.util.HashMap<>();
                    java.util.Map<Integer, List<Assignation>> assignationsParVehicule = new java.util.HashMap<>();
                    java.util.Set<Integer> reservationsTraitees = new java.util.HashSet<>();
                    List<Reservation> reportsDuGroupe = new ArrayList<>();
                    List<Trajet> trajetsDuGroupe = new ArrayList<>();

                    java.util.Map<Integer, Reservation> reservationsParIdDansGroupe = new java.util.LinkedHashMap<>();
                    for (Reservation reservation : reservationsATraiter) {
                        if (reservation != null && reservation.getIdReservation() > 0) {
                            reservationsParIdDansGroupe.putIfAbsent(reservation.getIdReservation(), reservation);
                        }
                    }

                    for (int idxRes = 0; idxRes < reservationsATraiter.size(); idxRes++) {
                        Reservation reservationCourante = reservationsATraiter.get(idxRes);

                        if (reservationCourante == null || reservationCourante.getIdReservation() <= 0) {
                            continue;
                        }
                        if (reservationsTraitees.contains(reservationCourante.getIdReservation())) {
                            continue;
                        }
                        if (reservationCourante.getHotel() == null) {
                            System.err.println("WARN: Reservation " + reservationCourante.getIdReservation() + " has no hotel, skipping");
                            continue;
                        }

                        int passagersRestants = reservationCourante.getNbPassager();
                        boolean dejaReportee = false;
                        java.util.Set<Integer> vehiculesExclus = new java.util.HashSet<>();

                        while (passagersRestants > 0) {
                            try {
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
                                    trajetsDuGroupe.add(trajet);
                                    trajetParVehicule.put(vehiculeChoisi.getIdVehicule(), trajet);
                                    capaciteUtiliseeParVehicule.putIfAbsent(vehiculeChoisi.getIdVehicule(), 0);

                                    java.util.LinkedHashSet<Integer> hotelsVehicule = new java.util.LinkedHashSet<>();
                                    if (reservationCourante.getHotel() != null) {
                                        hotelsVehicule.add(reservationCourante.getHotel().getIdHotel());
                                    }
                                    hotelsParVehicule.put(vehiculeChoisi.getIdVehicule(), hotelsVehicule);
                                }

                                synchronizeTrajetHotelsForReservation(
                                        trajet,
                                        vehiculeChoisi.getIdVehicule(),
                                        reservationCourante,
                                        hotelsParVehicule,
                                        connection,
                                        assignationsParVehicule
                                );

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
                                assignationsParVehicule
                                        .computeIfAbsent(vehiculeChoisi.getIdVehicule(), key -> new ArrayList<>())
                                        .add(assignation);

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
                                    while (capaciteRestante > 0) {
                                        Reservation bestFiller = null;
                                        int bestDelta = Integer.MAX_VALUE;
                                        int bestPax = 0;
                                        int bestIdx = -1;
                                        // Chercher la réservation non assignée la plus proche de la capacité restante
                                        for (int idxFiller = idxRes + 1; idxFiller < reservationsATraiter.size(); idxFiller++) {
                                            Reservation reservationFiller = reservationsATraiter.get(idxFiller);
                                            if (reservationFiller == null || reservationFiller.getIdReservation() <= 0) {
                                                continue;
                                            }
                                            if (reservationsTraitees.contains(reservationFiller.getIdReservation())) {
                                                continue;
                                            }
                                            if (reservationFiller.getHotel() == null) {
                                                continue;
                                            }
                                            int paxFiller = reservationFiller.getNbPassager();
                                            int delta = Math.abs(capaciteRestante - paxFiller);
                                            if (paxFiller <= capaciteRestante) {
                                                if (delta < bestDelta) {
                                                    bestFiller = reservationFiller;
                                                    bestDelta = delta;
                                                    bestPax = paxFiller;
                                                    bestIdx = idxFiller;
                                                } else if (delta == bestDelta && bestFiller == null) {
                                                    // En cas d'égalité, prendre la première dans l'ordre descendant
                                                    bestFiller = reservationFiller;
                                                    bestPax = paxFiller;
                                                    bestIdx = idxFiller;
                                                }
                                            }
                                        }
                                        // Si aucune réservation ne rentre entièrement, prendre la plus proche en split partiel
                                        if (bestFiller == null) {
                                            for (int idxFiller = idxRes + 1; idxFiller < reservationsATraiter.size(); idxFiller++) {
                                                Reservation reservationFiller = reservationsATraiter.get(idxFiller);
                                                if (reservationFiller == null || reservationFiller.getIdReservation() <= 0) {
                                                    continue;
                                                }
                                                if (reservationsTraitees.contains(reservationFiller.getIdReservation())) {
                                                    continue;
                                                }
                                                if (reservationFiller.getHotel() == null) {
                                                    continue;
                                                }
                                                int paxFiller = reservationFiller.getNbPassager();
                                                int delta = Math.abs(capaciteRestante - paxFiller);
                                                if (paxFiller > capaciteRestante && delta < bestDelta) {
                                                    bestFiller = reservationFiller;
                                                    bestDelta = delta;
                                                    bestPax = capaciteRestante;
                                                    bestIdx = idxFiller;
                                                } else if (paxFiller > capaciteRestante && delta == bestDelta && bestFiller == null) {
                                                    bestFiller = reservationFiller;
                                                    bestPax = capaciteRestante;
                                                    bestIdx = idxFiller;
                                                }
                                            }
                                        }
                                        if (bestFiller == null) {
                                            break;
                                        }
                                        // Assigner la réservation bestFiller (totalement ou partiellement)
                                        synchronizeTrajetHotelsForReservation(
                                                trajet,
                                                vehiculeChoisi.getIdVehicule(),
                                                bestFiller,
                                                hotelsParVehicule,
                                                connection,
                                                assignationsParVehicule
                                        );

                                        Assignation assignationFiller = new Assignation();
                                        assignationFiller.setIdReservation(bestFiller.getIdReservation());
                                        assignationFiller.setIdVehicule(vehiculeChoisi.getIdVehicule());
                                        assignationFiller.setIdTrajet(trajet.getIdTrajet());
                                        assignationFiller.setDateHeureDepart(dateDepartTrajet);
                                        assignationFiller.setDateHeureRetour(trajet.getDateHeureRetour());
                                        assignationFiller.setQuantitePassagersAssignes(bestPax);
                                        assignationFiller.setReservation(bestFiller);
                                        assignationFiller.setVehicule(vehiculeChoisi);

                                        assignationRepository.insertAssignation(assignationFiller, connection);
                                        assignations.add(assignationFiller);
                                        assignationsParVehicule
                                                .computeIfAbsent(vehiculeChoisi.getIdVehicule(), key -> new ArrayList<>())
                                                .add(assignationFiller);

                                        int utilisationMaj = capaciteUtiliseeParVehicule.getOrDefault(
                                                vehiculeChoisi.getIdVehicule(), 0) + bestPax;
                                        capaciteUtiliseeParVehicule.put(vehiculeChoisi.getIdVehicule(), utilisationMaj);

                                        capaciteRestante -= bestPax;

                                        if (bestPax == bestFiller.getNbPassager()) {
                                            reservationsTraitees.add(bestFiller.getIdReservation());
                                        } else {
                                            // Reporter le reste de la réservation non assignée
                                            int reste = bestFiller.getNbPassager() - bestPax;
                                            reportsDuGroupe.add(copierReservationAvecReste(bestFiller, reste));
                                            reservationsTraitees.add(bestFiller.getIdReservation());
                                        }
                                    }
                                }
                            } catch (SQLException sqlEx) {
                                System.err.println("ERROR: Processing reservation " + reservationCourante.getIdReservation() + ": " + sqlEx.getMessage());
                                reportsDuGroupe.add(copierReservationAvecReste(reservationCourante, passagersRestants));
                                dejaReportee = true;
                                break;
                            }
                        }

                        if (passagersRestants > 0 && !dejaReportee) {
                            reportsDuGroupe.add(copierReservationAvecReste(reservationCourante, passagersRestants));
                        }
                    }

                    if (!trajetsDuGroupe.isEmpty()) {
                        LocalDateTime departOperationnel = resolveDepartOperationnelGroupe(
                                dateDepartGroupe,
                                reservationsTraitees,
                                reservationsParIdDansGroupe,
                                trajetsDuGroupe
                        );
                        if (departOperationnel != null) {
                            try {
                                alignerDepartGroupeEtPersister(
                                        departOperationnel,
                                        trajetsDuGroupe,
                                        assignationsParVehicule,
                                        connection
                                );
                            } catch (SQLException sqlEx) {
                                System.err.println("WARN: alignerDepartGroupeEtPersister failed for groupe " + (idxGroupe + 1) + ": " + sqlEx.getMessage());
                            }
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

    private void synchronizeTrajetHotelsForReservation(
            Trajet trajet,
            int idVehicule,
            Reservation reservation,
            java.util.Map<Integer, java.util.LinkedHashSet<Integer>> hotelsParVehicule,
            Connection connection,
            java.util.Map<Integer, List<Assignation>> assignationsParVehicule) throws SQLException {

        if (trajet == null || reservation == null || reservation.getHotel() == null) {
            return;
        }

        java.util.LinkedHashSet<Integer> hotels = hotelsParVehicule.computeIfAbsent(idVehicule, key -> new java.util.LinkedHashSet<>());
        boolean added = hotels.add(reservation.getHotel().getIdHotel());
        if (!added) {
            return;
        }

        List<Integer> hotelsRoute = new ArrayList<>(hotels);
        List<TrajetEtape> etapesMaj = routeService.calculerRoute(hotelsRoute);
        DureeResult dureeMaj = dureeService.calculerDureeMultiStop(trajet.getDateHeureDepart(), etapesMaj);

        trajet.setDateHeureRetour(dureeMaj.getDateRetour());
        trajet.setEtapes(new ArrayList<>());

        updateTrajetTimes(trajet, connection);
        replaceTrajetEtapes(trajet.getIdTrajet(), etapesMaj, connection);

        for (TrajetEtape etape : etapesMaj) {
            etape.setIdTrajet(trajet.getIdTrajet());
            trajet.getEtapes().add(etape);
        }

        List<Assignation> assignationsVehicule = assignationsParVehicule.get(idVehicule);
        if (assignationsVehicule != null) {
            for (Assignation assignation : assignationsVehicule) {
                if (assignation != null && assignation.getIdTrajet() != null && assignation.getIdTrajet() == trajet.getIdTrajet()) {
                    assignation.setDateHeureDepart(trajet.getDateHeureDepart());
                    assignation.setDateHeureRetour(trajet.getDateHeureRetour());
                }
            }
        }
        updateAssignationsTimesByTrajet(trajet.getIdTrajet(), trajet.getDateHeureDepart(), trajet.getDateHeureRetour(), connection);
    }

    private LocalDateTime resolveDepartOperationnelGroupe(
            LocalDateTime departParDefaut,
            java.util.Set<Integer> reservationsTraitees,
            java.util.Map<Integer, Reservation> reservationsParId,
            List<Trajet> trajetsDuGroupe) {

        LocalDateTime maxReservationAssignee = null;
        for (Integer idReservation : reservationsTraitees) {
            Reservation reservation = reservationsParId.get(idReservation);
            if (reservation == null || reservation.getDateHeureArrive() == null) {
                continue;
            }
            LocalDateTime arrivee = reservation.getDateHeureArrive().truncatedTo(ChronoUnit.MINUTES);
            if (maxReservationAssignee == null || arrivee.isAfter(maxReservationAssignee)) {
                maxReservationAssignee = arrivee;
            }
        }

        LocalDateTime maxDepartTrajet = null;
        for (Trajet trajet : trajetsDuGroupe) {
            if (trajet == null || trajet.getDateHeureDepart() == null) {
                continue;
            }
            LocalDateTime depart = trajet.getDateHeureDepart().truncatedTo(ChronoUnit.MINUTES);
            if (maxDepartTrajet == null || depart.isAfter(maxDepartTrajet)) {
                maxDepartTrajet = depart;
            }
        }

        LocalDateTime departOperationnel = departParDefaut;
        if (maxReservationAssignee != null && (departOperationnel == null || maxReservationAssignee.isAfter(departOperationnel))) {
            departOperationnel = maxReservationAssignee;
        }
        if (maxDepartTrajet != null && (departOperationnel == null || maxDepartTrajet.isAfter(departOperationnel))) {
            departOperationnel = maxDepartTrajet;
        }

        return departOperationnel;
    }

    private void alignerDepartGroupeEtPersister(
            LocalDateTime departOperationnel,
            List<Trajet> trajetsDuGroupe,
            java.util.Map<Integer, List<Assignation>> assignationsParVehicule,
            Connection connection) throws SQLException {

        if (departOperationnel == null || trajetsDuGroupe == null || trajetsDuGroupe.isEmpty()) {
            return;
        }

        for (Trajet trajet : trajetsDuGroupe) {
            if (trajet == null || trajet.getIdTrajet() <= 0) {
                continue;
            }
            if (trajet.getDateHeureDepart() == null || trajet.getDateHeureRetour() == null) {
                continue;
            }

            long dureeMinutes = ChronoUnit.MINUTES.between(trajet.getDateHeureDepart(), trajet.getDateHeureRetour());
            if (dureeMinutes < 0) {
                dureeMinutes = 0;
            }

            LocalDateTime nouveauDepart = departOperationnel;
            LocalDateTime nouveauRetour = departOperationnel.plusMinutes(dureeMinutes);
            
            trajet.setDateHeureDepart(nouveauDepart);
            trajet.setDateHeureRetour(nouveauRetour);
            
            try {
                updateTrajetTimes(trajet, connection);
            } catch (SQLException updateEx) {
                System.err.println("WARN: updateTrajetTimes failed for trajet " + trajet.getIdTrajet() + ": " + updateEx.getMessage());
            }

            List<Assignation> assignationsVehicule = assignationsParVehicule.get(trajet.getIdVehicule());
            if (assignationsVehicule != null) {
                for (Assignation assignation : assignationsVehicule) {
                    if (assignation != null && assignation.getIdTrajet() != null && assignation.getIdTrajet() == trajet.getIdTrajet()) {
                        assignation.setDateHeureDepart(nouveauDepart);
                        assignation.setDateHeureRetour(nouveauRetour);
                    }
                }
            }

            try {
                updateAssignationsTimesByTrajet(trajet.getIdTrajet(), nouveauDepart, nouveauRetour, connection);
            } catch (SQLException updateEx) {
                System.err.println("WARN: updateAssignationsTimesByTrajet failed for trajet " + trajet.getIdTrajet() + ": " + updateEx.getMessage());
            }
        }
    }

    private void updateTrajetTimes(Trajet trajet, Connection connection) throws SQLException {
        if (trajet == null || trajet.getIdTrajet() <= 0) {
            return;
        }
        if (trajet.getDateHeureDepart() == null || trajet.getDateHeureRetour() == null) {
            return;
        }
        
        String schemaName = connection.getSchema();
        if (schemaName == null || schemaName.isEmpty()) {
            schemaName = "dev";
        }
        
        String sql = "UPDATE " + schemaName + ".Trajet SET date_heure_depart = ?, date_heure_retour = ? WHERE Id_Trajet = ?";
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(trajet.getDateHeureDepart()));
            statement.setTimestamp(2, java.sql.Timestamp.valueOf(trajet.getDateHeureRetour()));
            statement.setInt(3, trajet.getIdTrajet());
            statement.executeUpdate();
        }
    }

    private void updateAssignationsTimesByTrajet(int idTrajet, LocalDateTime depart, LocalDateTime retour, Connection connection)
            throws SQLException {
        if (idTrajet <= 0 || depart == null || retour == null) {
            return;
        }
        
        String schemaName = connection.getSchema();
        if (schemaName == null || schemaName.isEmpty()) {
            schemaName = "dev";
        }
        
        String sql = "UPDATE " + schemaName + ".Assignation SET date_heure_depart = ?, date_heure_retour = ? WHERE Id_Trajet = ?";
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(depart));
            statement.setTimestamp(2, java.sql.Timestamp.valueOf(retour));
            statement.setInt(3, idTrajet);
            statement.executeUpdate();
        }
    }

    private void replaceTrajetEtapes(int idTrajet, List<TrajetEtape> etapes, Connection connection) throws SQLException {
        if (idTrajet <= 0) {
            return;
        }
        
        String schemaName = connection.getSchema();
        if (schemaName == null || schemaName.isEmpty()) {
            schemaName = "dev";
        }
        
        try (java.sql.PreparedStatement delete = connection.prepareStatement("DELETE FROM " + schemaName + ".TrajetEtape WHERE Id_Trajet = ?")) {
            delete.setInt(1, idTrajet);
            delete.executeUpdate();
        }

        if (etapes == null) {
            return;
        }

        for (TrajetEtape etape : etapes) {
            etape.setIdTrajet(idTrajet);
            trajetRepository.insertTrajetEtape(etape, connection);
        }
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