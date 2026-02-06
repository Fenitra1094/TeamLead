package com.cousin.model;

public class Emp {
    private String nom;
    private String prenom;
    private int age;
    private double salaire;
    private boolean actif;
    private Adresse adresse; // Objet imbriqué
    private Departement departement; // Autre objet
    
    // Constructeurs
    public Emp() {}
    
    public Emp(String nom, String prenom, int age, double salaire, boolean actif) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.salaire = salaire;
        this.actif = actif;
    }
    
    // Getters et setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public double getSalaire() { return salaire; }
    public void setSalaire(double salaire) { this.salaire = salaire; }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    
    public Adresse getAdresse() { return adresse; }
    public void setAdresse(Adresse adresse) { this.adresse = adresse; }
    
    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }
    
    @Override
    public String toString() {
        return "Emp{" +
               "nom='" + nom + '\'' +
               ", prenom='" + prenom + '\'' +
               ", age=" + age +
               ", salaire=" + salaire +
               ", actif=" + actif +
               ", adresse=" + (adresse != null ? adresse.toString() : "null") +
               ", departement=" + (departement != null ? departement.toString() : "null") +
               '}';
    }
}