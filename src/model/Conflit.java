package model;

/**
 * Represente un conflit detecte lors de la planification d'une session.
 * Un conflit survient quand deux sessions se chevauchent sur la meme
 * ressource (salle, enseignant, ou groupe) au meme moment.
 */
public class Conflit {

    public enum TypeConflit {
        ENSEIGNANT, // L'enseignant a deja un cours sur ce creneau
        SALLE, // La salle est deja occupee sur ce creneau
        GROUPE, // Le groupe a deja un cours sur ce creneau
        CAPACITE // La salle est trop petite pour le groupe
    }

    private final TypeConflit type;
    private final String message;
    private final String ressourceConflictuelle;
    private final String creneauExistant;

    public Conflit(TypeConflit type, String message,
            String ressourceConflictuelle, String creneauExistant) {
        this.type = type;
        this.message = message;
        this.ressourceConflictuelle = ressourceConflictuelle;
        this.creneauExistant = creneauExistant;
    }

    public TypeConflit getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getRessourceConflictuelle() {
        return ressourceConflictuelle;
    }

    public String getCreneauExistant() {
        return creneauExistant;
    }

    /** Retourne le prefixe associe au type de conflit */
    public String getIcone() {
        return switch (type) {
            case ENSEIGNANT -> "[Enseignant]";
            case SALLE -> "[Salle]";
            case GROUPE -> "[Groupe]";
            case CAPACITE -> "[Capacite]";
        };
    }

    /** Retourne un message complet formate pour l'affichage */
    public String getMessageComplet() {
        String base = getIcone() + " " + message;
        if (creneauExistant != null && !creneauExistant.isEmpty()) {
            base += "\n    Conflit avec : " + creneauExistant;
        }
        return base;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + message;
    }
}
