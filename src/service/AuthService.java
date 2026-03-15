package service;

import model.Role;
import util.SessionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Service d'authentification a comptes predefinis.
 *
 * Dans une vraie application ce service ferait une requete SQL.
 * Ici les comptes sont hardcodes pour la demonstration.
 *
 * Comptes disponibles :
 * admin / admin123 -> ADMIN
 * prof / prof123 -> ENSEIGNANT
 * etudiant / etud123 -> ETUDIANT
 */
public class AuthService {

    /** Modele interne d'un compte */
    private record Compte(String prenom, String nom, Role role, int referenceId) {
    }

    /** Base de comptes (login -> Compte) */
    private static final Map<String, String> MOTS_DE_PASSE = new HashMap<>();
    private static final Map<String, Compte> COMPTES = new HashMap<>();

    static {
        // Administrateur
        MOTS_DE_PASSE.put("admin", "admin123");
        COMPTES.put("admin", new Compte("superAdmin", "Admin", Role.ADMIN, 0));

        // Enseignant
        MOTS_DE_PASSE.put("prof", "prof123");
        COMPTES.put("prof", new Compte("Jean", "Dupont", Role.ENSEIGNANT, 1));

        // Etudiant
        MOTS_DE_PASSE.put("etudiant", "etud123");
        COMPTES.put("etudiant", new Compte("Marie", "Martin", Role.ETUDIANT, 1));
    }

    /**
     * Tente de connecter l'utilisateur.
     *
     * @param login      identifiant saisi
     * @param motDePasse mot de passe saisi
     * @return true si la connexion reussit, false sinon
     */
    public boolean connecter(String login, String motDePasse) {
        String loginNorm = login.trim().toLowerCase();

        if (!MOTS_DE_PASSE.containsKey(loginNorm))
            return false;
        if (!MOTS_DE_PASSE.get(loginNorm).equals(motDePasse))
            return false;

        Compte c = COMPTES.get(loginNorm);
        SessionManager.getInstance().connecter(c.nom(), c.prenom(), c.role(), c.referenceId());
        return true;
    }

    /** Deconnecte l'utilisateur courant */
    public void deconnecter() {
        SessionManager.getInstance().deconnecter();
    }
}
