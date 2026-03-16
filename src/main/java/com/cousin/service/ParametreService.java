package com.cousin.service;

import java.sql.SQLException;

import com.cousin.repository.ParametreRepository;

public class ParametreService {
    private final ParametreRepository parametreRepository;

    public ParametreService() {
        this.parametreRepository = new ParametreRepository();
    }

    public ParametreService(ParametreRepository parametreRepository) {
        this.parametreRepository = parametreRepository;
    }

    public int getVm() throws SQLException {
        String valeurVm = parametreRepository.getValeurByCode("Vm");

        if (valeurVm == null || valeurVm.isBlank()) {
            throw new SQLException("Parametre Vm introuvable");
        }

        try {
            return Integer.parseInt(valeurVm);
        } catch (NumberFormatException e) {
            throw new SQLException("Valeur Vm invalide: " + valeurVm, e);
        }
    }

    public int getTempsAttente() throws SQLException {
        String valeurTempsAttente = parametreRepository.getValeurByCode("temps_attente");

        if (valeurTempsAttente == null || valeurTempsAttente.isBlank()) {
            return 30;
        }

        try {
            int tempsAttente = Integer.parseInt(valeurTempsAttente);
            if (tempsAttente <= 0) {
                throw new SQLException("Valeur temps_attente invalide: " + valeurTempsAttente);
            }
            return tempsAttente;
        } catch (NumberFormatException e) {
            throw new SQLException("Valeur temps_attente invalide: " + valeurTempsAttente, e);
        }
    }
}
