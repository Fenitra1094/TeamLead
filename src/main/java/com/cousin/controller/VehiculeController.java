package com.cousin.controller;

import com.cousin.model.Vehicule;
import com.cousin.service.VehiculeService;
import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Param;
import com.framework.annotation.PostMapping;
import com.framework.model.ModelView;
import java.sql.SQLException;
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
            @Param("typeVehicule") String typeVehicule) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setReference(reference);
        vehicule.setNbPlace(nbPlace != null ? nbPlace : 0);
        vehicule.setTypeVehicule(typeVehicule);

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
