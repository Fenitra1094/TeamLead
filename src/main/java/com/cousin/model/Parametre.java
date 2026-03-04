package com.cousin.model;

public class Parametre {
    private String code;
    private String valeur;
    private String unite;
    private String typeValeur;

    public Parametre() {
    }

    public Parametre(String code, String valeur, String unite, String typeValeur) {
        this.code = code;
        this.valeur = valeur;
        this.unite = unite;
        this.typeValeur = typeValeur;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public String getTypeValeur() {
        return typeValeur;
    }

    public void setTypeValeur(String typeValeur) {
        this.typeValeur = typeValeur;
    }
}
