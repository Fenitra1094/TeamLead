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
import com.cousin.util.RetourVehiculeResult;

public class AssignationService {
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

                    // Sprint 8 : file prioritaire des non assignes precedents.
                    List<Reservation> nonAssigneesPrecedentes = new ArrayList<>(reservationsReportees);
                    List<Reservation> reservationsNouvelles = new ArrayList<>(groupe.getReservations());
                    reservationsReportees.clear();

                    nonAssigneesPrecedentes.sort((r1, r2) -> Integer.compare(r2.getNbPassager(), r1.getNbPassager()));
                    reservationsNouvelles.sort((r1, r2) -> Integer.compare(r2.getNbPassager(), r1.getNbPassager()));

                    List<Reservation> reservationsATraiter = new ArrayList<>(nonAssigneesPrecedentes);
                    reservationsATraiter.addAll(reservationsNouvelles);

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

                                if (vehiculeChoisi.getDernierRetour() != null) {
                                    RetourVehiculeResult retourVehiculeResult = traiterVehiculeRevenu(
                                            vehiculeChoisi,
                                            vehiculeChoisi.getDernierRetour(),
                                            extraireReservationsRestantes(nonAssigneesPrecedentes, passagersRestantsParReservation),
                                            extraireReservationsRestantes(reservationsNouvelles, passagersRestantsParReservation),
                                            tempsAttenteMinutes,
                                            connection
                                    );

                                    if (retourVehiculeResult != null
                                            && retourVehiculeResult.getDateDepartEffective() != null
                                            && retourVehiculeResult.getDateDepartEffective().isAfter(dateDepartTrajet)) {
                                        dateDepartTrajet = retourVehiculeResult.getDateDepartEffective();
                                    }
                                }

                                // Garde-fou: jamais avant l'heure de disponibilite declaree du vehicule.
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

                                    Assignation assignationFiller = new Assignation();
                                    assignationFiller.setIdReservation(idReservationFiller);
                                    assignationFiller.setIdVehicule(vehiculeChoisi.getIdVehicule());
                                    assignationFiller.setIdTrajet(trajet.getIdTrajet());
                                    assignationFiller.setDateHeureDepart(dateDepartTrajet);
                                    assignationFiller.setDateHeureRetour(trajet.getDateHeureRetour());
                                    assignationFiller.setQuantitePassagersAssignes(personnesFillerAEmbarquer);
                                    assignationFiller.setReservation(reservationFiller);
                                    assignationFiller.setVehicule(vehiculeChoisi);

                                    assignationRepository.insertAssignation(assignationFiller, connection);
                                    assignations.add(assignationFiller);
                                    assignationsParVehicule
                                            .computeIfAbsent(vehiculeChoisi.getIdVehicule(), key -> new ArrayList<>())
                                            .add(assignationFiller);

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

    // Sprint 8 : logique dediee retour vehicule + priorite non assignes precedents.
    private RetourVehiculeResult traiterVehiculeRevenu(
            Vehicule vehicule,
            LocalDateTime heureRetourVehicule,
            List<Reservation> nonAssigneesPrecedentes,
            List<Reservation> reservationsNouvelles,
            int tempsAttenteMinutes,
            @SuppressWarnings("unused") Connection conn) {

        if (vehicule == null || heureRetourVehicule == null) {
            return RetourVehiculeResult.empty();
        }

        int capaciteVehicule = Math.max(0, vehicule.getNbPlace());
        if (capaciteVehicule == 0) {
            return RetourVehiculeResult.empty();
        }

        int totalPrecedents = sommePassagers(nonAssigneesPrecedentes);
        if (totalPrecedents <= 0) {
            return RetourVehiculeResult.empty();
        }

        if (totalPrecedents >= capaciteVehicule) {
            RetourVehiculeResult result = new RetourVehiculeResult();
            result.setModeDepart("IMMEDIAT");
            result.setDateDepartEffective(heureRetourVehicule);
            result.setTotalEmbarquesPrecedents(capaciteVehicule);
            result.setTotalRestantsPrecedents(totalPrecedents - capaciteVehicule);
            return result;
        }

        LocalDateTime finFenetre = heureRetourVehicule.plusMinutes(Math.max(0, tempsAttenteMinutes));
        int placesRestantes = capaciteVehicule - totalPrecedents;

        List<Reservation> reservationsDansFenetre = new ArrayList<>();
        if (reservationsNouvelles != null) {
            for (Reservation reservation : reservationsNouvelles) {
                if (reservation == null || reservation.getDateHeureArrive() == null) {
                    continue;
                }
                if (!reservation.getDateHeureArrive().isBefore(heureRetourVehicule)
                        && !reservation.getDateHeureArrive().isAfter(finFenetre)) {
                    reservationsDansFenetre.add(reservation);
                }
            }
        }

        reservationsDansFenetre.sort((r1, r2) -> {
            if (r1.getDateHeureArrive() == null && r2.getDateHeureArrive() == null) {
                return 0;
            }
            if (r1.getDateHeureArrive() == null) {
                return 1;
            }
            if (r2.getDateHeureArrive() == null) {
                return -1;
            }
            return r1.getDateHeureArrive().compareTo(r2.getDateHeureArrive());
        });

        int cumulNouveaux = 0;
        LocalDateTime instantRemplissage = null;
        for (Reservation reservation : reservationsDansFenetre) {
            cumulNouveaux += Math.max(0, reservation.getNbPassager());
            if (cumulNouveaux >= placesRestantes) {
                instantRemplissage = reservation.getDateHeureArrive();
                break;
            }
        }

        RetourVehiculeResult result = new RetourVehiculeResult();
        result.setFenetreDebut(heureRetourVehicule);
        result.setFenetreFin(finFenetre);
        result.setTotalEmbarquesPrecedents(totalPrecedents);

        if (instantRemplissage != null) {
            result.setModeDepart("IMMEDIAT");
            result.setDateDepartEffective(instantRemplissage);
            result.setTotalEmbarquesNouveaux(Math.max(0, placesRestantes));
            result.setTotalRestantsPrecedents(0);
            result.setTotalRestantsNouveaux(Math.max(0, cumulNouveaux - placesRestantes));
        } else {
            result.setModeDepart("ATTENTE");
            result.setDateDepartEffective(finFenetre);
            result.setTotalEmbarquesNouveaux(Math.max(0, cumulNouveaux));
            result.setTotalRestantsPrecedents(0);
            result.setTotalRestantsNouveaux(Math.max(0, placesRestantes - cumulNouveaux));
        }

        return result;
    }

    private int sommePassagers(List<Reservation> reservations) {
        int total = 0;
        if (reservations == null) {
            return total;
        }

        for (Reservation reservation : reservations) {
            if (reservation == null) {
                continue;
            }
            total += Math.max(0, reservation.getNbPassager());
        }
        return total;
    }

    private List<Reservation> extraireReservationsRestantes(
            List<Reservation> source,
            java.util.Map<Integer, Integer> passagersRestantsParReservation) {

        List<Reservation> restants = new ArrayList<>();
        if (source == null) {
            return restants;
        }

        for (Reservation reservation : source) {
            if (reservation == null || reservation.getIdReservation() <= 0) {
                continue;
            }

            int restantsPassagers = passagersRestantsParReservation.getOrDefault(
                    reservation.getIdReservation(),
                    Math.max(0, reservation.getNbPassager())
            );
            if (restantsPassagers <= 0) {
                continue;
            }

            restants.add(copierReservationAvecReste(reservation, restantsPassagers));
        }

        return restants;
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