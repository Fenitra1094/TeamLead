package com.cousin.controller;

import com.cousin.service.AssignationService;
import com.cousin.util.AssignationResult;
import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Param;
import com.framework.model.ModelView;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
public class AssignationController {
    private final AssignationService assignationService = new AssignationService();

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
        ModelView mv = new ModelView("/WEB-INF/views/assignation.jsp");

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
            AssignationResult result = assignationService.assignerPourDate(date);
            mv.addAttribute("date", date.toString());
            mv.addAttribute("assignations", result.getAssignations());
            mv.addAttribute("reservationsNonAssignees", result.getReservationsNonAssignees());
            mv.addAttribute("trajets", result.getTrajets());
            mv.addAttribute("message", "Assignation effectuee avec succes pour le " + date);
        } catch (Exception e) {
            mv.addAttribute("error", "Erreur inattendue lors de l'assignation: " + e.getMessage());
        }

        return mv;
    }
}