package com.cousin.controller;

import com.cousin.service.AssignationService;
import com.cousin.service.ParametreService;
import com.cousin.service.VehiculeService;
import com.cousin.model.Assignation;
import com.cousin.model.Reservation;
import com.cousin.model.Vehicule;
import com.cousin.util.AssignationResult;
import com.cousin.util.GroupeTemps;
import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Param;
import com.framework.model.ModelView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class AssignationController {
    private final AssignationService assignationService = new AssignationService();
    private final ParametreService parametreService = new ParametreService();
    private final VehiculeService vehiculeService = new VehiculeService();

    /**
     * Affichage du formulaire pour choisir une date.
     */
    @GetMapping("/assignation/form")
    public ModelView showForm() {
        return new ModelView("/WEB-INF/views/assignation.jsp");
    }

    /**
     * Lance l'assignation pour la date choisie.
     * /assignation/assigner?date=YYYY-MM-DD
     */
    @GetMapping("/assignation/assigner")
    public ModelView assigner(@Param("date") String dateStr) {
        ModelView mv = new ModelView("/WEB-INF/views/assignation_result.jsp");

        // Validation de la date
        if (dateStr == null || dateStr.isBlank()) {
            mv.addAttribute("error", "Veuillez choisir une date.");
            return mv;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            mv.addAttribute("error", "Format de date invalide. Utilisez YYYY-MM-DD.");
            return mv;
        }

        try {
            int tempsAttenteMinutes = parametreService.getTempsAttente();
            AssignationResult result = assignationService.assignerPourDate(date, tempsAttenteMinutes);

            List<Assignation> assignations = result.getAssignations() != null
                ? result.getAssignations()
                : new ArrayList<>();
            List<GroupeTemps> groupes = result.getGroupes() != null
                ? result.getGroupes()
                : new ArrayList<>();
            List<Reservation> reservationsNonAssignees = result.getReservationsNonAssignees() != null
                ? result.getReservationsNonAssignees()
                : new ArrayList<>();
            List<com.cousin.model.Trajet> trajets = result.getTrajets() != null
                ? result.getTrajets()
                : new ArrayList<>();

            Map<Integer, Vehicule> vehiculesUtilisesMap = new LinkedHashMap<>();
            Set<Integer> vehiculeIdsUtilises = new LinkedHashSet<>();
            for (Assignation assignation : assignations) {
                if (assignation == null) {
                    continue;
                }

                Vehicule vehicule = assignation.getVehicule();
                if (vehicule != null) {
                    vehiculesUtilisesMap.putIfAbsent(vehicule.getIdVehicule(), vehicule);
                    vehiculeIdsUtilises.add(vehicule.getIdVehicule());
                    continue;
                }

                int idVehicule = assignation.getIdVehicule();
                if (idVehicule > 0 && !vehiculeIdsUtilises.contains(idVehicule)) {
                    Vehicule proxyVehicule = new Vehicule();
                    proxyVehicule.setIdVehicule(idVehicule);
                    vehiculesUtilisesMap.put(idVehicule, proxyVehicule);
                    vehiculeIdsUtilises.add(idVehicule);
                }
            }

            Set<Vehicule> vehiculesUtilises = new LinkedHashSet<>(vehiculesUtilisesMap.values());
            List<Vehicule> tousVehicules = vehiculeService.listVehicules();
            List<Vehicule> vehiculesNonUtilises = new ArrayList<>();
            Map<Integer, Integer> capaciteParVehicule = new LinkedHashMap<>();
            for (Vehicule vehicule : tousVehicules) {
                if (vehicule == null) {
                    continue;
                }
                capaciteParVehicule.put(vehicule.getIdVehicule(), Math.max(0, vehicule.getNbPlace()));
                if (!vehiculeIdsUtilises.contains(vehicule.getIdVehicule())) {
                    vehiculesNonUtilises.add(vehicule);
                }
            }

            // Sprint 7 - Donnees de split exposees pour l'UI
            Map<Integer, Integer> passagersDemandesParReservation = new LinkedHashMap<>();
            Map<Integer, Integer> passagersAssignesParReservation = new LinkedHashMap<>();
            Map<Integer, List<Map<String, Object>>> splitDetailsParReservation = new LinkedHashMap<>();

            for (GroupeTemps groupe : groupes) {
                if (groupe == null || groupe.getReservations() == null) {
                    continue;
                }
                for (Reservation reservation : groupe.getReservations()) {
                    registerReservation(passagersDemandesParReservation, reservation);
                }
            }

            for (Reservation reservation : reservationsNonAssignees) {
                registerReservation(passagersDemandesParReservation, reservation);
            }

            for (Assignation assignation : assignations) {
                if (assignation == null) {
                    continue;
                }

                Reservation reservation = assignation.getReservation();
                registerReservation(passagersDemandesParReservation, reservation);

                int idReservation = reservation != null ? reservation.getIdReservation() : assignation.getIdReservation();
                if (idReservation <= 0) {
                    continue;
                }

                int passagersAssignes = resolvePassagersAssignes(assignation);
                passagersAssignesParReservation.put(
                        idReservation,
                        passagersAssignesParReservation.getOrDefault(idReservation, 0) + passagersAssignes
                );

                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("idAssignation", assignation.getIdAssignation());
                detail.put("idReservation", idReservation);
                detail.put("idVehicule", assignation.getIdVehicule());
                detail.put("vehiculeReference", assignation.getVehicule() != null
                        ? assignation.getVehicule().getReference()
                        : null);
                detail.put("idTrajet", assignation.getIdTrajet());
                detail.put("dateHeureDepart", assignation.getDateHeureDepart());
                detail.put("dateHeureRetour", assignation.getDateHeureRetour());
                detail.put("passagersAssignes", passagersAssignes);
                int capaciteVehicule = assignation.getVehicule() != null
                    ? Math.max(0, assignation.getVehicule().getNbPlace())
                    : Math.max(0, capaciteParVehicule.getOrDefault(assignation.getIdVehicule(), 0));
                detail.put("capaciteVehicule", capaciteVehicule);
                detail.put("placesRestantesVehicule", Math.max(0, capaciteVehicule - passagersAssignes));
                detail.put("estPartielle", reservation != null
                        && passagersAssignes > 0
                        && passagersAssignes < reservation.getNbPassager());

                splitDetailsParReservation
                        .computeIfAbsent(idReservation, key -> new ArrayList<>())
                        .add(detail);
            }

            Map<Integer, Integer> passagersRestantsParReservation = new LinkedHashMap<>();
            for (Map.Entry<Integer, Integer> entry : passagersDemandesParReservation.entrySet()) {
                int idReservation = entry.getKey();
                int demandes = Math.max(0, entry.getValue());
                int assignes = Math.max(0, passagersAssignesParReservation.getOrDefault(idReservation, 0));
                passagersRestantsParReservation.put(idReservation, Math.max(0, demandes - assignes));
            }

            for (Reservation reservation : reservationsNonAssignees) {
                if (reservation == null || reservation.getIdReservation() <= 0) {
                    continue;
                }
                passagersRestantsParReservation.putIfAbsent(
                        reservation.getIdReservation(),
                        Math.max(0, reservation.getNbPassager())
                );
            }

            List<Map<String, Object>> resumeGroupesSplit = new ArrayList<>();
            for (int i = 0; i < groupes.size(); i++) {
                GroupeTemps groupe = groupes.get(i);
                List<Reservation> reservationsGroupe = (groupe != null && groupe.getReservations() != null)
                        ? groupe.getReservations()
                        : new ArrayList<>();

                Set<Integer> reservationIdsGroupe = new LinkedHashSet<>();
                int totalDemandes = 0;
                for (Reservation reservation : reservationsGroupe) {
                    if (reservation == null || reservation.getIdReservation() <= 0) {
                        continue;
                    }
                    reservationIdsGroupe.add(reservation.getIdReservation());
                    totalDemandes += Math.max(0, reservation.getNbPassager());
                }

                LocalDateTime debutIntervalle = groupe != null ? groupe.getHeureDepartGroupe() : null;
                int attenteMinutes = groupe != null ? Math.max(0, groupe.getTempsAttenteMinutes()) : 0;
                LocalDateTime finIntervalle = debutIntervalle != null ? debutIntervalle.plusMinutes(attenteMinutes) : null;
                boolean dernierGroupe = (i == groupes.size() - 1);

                int totalAssignes = 0;
                for (Assignation assignation : assignations) {
                    if (assignation == null) {
                        continue;
                    }

                    Reservation reservation = assignation.getReservation();
                    int idReservation = reservation != null ? reservation.getIdReservation() : assignation.getIdReservation();
                    if (!reservationIdsGroupe.contains(idReservation)) {
                        continue;
                    }

                    if (!isInInterval(assignation.getDateHeureDepart(), debutIntervalle, finIntervalle, dernierGroupe)) {
                        continue;
                    }

                    totalAssignes += resolvePassagersAssignes(assignation);
                }

                Map<String, Object> resume = new LinkedHashMap<>();
                resume.put("numeroGroupe", i + 1);
                resume.put("debutIntervalle", debutIntervalle);
                resume.put("finIntervalle", finIntervalle);
                resume.put("totalDemandes", totalDemandes);
                resume.put("totalAssignes", totalAssignes);
                resume.put("totalReportes", Math.max(0, totalDemandes - totalAssignes));
                resume.put("nbReservations", reservationIdsGroupe.size());
                resumeGroupesSplit.add(resume);
            }

            mv.addAttribute("date", date.toString());
            mv.addAttribute("tempsAttente", tempsAttenteMinutes);
            mv.addAttribute("groupes", groupes);
            mv.addAttribute("assignations", assignations);
            mv.addAttribute("trajets", trajets);
            mv.addAttribute("vehiculesUtilises", vehiculesUtilises);
            mv.addAttribute("vehiculesNonUtilises", vehiculesNonUtilises);
            mv.addAttribute("reservationsNonAssignees", reservationsNonAssignees);
            mv.addAttribute("splitDetailsParReservation", splitDetailsParReservation);
            mv.addAttribute("passagersDemandesParReservation", passagersDemandesParReservation);
            mv.addAttribute("passagersAssignesParReservation", passagersAssignesParReservation);
            mv.addAttribute("passagersRestantsParReservation", passagersRestantsParReservation);
            mv.addAttribute("resumeGroupesSplit", resumeGroupesSplit);
            // Ajout : Map des réservations par véhicule pour affichage JSP
            Map<Integer, List<Reservation>> reservationsParVehicule = new LinkedHashMap<>();
            for (Assignation assignation : assignations) {
                if (assignation == null) continue;
                int idVehicule = assignation.getIdVehicule();
                Reservation reservation = assignation.getReservation();
                if (idVehicule > 0 && reservation != null) {
                    reservationsParVehicule.computeIfAbsent(idVehicule, k -> new ArrayList<>()).add(reservation);
                }
            }
            mv.addAttribute("reservationsParVehicule", reservationsParVehicule);
            mv.addAttribute("message", "Assignation effectuee avec succes pour le " + date);
        } catch (Exception e) {
            mv.addAttribute("error", "Erreur inattendue lors de l'assignation: " + e.getMessage());
        }

        return mv;
    }

    private void registerReservation(Map<Integer, Integer> passagersDemandesParReservation,
                                     Reservation reservation) {
        if (reservation == null || reservation.getIdReservation() <= 0) {
            return;
        }
        int idReservation = reservation.getIdReservation();
        passagersDemandesParReservation.putIfAbsent(idReservation, Math.max(0, reservation.getNbPassager()));
    }

    private int resolvePassagersAssignes(Assignation assignation) {
        if (assignation == null) {
            return 0;
        }

        int quantite = assignation.getQuantitePassagersAssignes();
        if (quantite > 0) {
            return quantite;
        }

        Reservation reservation = assignation.getReservation();
        if (reservation != null) {
            return Math.max(0, reservation.getNbPassager());
        }

        return 0;
    }

    private boolean isInInterval(LocalDateTime dateHeureDepart,
                                 LocalDateTime debutIntervalle,
                                 LocalDateTime finIntervalle,
                                 boolean dernierGroupe) {
        if (dateHeureDepart == null || debutIntervalle == null) {
            return false;
        }

        if (dernierGroupe) {
            return !dateHeureDepart.isBefore(debutIntervalle);
        }

        if (finIntervalle == null) {
            return !dateHeureDepart.isBefore(debutIntervalle);
        }

        return !dateHeureDepart.isBefore(debutIntervalle) && dateHeureDepart.isBefore(finIntervalle);
    }
}