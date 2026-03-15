package service;

import database.DatabaseConnection;
import model.Cours;
import model.Enseignant;
import model.Salle;
import model.Planning;
import model.Examen;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanningService {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ===================== COURS =====================

    public boolean ajouterCours(Cours cours) {
        String sql = "INSERT INTO courses (nom, description) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, cours.getNom());
            ps.setString(2, cours.getDescription());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout cours : " + e.getMessage());
            return false;
        }
    }

    public List<Cours> getAllCours() {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY nom";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Cours(rs.getInt("id"), rs.getString("nom"), rs.getString("description")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture cours : " + e.getMessage());
        }
        return list;
    }

    public boolean supprimerCours(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression cours : " + e.getMessage());
            return false;
        }
    }

    // ===================== ENSEIGNANTS =====================

    public boolean ajouterEnseignant(Enseignant e) {
        String sql = "INSERT INTO teachers (nom, prenom, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getEmail());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erreur ajout enseignant : " + ex.getMessage());
            return false;
        }
    }

    public List<Enseignant> getAllEnseignants() {
        List<Enseignant> list = new ArrayList<>();
        String sql = "SELECT * FROM teachers ORDER BY nom";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Enseignant(rs.getInt("id"), rs.getString("nom"),
                        rs.getString("prenom"), rs.getString("email")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture enseignants : " + e.getMessage());
        }
        return list;
    }

    public boolean supprimerEnseignant(int id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression enseignant : " + e.getMessage());
            return false;
        }
    }

    // ===================== SALLES =====================

    public boolean ajouterSalle(Salle salle) {
        String sql = "INSERT INTO classrooms (nom, capacite) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, salle.getNom());
            ps.setInt(2, salle.getCapacite());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout salle : " + e.getMessage());
            return false;
        }
    }

    public List<Salle> getAllSalles() {
        List<Salle> list = new ArrayList<>();
        String sql = "SELECT * FROM classrooms ORDER BY nom";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Salle(rs.getInt("id"), rs.getString("nom"), rs.getInt("capacite")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture salles : " + e.getMessage());
        }
        return list;
    }

    public boolean supprimerSalle(int id) {
        String sql = "DELETE FROM classrooms WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression salle : " + e.getMessage());
            return false;
        }
    }

    // ===================== PLANNING =====================

    public boolean ajouterPlanning(Planning p) {
        String sql = "INSERT INTO planning (course_id, teacher_id, classroom_id, jour, heure) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, p.getCourseId());
            ps.setInt(2, p.getTeacherId());
            ps.setInt(3, p.getClassroomId());
            ps.setString(4, p.getJour());
            ps.setString(5, p.getHeure());
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
                    SELECT p.id, p.course_id, p.teacher_id, p.classroom_id, p.jour, p.heure,
                           c.nom AS cours_nom,
                           (t.prenom || ' ' || t.nom) AS enseignant_nom,
                           cl.nom AS salle_nom
                    FROM planning p
                    JOIN courses c ON p.course_id = c.id
                    JOIN teachers t ON p.teacher_id = t.id
                    JOIN classrooms cl ON p.classroom_id = cl.id
                    ORDER BY p.jour, p.heure
                """;
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Planning pl = new Planning(rs.getInt("id"), rs.getInt("course_id"),
                        rs.getInt("teacher_id"), rs.getInt("classroom_id"),
                        rs.getString("jour"), rs.getString("heure"));
                pl.setCoursNom(rs.getString("cours_nom"));
                pl.setEnseignantNom(rs.getString("enseignant_nom"));
                pl.setSalleNom(rs.getString("salle_nom"));
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
                           c.nom AS cours_nom, cl.nom AS salle_nom
                    FROM exams e
                    JOIN courses c ON e.course_id = c.id
                    JOIN classrooms cl ON e.classroom_id = cl.id
                    ORDER BY e.date, e.heure
                """;
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Examen ex = new Examen(rs.getInt("id"), rs.getInt("course_id"),
                        rs.getString("date"), rs.getString("heure"), rs.getInt("classroom_id"));
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

    // ===================== STATISTIQUES =====================

    public int countCours() {
        return countTable("courses");
    }

    public int countEnseignants() {
        return countTable("teachers");
    }

    public int countSalles() {
        return countTable("classrooms");
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
}
