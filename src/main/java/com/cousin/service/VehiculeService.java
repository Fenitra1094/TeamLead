package com.cousin.service;

import java.sql.SQLException;
import java.util.List;

import com.cousin.model.Vehicule;
import com.cousin.repository.VehiculeRepository;

public class VehiculeService {
    private final VehiculeRepository vehiculeRepository;

    public VehiculeService() {
        this.vehiculeRepository = new VehiculeRepository();
    }

    public VehiculeService(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    public void createVehicule(Vehicule vehicule) throws SQLException {
        vehiculeRepository.insert(vehicule);
    }

    public void updateVehicule(Vehicule vehicule) throws SQLException {
        vehiculeRepository.update(vehicule);
    }

    public void deleteVehicule(int idVehicule) throws SQLException {
        vehiculeRepository.deleteById(idVehicule);
    }

    public Vehicule getVehicule(int idVehicule) throws SQLException {
        return vehiculeRepository.findById(idVehicule);
    }

    public List<Vehicule> listVehicules() throws SQLException {
        return vehiculeRepository.findAll();
    }
}
