package model;

/**
 * Roles disponibles dans PlanifyEdu.
 * Chaque role determine les fonctionnalites accessibles dans l'interface.
 */
public enum Role {

    ADMIN(
            "Administrateur",
            "Acces complet : gestion des plannings, enseignants, salles, cours et examens.",
            "[Admin]"),
    ENSEIGNANT(
            "Enseignant",
            "Consultation du planning personnel, vue calendrier.",
            "[Ens]"),
    ETUDIANT(
            "Etudiant",
            "Consultation du planning de son groupe, vue calendrier en lecture seule.",
            "[Etu]");

    private final String libelle;
    private final String description;
    private final String icone;

    Role(String libelle, String description, String icone) {
        this.libelle = libelle;
        this.description = description;
        this.icone = icone;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public String getIcone() {
        return icone;
    }

    @Override
    public String toString() {
        return icone + "  " + libelle;
    }
}
