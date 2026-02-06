package com.cousin.model;

public class Diplome {
    private String titre;
    private String etablissement;
    private int annee;
    private String mention;
    
    // Constructeurs
    public Diplome() {}
    
    public Diplome(String titre, String etablissement, int annee, String mention) {
        this.titre = titre;
        this.etablissement = etablissement;
        this.annee = annee;
        this.mention = mention;
    }
    
    // Getters et setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getEtablissement() { return etablissement; }
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }
    
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    
    public String getMention() { return mention; }
    public void setMention(String mention) { this.mention = mention; }
    
    @Override
    public String toString() {
        return "Diplome{" +
               "titre='" + titre + '\'' +
               ", etablissement='" + etablissement + '\'' +
               ", annee=" + annee +
               ", mention='" + mention + '\'' +
               '}';
    }
}