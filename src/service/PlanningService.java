package service;

import database.DatabaseConnection;
import model.Conflit;
import model.Examen;
import model.Planning;
import model.Planning.TypeSession;
import model.Planning.Recurrence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service dédié à la gestion des Sessions de Planning et des Examens.
 * Les CRUD Cours/Enseignant/Salle/Groupe sont délégués à leurs services dédiés.
 * Ce service gère aussi les statistiques globales pour le Dashboard.
 */
public class PlanningService {

    // Délégation aux services spécialisés
    private final CoursService coursService = new CoursService();
    private final EnseignantService enseignantService = new EnseignantService();
    private final SalleService salleService = new SalleService();
    private final GroupeService groupeService = new GroupeService();
    private final ConflitService conflitService = new ConflitService();

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ===================== COMPATIBILITÉ (délégation) =====================
    // Ces méthodes conservent la compatibilité avec l'ancien code des contrôleurs.

    public java.util.List<model.Cours> getAllCours() {
        return coursService.getAll();
    }

    public java.util.List<model.Enseignant> getAllEnseignants() {
        return enseignantService.getAll();
    }

    public java.util.List<model.Salle> getAllSalles() {
        return salleService.getAll();
    }

    public java.util.List<model.Groupe> getAllGroupes() {
        return groupeService.getAll();
    }

    public boolean ajouterCours(model.Cours c) {
        return coursService.ajouter(c);
    }

    public boolean supprimerCours(int id) {
        return coursService.supprimer(id);
    }

    public boolean ajouterEnseignant(model.Enseignant e) {
        return enseignantService.ajouter(e);
    }

    public boolean supprimerEnseignant(int id) {
        return enseignantService.supprimer(id);
    }

    public boolean ajouterSalle(model.Salle s) {
        return salleService.ajouter(s);
    }

    public boolean supprimerSalle(int id) {
        return salleService.supprimer(id);
    }

    // ===================== PLANNING =====================

    /**
     * Vérifie les conflits sans enregistrer.
     * 
     * @return liste de conflits détectés (vide = aucun conflit)
     */
    public List<Conflit> verifierConflits(Planning p) {
        return conflitService.verifierTousLesConflits(p);
    }

    /**
     * Tente d'ajouter une session après vérification des conflits.
     * 
     * @return liste vide si succès, liste de conflits si refus
     */
    public List<Conflit> ajouterPlanningAvecVerification(Planning p) {
        List<Conflit> conflits = conflitService.verifierTousLesConflits(p);
        if (!conflits.isEmpty()) {
            return conflits; // Bloqué : retourne les conflits trouvés
        }
        ajouterPlanning(p); // Pas de conflit : on insère
        return List.of(); // Succès = liste vide
    }

    public boolean ajouterPlanning(Planning p) {
        String sql = """
                INSERT INTO planning
                    (course_id, teacher_id, classroom_id, groupe_id,
                     jour, heure_debut, heure_fin, type_session, recurrence)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, p.getCourseId());
            ps.setInt(2, p.getTeacherId());
            ps.setInt(3, p.getClassroomId());
            if (p.getGroupeId() > 0)
                ps.setInt(4, p.getGroupeId());
            else
                ps.setNull(4, Types.INTEGER);
            ps.setString(5, p.getJour());
            ps.setString(6, p.getHeureDebut());
            ps.setString(7, p.getHeureFin());
            ps.setString(8, p.getTypeSession() != null ? p.getTypeSession().name() : "CM");
            ps.setString(9, p.getRecurrence() != null ? p.getRecurrence().name() : "HEBDOMADAIRE");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout planning : " + e.getMessage());
            return false;
        }
    }

    public List<Planning> getAllPlannings() {
        List<Planning> list = new ArrayList<>();
        String sql = """
                SELECT p.id, p.course_id, p.teacher_id, p.classroom_id,
                       COALESCE(p.groupe_id, 0) AS groupe_id,
                       p.jour, p.heure_debut, p.heure_fin,
                       p.type_session, p.recurrence,
                       c.nom  AS cours_nom,
                       (t.prenom || ' ' || t.nom) AS enseignant_nom,
                       cl.nom AS salle_nom,
                       COALESCE(g.nom, '—') AS groupe_nom
                FROM planning p
                JOIN courses    c  ON p.course_id    = c.id
                JOIN teachers   t  ON p.teacher_id   = t.id
                JOIN classrooms cl ON p.classroom_id = cl.id
                LEFT JOIN groupes g ON p.groupe_id   = g.id
                ORDER BY
                    CASE p.jour
                        WHEN 'Lundi'    THEN 1
                        WHEN 'Mardi'    THEN 2
                        WHEN 'Mercredi' THEN 3
                        WHEN 'Jeudi'    THEN 4
                        WHEN 'Vendredi' THEN 5
                        WHEN 'Samedi'   THEN 6
                        ELSE 7
                    END,
                    p.heure_debut
                """;
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                TypeSession type = parseTypeSession(rs.getString("type_session"));
                Recurrence rec = parseRecurrence(rs.getString("recurrence"));

                Planning pl = new Planning(
                        rs.getInt("id"),
                        rs.getInt("course_id"),
                        rs.getInt("teacher_id"),
                        rs.getInt("classroom_id"),
                        rs.getInt("groupe_id"),
                        rs.getString("jour"),
                        rs.getString("heure_debut"),
                        rs.getString("heure_fin"),
                        type, rec);

                pl.setCoursNom(rs.getString("cours_nom"));
                pl.setEnseignantNom(rs.getString("enseignant_nom"));
                pl.setSalleNom(rs.getString("salle_nom"));
                pl.setGroupeNom(rs.getString("groupe_nom"));
                list.add(pl);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture planning : " + e.getMessage());
        }
        return list;
    }

    public boolean supprimerPlanning(int id) {
        String sql = "DELETE FROM planning WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression planning : " + e.getMessage());
            return false;
        }
    }

    // ===================== EXAMENS =====================

    public boolean ajouterExamen(Examen ex) {
        String sql = "INSERT INTO exams (course_id, date, heure, classroom_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, ex.getCourseId());
            ps.setString(2, ex.getDate());
            ps.setString(3, ex.getHeure());
            ps.setInt(4, ex.getClassroomId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout examen : " + e.getMessage());
            return false;
        }
    }

    public List<Examen> getAllExamens() {
        List<Examen> list = new ArrayList<>();
        String sql = """
                SELECT e.id, e.course_id, e.date, e.heure, e.classroom_id,
                       c.nom  AS cours_nom,
                       cl.nom AS salle_nom
                FROM exams e
                JOIN courses    c  ON e.course_id    = c.id
                JOIN classrooms cl ON e.classroom_id = cl.id
                ORDER BY e.date, e.heure
                """;
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Examen ex = new Examen(
                        rs.getInt("id"), rs.getInt("course_id"),
                        rs.getString("date"), rs.getString("heure"),
                        rs.getInt("classroom_id"));
                ex.setCoursNom(rs.getString("cours_nom"));
                ex.setSalleNom(rs.getString("salle_nom"));
                list.add(ex);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture examens : " + e.getMessage());
        }
        return list;
    }

    public boolean supprimerExamen(int id) {
        String sql = "DELETE FROM exams WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression examen : " + e.getMessage());
            return false;
        }
    }

    // ===================== STATISTIQUES (Dashboard) =====================

    public int countCours() {
        return coursService.count();
    }

    public int countEnseignants() {
        return enseignantService.count();
    }

    public int countSalles() {
        return salleService.count();
    }

    public int countGroupes() {
        return groupeService.count();
    }

    public int countPlannings() {
        return countTable("planning");
    }

    public int countExamens() {
        return countTable("exams");
    }

    private int countTable(String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count " + table + " : " + e.getMessage());
        }
        return 0;
    }

    // ===================== UTILITAIRES =====================

    private TypeSession parseTypeSession(String val) {
        try {
            return TypeSession.valueOf(val);
        } catch (Exception e) {
            return TypeSession.CM;
        }
    }

    private Recurrence parseRecurrence(String val) {
        try {
            return Recurrence.valueOf(val);
        } catch (Exception e) {
            return Recurrence.HEBDOMADAIRE;
        }
    }
}
