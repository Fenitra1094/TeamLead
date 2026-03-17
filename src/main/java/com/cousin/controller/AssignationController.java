package com.cousin.controller;

import com.cousin.service.AssignationService;
import com.cousin.service.ParametreService;
import com.cousin.service.VehiculeService;
import com.cousin.model.Assignation;
import com.cousin.model.Vehicule;
import com.cousin.util.AssignationResult;
import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Param;
import com.framework.model.ModelView;

import java.time.LocalDate;
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

            Map<Integer, Vehicule> vehiculesUtilisesMap = new LinkedHashMap<>();
            Set<Integer> vehiculeIdsUtilises = new LinkedHashSet<>();
            for (Assignation assignation : result.getAssignations()) {
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
            for (Vehicule vehicule : tousVehicules) {
                if (vehicule == null) {
                    continue;
                }
                if (!vehiculeIdsUtilises.contains(vehicule.getIdVehicule())) {
                    vehiculesNonUtilises.add(vehicule);
                }
            }

            mv.addAttribute("date", date.toString());
            mv.addAttribute("tempsAttente", tempsAttenteMinutes);
            mv.addAttribute("groupes", result.getGroupes());
            mv.addAttribute("assignations", result.getAssignations());
            mv.addAttribute("trajets", result.getTrajets());
            mv.addAttribute("vehiculesUtilises", vehiculesUtilises);
            mv.addAttribute("vehiculesNonUtilises", vehiculesNonUtilises);
            mv.addAttribute("reservationsNonAssignees", result.getReservationsNonAssignees());
            mv.addAttribute("message", "Assignation effectuee avec succes pour le " + date);
        } catch (Exception e) {
            mv.addAttribute("error", "Erreur inattendue lors de l'assignation: " + e.getMessage());
        }

        return mv;
    }
}