package service;

import database.DatabaseConnection;
import model.Conflit;
import model.Conflit.TypeConflit;
import model.Planning;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de détection des conflits de planning.
 *
 * <p>
 * Principe de chevauchement horaire :
 * </p>
 * 
 * <pre>
 *   Deux créneaux [debutA, finA] et [debutB, finB] se chevauchent si :
 *       debutA < finB  ET  debutB < finA
 * </pre>
 * <p>
 * Ce service est appelé AVANT toute insertion en base pour garantir
 * la cohérence du planning universitaire.
 * </p>
 */
public class ConflitService {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Point d'entrée principal. Vérifie tous les types de conflits
     * pour une session candidate et retourne la liste des conflits trouvés.
     *
     * @param candidat La session à valider (non encore enregistrée)
     * @return Liste des conflits détectés (vide = pas de conflit)
     */
    public List<Conflit> verifierTousLesConflits(Planning candidat) {
        List<Conflit> conflits = new ArrayList<>();

        conflits.addAll(verifierConflitEnseignant(candidat));
        conflits.addAll(verifierConflitSalle(candidat));
        if (candidat.getGroupeId() > 0) {
            conflits.addAll(verifierConflitGroupe(candidat));
        }

        return conflits;
    }

    // ===================== CONFLIT ENSEIGNANT =====================

    /**
     * Vérifie si l'enseignant est déjà occupé sur ce créneau (même jour,
     * plage horaire qui se chevauche).
     */
    public List<Conflit> verifierConflitEnseignant(Planning candidat) {
        List<Conflit> conflits = new ArrayList<>();

        String sql = """
                SELECT p.id,
                       p.jour,
                       p.heure_debut,
                       p.heure_fin,
                       c.nom  AS cours_nom,
                       cl.nom AS salle_nom
                FROM planning p
                JOIN courses    c  ON p.course_id    = c.id
                JOIN classrooms cl ON p.classroom_id = cl.id
                WHERE p.teacher_id = ?
                  AND p.jour       = ?
                  AND p.heure_debut < ?
                  AND p.heure_fin   > ?
                  AND p.id != ?
                """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, candidat.getTeacherId());
            ps.setString(2, candidat.getJour());
            ps.setString(3, candidat.getHeureFin());
            ps.setString(4, candidat.getHeureDebut());
            ps.setInt(5, candidat.getId()); // 0 si nouvel enregistrement — ignoré

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String creneauExistant = rs.getString("cours_nom")
                        + " | " + rs.getString("salle_nom")
                        + " | " + rs.getString("heure_debut")
                        + "–" + rs.getString("heure_fin");

                conflits.add(new Conflit(
                        TypeConflit.ENSEIGNANT,
                        "L'enseignant est déjà occupé le "
                                + candidat.getJour()
                                + " de " + candidat.getHeureDebut()
                                + " à " + candidat.getHeureFin(),
                        "Enseignant (ID " + candidat.getTeacherId() + ")",
                        creneauExistant));
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification conflit enseignant : " + e.getMessage());
        }

        return conflits;
    }

    // ===================== CONFLIT SALLE =====================

    /**
     * Vérifie si la salle est déjà occupée sur ce créneau.
     */
    public List<Conflit> verifierConflitSalle(Planning candidat) {
        List<Conflit> conflits = new ArrayList<>();

        String sql = """
                SELECT p.id,
                       p.heure_debut,
                       p.heure_fin,
                       c.nom  AS cours_nom,
                       (t.prenom || ' ' || t.nom) AS enseignant_nom
                FROM planning p
                JOIN courses  c ON p.course_id  = c.id
                JOIN teachers t ON p.teacher_id = t.id
                WHERE p.classroom_id = ?
                  AND p.jour         = ?
                  AND p.heure_debut  < ?
                  AND p.heure_fin    > ?
                  AND p.id          != ?
                """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, candidat.getClassroomId());
            ps.setString(2, candidat.getJour());
            ps.setString(3, candidat.getHeureFin());
            ps.setString(4, candidat.getHeureDebut());
            ps.setInt(5, candidat.getId());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String creneauExistant = rs.getString("cours_nom")
                        + " | " + rs.getString("enseignant_nom")
                        + " | " + rs.getString("heure_debut")
                        + "–" + rs.getString("heure_fin");

                conflits.add(new Conflit(
                        TypeConflit.SALLE,
                        "La salle est déjà occupée le "
                                + candidat.getJour()
                                + " de " + candidat.getHeureDebut()
                                + " à " + candidat.getHeureFin(),
                        "Salle (ID " + candidat.getClassroomId() + ")",
                        creneauExistant));
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification conflit salle : " + e.getMessage());
        }

        return conflits;
    }

    // ===================== CONFLIT GROUPE =====================

    /**
     * Vérifie si le groupe a déjà un cours sur ce créneau.
     */
    public List<Conflit> verifierConflitGroupe(Planning candidat) {
        List<Conflit> conflits = new ArrayList<>();

        String sql = """
                SELECT p.id,
                       p.heure_debut,
                       p.heure_fin,
                       c.nom  AS cours_nom,
                       (t.prenom || ' ' || t.nom) AS enseignant_nom,
                       cl.nom AS salle_nom
                FROM planning p
                JOIN courses    c  ON p.course_id    = c.id
                JOIN teachers   t  ON p.teacher_id   = t.id
                JOIN classrooms cl ON p.classroom_id = cl.id
                WHERE p.groupe_id  = ?
                  AND p.jour       = ?
                  AND p.heure_debut < ?
                  AND p.heure_fin   > ?
                  AND p.id         != ?
                """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, candidat.getGroupeId());
            ps.setString(2, candidat.getJour());
            ps.setString(3, candidat.getHeureFin());
            ps.setString(4, candidat.getHeureDebut());
            ps.setInt(5, candidat.getId());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String creneauExistant = rs.getString("cours_nom")
                        + " | " + rs.getString("enseignant_nom")
                        + " | " + rs.getString("salle_nom")
                        + " | " + rs.getString("heure_debut")
                        + "–" + rs.getString("heure_fin");

                conflits.add(new Conflit(
                        TypeConflit.GROUPE,
                        "Le groupe a déjà un cours le "
                                + candidat.getJour()
                                + " de " + candidat.getHeureDebut()
                                + " à " + candidat.getHeureFin(),
                        "Groupe (ID " + candidat.getGroupeId() + ")",
                        creneauExistant));
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification conflit groupe : " + e.getMessage());
        }

        return conflits;
    }

    // ===================== RÉSUMÉ =====================

    /**
     * Retourne un résumé textuel formaté de tous les conflits.
     * Utilisé pour l'affichage dans les boîtes de dialogue.
     */
    public static String formaterConflits(List<Conflit> conflits) {
        if (conflits.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("⛔ ")
                .append(conflits.size())
                .append(" conflit(s) détecté(s) :\n\n");

        for (Conflit c : conflits) {
            sb.append(c.getMessageComplet()).append("\n\n");
        }

        return sb.toString().trim();
    }
}
