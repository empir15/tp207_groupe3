package util;

import model.Role;

/**
 * Gestionnaire de session utilisateur (singleton).
 *
 * <p>
 * Conserve l'utilisateur actuellement connecté ainsi que son rôle.
 * Utilisé par tous les contrôleurs pour adapter l'interface.
 * </p>
 */
public class SessionManager {

    private static SessionManager instance;

    private String nomUtilisateur;
    private String prenomUtilisateur;
    private Role role;
    private int referenceId; // ID enseignant ou groupe lié (0 si admin)

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ---- Connexion ----

    public void connecter(String nom, String prenom, Role role, int referenceId) {
        this.nomUtilisateur = nom;
        this.prenomUtilisateur = prenom;
        this.role = role;
        this.referenceId = referenceId;
    }

    public void deconnecter() {
        this.nomUtilisateur = null;
        this.prenomUtilisateur = null;
        this.role = null;
        this.referenceId = 0;
    }

    // ---- Vérifications de rôle ----

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isEnseignant() {
        return role == Role.ENSEIGNANT;
    }

    public boolean isEtudiant() {
        return role == Role.ETUDIANT;
    }

    public boolean isConnecte() {
        return role != null;
    }

    /** Vrai si le rôle actuel a accès à une action de modification */
    public boolean peutModifier() {
        return role == Role.ADMIN;
    }

    /** Vrai si le rôle actuel peut consulter le calendrier */
    public boolean peutVoirCalendrier() {
        return role != null;
    }

    // ---- Getters ----

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public String getPrenomUtilisateur() {
        return prenomUtilisateur;
    }

    public Role getRole() {
        return role;
    }

    public int getReferenceId() {
        return referenceId;
    }

    /** Ex : "Jean Dupont (Administrateur)" */
    public String getAffichageComplet() {
        if (!isConnecte())
            return "Non connecté";
        return prenomUtilisateur + " " + nomUtilisateur
                + " — " + role.getIcone() + " " + role.getLibelle();
    }
}
