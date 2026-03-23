package com.cousin.controller;

import com.cousin.model.Vehicule;
import com.cousin.service.VehiculeService;
import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Param;
import com.framework.annotation.PostMapping;
import com.framework.model.ModelView;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class VehiculeController {
    private final VehiculeService vehiculeService = new VehiculeService();

    @GetMapping("/vehicule/form")
    public ModelView showForm(
            @Param("id_vehicule") Integer idVehicule,
            @Param("action") String action) throws SQLException {
        ModelView mv = new ModelView("/WEB-INF/views/vehicule/form.jsp");
        String resolvedAction = "insert";

        if ("edit".equalsIgnoreCase(action) && idVehicule != null) {
            Vehicule vehicule = vehiculeService.getVehicule(idVehicule);
            mv.addAttribute("vehicule", vehicule);
            resolvedAction = "edit";
        }

        mv.addAttribute("action", resolvedAction);
        return mv;
    }

    @PostMapping("/vehicule/form")
    public ModelView saveVehicule(
            @Param("action") String action,
            @Param("id_vehicule") Integer idVehicule,
            @Param("reference") String reference,
            @Param("nbPlace") Integer nbPlace,
            @Param("typeVehicule") String typeVehicule,
            @Param("heureDisponibilite") String heureDisponibilite) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setReference(reference);
        vehicule.setNbPlace(nbPlace != null ? nbPlace : 0);
        vehicule.setTypeVehicule(typeVehicule);

        if (heureDisponibilite == null || heureDisponibilite.isBlank()) {
            ModelView mv = new ModelView("/WEB-INF/views/vehicule/form.jsp");
            mv.addAttribute("error", "Heure de disponibilite obligatoire.");
            mv.addAttribute("vehicule", vehicule);
            mv.addAttribute("action", action != null ? action : "insert");
            return mv;
        }

        LocalTime heureDisponibiliteValue;
        try {
            heureDisponibiliteValue = LocalTime.parse(heureDisponibilite);
        } catch (DateTimeParseException e) {
            ModelView mv = new ModelView("/WEB-INF/views/vehicule/form.jsp");
            mv.addAttribute("error", "Format d'heure invalide. Utilisez HH:mm.");
            mv.addAttribute("vehicule", vehicule);
            mv.addAttribute("action", action != null ? action : "insert");
            return mv;
        }
        vehicule.setHeureDisponibilite(heureDisponibiliteValue);

        String message;
        if ("edit".equalsIgnoreCase(action) && idVehicule != null) {
            vehicule.setIdVehicule(idVehicule);
            vehiculeService.updateVehicule(vehicule);
            message = "Vehicule modifie";
        } else {
            vehiculeService.createVehicule(vehicule);
            message = "Vehicule enregistre";
        }

        ModelView mv = new ModelView("/WEB-INF/views/vehicule/form.jsp");
        mv.addAttribute("message", message);
        mv.addAttribute("action", "insert");
        return mv;
    }

    @GetMapping("/vehicule/list")
    public ModelView listVehicules(
            @Param("action") String action,
            @Param("id_vehicule") Integer idVehicule) throws SQLException {
        if ("edit".equalsIgnoreCase(action) && idVehicule != null) {
            Vehicule vehicule = vehiculeService.getVehicule(idVehicule);
            ModelView formView = new ModelView("/WEB-INF/views/vehicule/form.jsp");
            formView.addAttribute("vehicule", vehicule);
            formView.addAttribute("action", "edit");
            return formView;
        }

        String message = null;
        if ("delete".equalsIgnoreCase(action) && idVehicule != null) {
            vehiculeService.deleteVehicule(idVehicule);
            message = "Vehicule supprime";
        }

        List<Vehicule> vehicules = vehiculeService.listVehicules();
        ModelView mv = new ModelView("/WEB-INF/views/vehicule/list.jsp");
        mv.addAttribute("vehicules", vehicules);
        if (message != null) {
            mv.addAttribute("message", message);
        }
        return mv;
    }
}
