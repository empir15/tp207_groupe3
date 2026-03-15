package service;

import database.DatabaseConnection;
import model.Cours;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service dédié à la gestion des Cours (CRUD).
 */
public class CoursService {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean ajouter(Cours cours) {
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

    public List<Cours> getAll() {
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

    public boolean supprimer(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression cours : " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM courses";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count cours : " + e.getMessage());
        }
        return 0;
    }
}
