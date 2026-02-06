package com.cousin.model;

public class Departement {
    private String code;
    private String nom;
    private String responsable;
    
    // Constructeurs
    public Departement() {}
    
    public Departement(String code, String nom, String responsable) {
        this.code = code;
        this.nom = nom;
        this.responsable = responsable;
    }
    
    // Getters et setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    
    @Override
    public String toString() {
        return "Departement{" +
               "code='" + code + '\'' +
               ", nom='" + nom + '\'' +
               ", responsable='" + responsable + '\'' +
               '}';
    }
}