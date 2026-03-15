package model;

/**
 * Représente un groupe d'étudiants (promotion / filière).
 * Exemple : "L2 Informatique - Groupe A", niveau "L2", annee "2025-2026"
 */
public class Groupe {

    private int id;
    private String nom; // Ex: "L2 INFO - Groupe A"
    private String niveau; // Ex: "L1", "L2", "L3", "M1", "M2"
    private String anneeScolaire; // Ex: "2025-2026"

    public Groupe() {
    }

    public Groupe(int id, String nom, String niveau, String anneeScolaire) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.anneeScolaire = anneeScolaire;
    }

    public Groupe(String nom, String niveau, String anneeScolaire) {
        this.nom = nom;
        this.niveau = niveau;
        this.anneeScolaire = anneeScolaire;
    }

    // --- Getters & Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    @Override
    public String toString() {
        return nom + " (" + niveau + " — " + anneeScolaire + ")";
    }
}
