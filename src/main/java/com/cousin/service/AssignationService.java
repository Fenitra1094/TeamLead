package com.cousin.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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

public class AssignationService {
    private static final java.util.Comparator<Reservation> RESERVATION_PASSAGERS_DESC =
            (r1, r2) -> Integer.compare(
                    r2 != null ? r2.getNbPassager() : 0,
                    r1 != null ? r1.getNbPassager() : 0
            );

    private final AssignationRepository assignationRepository;
    private final ReservationRepository reservationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetRepository trajetRepository;
    private final GroupingService groupingService;
    private final RouteService routeService;
    private final DureeService dureeService;

    public AssignationService() {
        this.assignationRepository = new AssignationRepository();
        this.reservationRepository = new ReservationRepository();
        this.vehiculeRepository = new VehiculeRepository();
        this.trajetRepository = new TrajetRepository();
        this.groupingService = new GroupingService();
        this.routeService = new RouteService();
        this.dureeService = new DureeService();
    }

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
                    if (!reservationsReportees.isEmpty()) {
                        LocalDateTime borneInferieure = null;
                        if (idxGroupe > 0) {
                            GroupeTemps groupePrecedent = groupes.get(idxGroupe - 1);
                            LocalDateTime debutFenetrePrecedente = resolveDebutFenetreGroupe(groupePrecedent);
                            if (debutFenetrePrecedente != null) {
                                int attentePrecedente = groupePrecedent != null
                                        ? groupePrecedent.getTempsAttenteMinutes()
                                        : tempsAttenteMinutes;
                                borneInferieure = debutFenetrePrecedente.plusMinutes(attentePrecedente);
                            }
                        }

                        GroupeTemps groupeCourant = groupes.get(idxGroupe);
                        LocalDateTime borneSuperieure = resolveDebutFenetreGroupe(groupeCourant);
                        Trajet trajetPivotRetour = trouverTrajetPivotRetour(
                                trajets,
                                borneInferieure,
                                borneSuperieure,
                                reservationsReportees
                        );

                        if (trajetPivotRetour != null && trajetPivotRetour.getDateHeureRetour() != null) {
                            int totalReportes = sumPassagersReservations(reservationsReportees);
                            int capacitePivot = trajetPivotRetour.getVehicule() != null
                                    ? Math.max(0, trajetPivotRetour.getVehicule().getNbPlace())
                                    : 0;

                            if (totalReportes > 0 && capacitePivot > 0 && totalReportes < capacitePivot) {
                                GroupeTemps groupePivotRetour = construireGroupePivotRetour(
                                        trajetPivotRetour,
                                        tempsAttenteMinutes,
                                        groupeCourant
                                );
                                if (groupePivotRetour != null && groupePivotRetour.getReservations() != null
                                        && !groupePivotRetour.getReservations().isEmpty()) {
                                    groupes.add(idxGroupe, groupePivotRetour);
                                    idxGroupe--;
                                    continue;
                                }
                            }
                        }
                    }

                    GroupeTemps groupe = groupes.get(idxGroupe);
                    if ((groupe == null || groupe.getReservations() == null || groupe.getReservations().isEmpty())
                            && reservationsReportees.isEmpty()) {
                        groupes.remove(idxGroupe);
                        idxGroupe--;
                        continue;
                    }
                    if (groupe == null) {
                        continue;
                    }

                    boolean isLastGroupe = (idxGroupe == groupes.size() - 1);

                    Integer idPivot = groupe.getIdVehiculePivot();
                    final int idVehiculePivotPrioritaire = idPivot != null ? idPivot : 0;
                    boolean prioritePivotEnAttente = groupe.isGroupeCreeParRetourVehicule()
                            && idVehiculePivotPrioritaire > 0;

                    java.util.Set<Integer> reportsEnEntreeIds = new java.util.HashSet<>();
                    for (Reservation reservationReportee : reservationsReportees) {
                        if (reservationReportee != null && reservationReportee.getIdReservation() > 0) {
                            reportsEnEntreeIds.add(reservationReportee.getIdReservation());
                        }
                    }

                    List<Reservation> reservationsATraiter = new ArrayList<>();
                    List<Reservation> reportsEnEntree = new ArrayList<>(reservationsReportees);
                    if (groupe.isGroupeCreeParRetourVehicule()) {
                        reportsEnEntree.sort(RESERVATION_PASSAGERS_DESC);
                        reservationsATraiter.addAll(reportsEnEntree);

                        if (groupe.getReservations() != null) {
                            java.util.Set<Integer> idsReports = new java.util.HashSet<>(reportsEnEntreeIds);
                            List<Reservation> natives = new ArrayList<>();
                            for (Reservation reservationNative : groupe.getReservations()) {
                                if (reservationNative == null) {
                                    continue;
                                }
                                if (reservationNative.getIdReservation() > 0
                                        && idsReports.contains(reservationNative.getIdReservation())) {
                                    continue;
                                }
                                natives.add(reservationNative);
                            }
                            natives.sort(RESERVATION_PASSAGERS_DESC);
                            reservationsATraiter.addAll(natives);
                        }
                    } else {
                        if (groupe.getReservations() != null) {
                            reservationsATraiter.addAll(groupe.getReservations());
                        }
                        reservationsATraiter.addAll(reportsEnEntree);
                    }
                    reservationsReportees.clear();

                    reservationsATraiter.sort((r1, r2) -> {
                        boolean r1Reportee = r1 != null && reportsEnEntreeIds.contains(r1.getIdReservation());
                        boolean r2Reportee = r2 != null && reportsEnEntreeIds.contains(r2.getIdReservation());
                        if (r1Reportee != r2Reportee) {
                            return r1Reportee ? -1 : 1;
                        }
                        int p1 = r1 != null ? r1.getNbPassager() : 0;
                        int p2 = r2 != null ? r2.getNbPassager() : 0;
                        return Integer.compare(p2, p1);
                    });

                    LocalDateTime dateDepartGroupe = groupe.getHeureDepartGroupe();
                    LocalDateTime finGroupe = dateDepartGroupe.plusMinutes(tempsAttenteMinutes);

                    java.util.Map<Integer, Integer> capaciteUtiliseeParVehicule = new java.util.HashMap<>();
                    java.util.Map<Integer, Trajet> trajetParVehicule = new java.util.HashMap<>();
                    java.util.Map<Integer, java.util.LinkedHashSet<Integer>> hotelsParVehicule = new java.util.HashMap<>();
                    java.util.Map<Integer, List<Assignation>> assignationsParVehicule = new java.util.HashMap<>();
                    java.util.Set<Integer> reservationsTraitees = new java.util.HashSet<>();
                    java.util.Set<Integer> reservationsAvecAssignationDansGroupe = new java.util.HashSet<>();
                    List<Reservation> reportsDuGroupe = new ArrayList<>();
                    List<Trajet> trajetsDuGroupe = new ArrayList<>();

                    java.util.Map<Integer, Reservation> reservationsParIdDansGroupe = new java.util.LinkedHashMap<>();
                    java.util.Map<Integer, Integer> passagersRestantsParReservation = new java.util.HashMap<>();
                    for (Reservation reservation : reservationsATraiter) {
                        if (reservation != null && reservation.getIdReservation() > 0) {
                            reservationsParIdDansGroupe.putIfAbsent(reservation.getIdReservation(), reservation);
                            passagersRestantsParReservation.put(
                                    reservation.getIdReservation(),
                                    Math.max(0, reservation.getNbPassager())
                            );
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

                        int idReservationCourante = reservationCourante.getIdReservation();
                        int passagersRestants = passagersRestantsParReservation.getOrDefault(
                                idReservationCourante,
                                Math.max(0, reservationCourante.getNbPassager())
                        );
                        if (passagersRestants <= 0) {
                            reservationsTraitees.add(idReservationCourante);
                            continue;
                        }
                        boolean dejaReportee = false;
                        java.util.Set<Integer> vehiculesExclus = new java.util.HashSet<>();

                        while (passagersRestants > 0) {
                            try {
                                List<Vehicule> candidats = vehiculeRepository.getVehiculesCandidatsPourSplit(
                                        passagersRestants, date, dateDepartGroupe, finGroupe, connection);

                                Vehicule vehiculeChoisi = null;
                                if (prioritePivotEnAttente) {
                                    vehiculeChoisi = selectionnerVehiculePivotPrioritaire(
                                            candidats,
                                            idVehiculePivotPrioritaire,
                                            capaciteUtiliseeParVehicule,
                                            vehiculesExclus,
                                            trajets,
                                            dateDepartGroupe,
                                            finGroupe
                                    );
                                }
                                if (vehiculeChoisi == null) {
                                    vehiculeChoisi = selectionnerVehiculePlusProche(
                                            candidats, passagersRestants, capaciteUtiliseeParVehicule, vehiculesExclus);
                                }

                                if (vehiculeChoisi == null) {
                                    reportsDuGroupe.add(copierReservationAvecReste(reservationCourante, passagersRestants));
                                    dejaReportee = true;
                                    break;
                                }
                                if (prioritePivotEnAttente && vehiculeChoisi.getIdVehicule() == idVehiculePivotPrioritaire) {
                                    prioritePivotEnAttente = false;
                                }

                                int capaciteDisponible = getCapaciteDisponible(vehiculeChoisi, capaciteUtiliseeParVehicule);
                                if (capaciteDisponible <= 0) {
                                    vehiculesExclus.add(vehiculeChoisi.getIdVehicule());
                                    continue;
                                }

                                Trajet trajet = trajetParVehicule.get(vehiculeChoisi.getIdVehicule());
                                LocalDateTime dateDepartTrajet = dateDepartGroupe;
                                if (vehiculeChoisi.getDernierRetour() != null
                                        && vehiculeChoisi.getDernierRetour().isAfter(dateDepartTrajet)) {
                                    dateDepartTrajet = vehiculeChoisi.getDernierRetour();
                                }

                                LocalTime heureDisponibiliteVehicule = vehiculeChoisi.getHeureDisponibilite() != null
                                        ? vehiculeChoisi.getHeureDisponibilite()
                                        : LocalTime.MIDNIGHT;
                                LocalDateTime disponibiliteDuJour = LocalDateTime.of(date, heureDisponibiliteVehicule);
                                if (disponibiliteDuJour.isAfter(dateDepartTrajet)) {
                                    dateDepartTrajet = disponibiliteDuJour;
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

                                insererAssignation(
                                        reservationCourante,
                                        vehiculeChoisi,
                                        trajet,
                                        dateDepartTrajet,
                                        reportsEnEntreeIds.contains(reservationCourante.getIdReservation()),
                                        personnesAEmbarquer,
                                        connection,
                                        assignations,
                                        assignationsParVehicule,
                                        reservationsAvecAssignationDansGroupe
                                );

                                int nouvelleUtilisation = capaciteUtiliseeParVehicule
                                        .getOrDefault(vehiculeChoisi.getIdVehicule(), 0)
                                        + personnesAEmbarquer;
                                capaciteUtiliseeParVehicule.put(vehiculeChoisi.getIdVehicule(), nouvelleUtilisation);

                                passagersRestants -= personnesAEmbarquer;
                                passagersRestantsParReservation.put(idReservationCourante, Math.max(0, passagersRestants));

                                if (passagersRestants == 0) {
                                    reservationsTraitees.add(idReservationCourante);
                                }

                                int capaciteRestante = getCapaciteDisponible(vehiculeChoisi, capaciteUtiliseeParVehicule);
                                while (capaciteRestante > 0) {
                                    Reservation reservationFiller = choisirReservationFillerPlusProche(
                                            reservationsATraiter,
                                            idxRes + 1,
                                            reservationsTraitees,
                                            passagersRestantsParReservation,
                                            capaciteRestante
                                    );

                                    if (reservationFiller == null) {
                                        break;
                                    }

                                    int idReservationFiller = reservationFiller.getIdReservation();
                                    int passagersFillerRestants = passagersRestantsParReservation.getOrDefault(
                                            idReservationFiller,
                                            Math.max(0, reservationFiller.getNbPassager())
                                    );

                                    if (passagersFillerRestants <= 0) {
                                        reservationsTraitees.add(idReservationFiller);
                                        continue;
                                    }

                                    synchronizeTrajetHotelsForReservation(
                                            trajet,
                                            vehiculeChoisi.getIdVehicule(),
                                            reservationFiller,
                                            hotelsParVehicule,
                                            connection,
                                            assignationsParVehicule
                                    );

                                    int personnesFillerAEmbarquer = Math.min(passagersFillerRestants, capaciteRestante);

                                    insererAssignation(
                                            reservationFiller,
                                            vehiculeChoisi,
                                            trajet,
                                            dateDepartTrajet,
                                            reportsEnEntreeIds.contains(reservationFiller.getIdReservation()),
                                            personnesFillerAEmbarquer,
                                            connection,
                                            assignations,
                                            assignationsParVehicule,
                                            reservationsAvecAssignationDansGroupe
                                    );

                                    int utilisationMaj = capaciteUtiliseeParVehicule.getOrDefault(
                                            vehiculeChoisi.getIdVehicule(), 0) + personnesFillerAEmbarquer;
                                    capaciteUtiliseeParVehicule.put(vehiculeChoisi.getIdVehicule(), utilisationMaj);

                                    int nouveauResteFiller = passagersFillerRestants - personnesFillerAEmbarquer;
                                    passagersRestantsParReservation.put(idReservationFiller, Math.max(0, nouveauResteFiller));
                                    if (nouveauResteFiller == 0) {
                                        reservationsTraitees.add(idReservationFiller);
                                    }

                                    capaciteRestante -= personnesFillerAEmbarquer;
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
                                reservationsAvecAssignationDansGroupe,
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

    private void insererAssignation(
            Reservation reservation,
            Vehicule vehicule,
            Trajet trajet,
            LocalDateTime dateDepart,
            boolean fromNonAssigneePrecedent,
            int quantitePassagers,
            Connection connection,
            List<Assignation> assignations,
            java.util.Map<Integer, List<Assignation>> assignationsParVehicule,
            java.util.Set<Integer> reservationsAvecAssignationDansGroupe) throws SQLException {

        Assignation assignation = new Assignation();
        assignation.setIdReservation(reservation.getIdReservation());
        assignation.setIdVehicule(vehicule.getIdVehicule());
        assignation.setIdTrajet(trajet.getIdTrajet());
        assignation.setDateHeureDepart(dateDepart);
        assignation.setDateHeureRetour(trajet.getDateHeureRetour());
        assignation.setQuantitePassagersAssignes(quantitePassagers);
        assignation.setFromNonAssigneePrecedent(fromNonAssigneePrecedent);
        assignation.setReservation(reservation);
        assignation.setVehicule(vehicule);

        assignationRepository.insertAssignation(assignation, connection);
        assignations.add(assignation);
        assignationsParVehicule
                .computeIfAbsent(vehicule.getIdVehicule(), key -> new ArrayList<>())
                .add(assignation);
        reservationsAvecAssignationDansGroupe.add(assignation.getIdReservation());
    }

    private Trajet trouverTrajetPivotRetour(
            List<Trajet> trajets,
            LocalDateTime borneInferieureExclusive,
            LocalDateTime borneSuperieureExclusive,
            List<Reservation> reservationsReportees) {

        List<Trajet> candidats = new ArrayList<>();
        LocalDateTime pivotRetour = null;

        for (Trajet trajet : trajets) {
            if (trajet == null || trajet.getDateHeureRetour() == null) {
                continue;
            }

            LocalDateTime retour = trajet.getDateHeureRetour().truncatedTo(ChronoUnit.MINUTES);
            if (borneInferieureExclusive != null && !retour.isAfter(borneInferieureExclusive)) {
                continue;
            }
            if (borneSuperieureExclusive != null && !retour.isBefore(borneSuperieureExclusive)) {
                continue;
            }

            if (pivotRetour == null || retour.isBefore(pivotRetour)) {
                candidats.clear();
                candidats.add(trajet);
                pivotRetour = retour;
            } else if (retour.isEqual(pivotRetour)) {
                candidats.add(trajet);
            }
        }

        if (candidats.isEmpty()) {
            return null;
        }
        if (candidats.size() == 1) {
            return candidats.get(0);
        }

        List<Vehicule> vehicules = new ArrayList<>();
        for (Trajet trajet : candidats) {
            if (trajet.getVehicule() != null) {
                vehicules.add(trajet.getVehicule());
            }
        }

        if (vehicules.isEmpty()) {
            return candidats.get(0);
        }

        int besoinReference = Math.max(1, maxPassagersReservations(reservationsReportees));
        Vehicule choisi = selectionnerVehiculePlusProche(
                vehicules,
                besoinReference,
                new java.util.HashMap<>(),
                new java.util.HashSet<>()
        );

        if (choisi == null) {
            return candidats.get(0);
        }

        for (Trajet trajet : candidats) {
            if (trajet != null && trajet.getIdVehicule() == choisi.getIdVehicule()) {
                return trajet;
            }
        }

        return candidats.get(0);
    }

    private GroupeTemps construireGroupePivotRetour(
            Trajet trajetPivotRetour,
            int tempsAttenteMinutes,
            GroupeTemps groupeCourantNaturel) {

        LocalDateTime pivotRetour = trajetPivotRetour.getDateHeureRetour().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime finFenetre = pivotRetour.plusMinutes(tempsAttenteMinutes);
        List<Reservation> reservationsFenetre = new ArrayList<>();

        if (groupeCourantNaturel == null || groupeCourantNaturel.getReservations() == null
                || groupeCourantNaturel.getReservations().isEmpty()) {
            return null;
        }

        List<Reservation> restantes = new ArrayList<>();
        for (Reservation reservation : groupeCourantNaturel.getReservations()) {
            if (reservation == null || reservation.getDateHeureArrive() == null) {
                continue;
            }

            LocalDateTime arrivee = reservation.getDateHeureArrive().truncatedTo(ChronoUnit.MINUTES);
            boolean dansFenetreRetour = !arrivee.isBefore(pivotRetour) && !arrivee.isAfter(finFenetre);
            if (dansFenetreRetour) {
                reservationsFenetre.add(reservation);
            } else {
                restantes.add(reservation);
            }
        }

        if (reservationsFenetre.isEmpty()) {
            return null;
        }

        groupeCourantNaturel.setReservations(restantes);
        reservationsFenetre.sort(RESERVATION_PASSAGERS_DESC);

        GroupeTemps groupePivotRetour = new GroupeTemps();
        groupePivotRetour.setHeureDepartGroupe(pivotRetour);
        groupePivotRetour.setTempsAttenteMinutes(tempsAttenteMinutes);
        groupePivotRetour.setReservations(reservationsFenetre);
        groupePivotRetour.setGroupeCreeParRetourVehicule(true);
        groupePivotRetour.setIdVehiculePivot(trajetPivotRetour.getIdVehicule());
        if (trajetPivotRetour.getVehicule() != null) {
            groupePivotRetour.setReferenceVehiculePivot(trajetPivotRetour.getVehicule().getReference());
        }
        return groupePivotRetour;
    }

    private Vehicule selectionnerVehiculePivotPrioritaire(
            List<Vehicule> candidats,
            int idVehiculePivot,
            java.util.Map<Integer, Integer> capaciteParVehicule,
            java.util.Set<Integer> vehiculesExclus,
            List<Trajet> trajets,
            LocalDateTime debutGroupe,
            LocalDateTime finGroupe) {

        if (idVehiculePivot <= 0) {
            return null;
        }

        if (vehiculesExclus != null && vehiculesExclus.contains(idVehiculePivot)) {
            return null;
        }

        if (candidats != null && !candidats.isEmpty()) {
            for (Vehicule vehicule : candidats) {
                if (vehicule == null || vehicule.getIdVehicule() != idVehiculePivot) {
                    continue;
                }
                if (getCapaciteDisponible(vehicule, capaciteParVehicule) > 0) {
                    return vehicule;
                }
                return null;
            }
        }

        Trajet trajetPivot = findTrajetPivotForVehiculeInWindow(trajets, idVehiculePivot, debutGroupe, finGroupe);
        if (trajetPivot == null || trajetPivot.getVehicule() == null) {
            return null;
        }

        Vehicule pivot = cloneVehiculeForSelection(trajetPivot.getVehicule());
        pivot.setDernierRetour(trajetPivot.getDateHeureRetour());
        if (getCapaciteDisponible(pivot, capaciteParVehicule) > 0) {
            return pivot;
        }

        return null;
    }

    private Trajet findTrajetPivotForVehiculeInWindow(
            List<Trajet> trajets,
            int idVehiculePivot,
            LocalDateTime debutGroupe,
            LocalDateTime finGroupe) {

        if (trajets == null || trajets.isEmpty() || idVehiculePivot <= 0) {
            return null;
        }

        Trajet best = null;
        for (Trajet trajet : trajets) {
            if (trajet == null || trajet.getIdVehicule() != idVehiculePivot || trajet.getDateHeureRetour() == null) {
                continue;
            }

            LocalDateTime retour = trajet.getDateHeureRetour().truncatedTo(ChronoUnit.MINUTES);
            if (debutGroupe != null && retour.isBefore(debutGroupe.truncatedTo(ChronoUnit.MINUTES))) {
                continue;
            }
            if (finGroupe != null && retour.isAfter(finGroupe.truncatedTo(ChronoUnit.MINUTES))) {
                continue;
            }

            if (best == null || retour.isBefore(best.getDateHeureRetour().truncatedTo(ChronoUnit.MINUTES))) {
                best = trajet;
            }
        }

        return best;
    }

    private Vehicule cloneVehiculeForSelection(Vehicule source) {
        if (source == null) {
            return null;
        }
        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(source.getIdVehicule());
        vehicule.setReference(source.getReference());
        vehicule.setNbPlace(source.getNbPlace());
        vehicule.setTypeVehicule(source.getTypeVehicule());
        vehicule.setHeureDisponibilite(source.getHeureDisponibilite());
        vehicule.setTrajetCount(source.getTrajetCount());
        vehicule.setDernierRetour(source.getDernierRetour());
        return vehicule;
    }

    private int sumPassagersReservations(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                continue;
            }
            total += Math.max(0, reservation.getNbPassager());
        }
        return total;
    }

    private int maxPassagersReservations(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return 0;
        }
        int max = 0;
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                continue;
            }
            max = Math.max(max, Math.max(0, reservation.getNbPassager()));
        }
        return max;
    }

    private LocalDateTime resolveDebutFenetreGroupe(GroupeTemps groupe) {
        if (groupe == null) {
            return null;
        }

        if (groupe.isGroupeCreeParRetourVehicule() && groupe.getHeureDepartGroupe() != null) {
            return groupe.getHeureDepartGroupe().truncatedTo(ChronoUnit.MINUTES);
        }

        LocalDateTime minArrivee = null;
        List<Reservation> reservations = groupe.getReservations();
        if (reservations != null) {
            for (Reservation reservation : reservations) {
                if (reservation == null || reservation.getDateHeureArrive() == null) {
                    continue;
                }
                LocalDateTime arrivee = reservation.getDateHeureArrive().truncatedTo(ChronoUnit.MINUTES);
                if (minArrivee == null || arrivee.isBefore(minArrivee)) {
                    minArrivee = arrivee;
                }
            }
        }

        if (minArrivee != null) {
            return minArrivee;
        }

        if (groupe.getHeureDepartGroupe() != null) {
            return groupe.getHeureDepartGroupe().truncatedTo(ChronoUnit.MINUTES);
        }

        return null;
    }

    private int getCapaciteDisponible(Vehicule vehicule, java.util.Map<Integer, Integer> capaciteParVehicule) {
        return vehicule.getNbPlace() - capaciteParVehicule.getOrDefault(vehicule.getIdVehicule(), 0);
    }

    private Reservation choisirReservationFillerPlusProche(
            List<Reservation> reservationsATraiter,
            int startIndex,
            java.util.Set<Integer> reservationsTraitees,
            java.util.Map<Integer, Integer> passagersRestantsParReservation,
            int capaciteRestante) {

        Reservation meilleur = null;
        int meilleurEcart = Integer.MAX_VALUE;
        int meilleurVolume = -1;

        for (int idx = Math.max(0, startIndex); idx < reservationsATraiter.size(); idx++) {
            Reservation candidate = reservationsATraiter.get(idx);
            if (candidate == null || candidate.getIdReservation() <= 0) {
                continue;
            }
            if (candidate.getHotel() == null) {
                continue;
            }
            if (reservationsTraitees.contains(candidate.getIdReservation())) {
                continue;
            }

            int passagersRestants = passagersRestantsParReservation.getOrDefault(
                    candidate.getIdReservation(),
                    Math.max(0, candidate.getNbPassager())
            );
            if (passagersRestants <= 0) {
                continue;
            }

            int ecart = Math.abs(passagersRestants - capaciteRestante);
            if (meilleur == null
                    || ecart < meilleurEcart
                    || (ecart == meilleurEcart && passagersRestants > meilleurVolume)
                    || (ecart == meilleurEcart
                    && passagersRestants == meilleurVolume
                    && candidate.getIdReservation() < meilleur.getIdReservation())) {
                meilleur = candidate;
                meilleurEcart = ecart;
                meilleurVolume = passagersRestants;
            }
        }

        return meilleur;
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
            java.util.Set<Integer> reservationsAvecAssignation,
            java.util.Map<Integer, Reservation> reservationsParId,
            List<Trajet> trajetsDuGroupe) {

        LocalDateTime maxReservationAssignee = null;
        for (Integer idReservation : reservationsAvecAssignation) {
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

        String schemaName = resolveSchemaName(connection);
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

        String schemaName = resolveSchemaName(connection);
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

        String schemaName = resolveSchemaName(connection);
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

    private String resolveSchemaName(Connection connection) throws SQLException {
        String schemaName = connection.getSchema();
        if (schemaName == null || schemaName.isEmpty()) {
            return "dev";
        }
        return schemaName;
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
