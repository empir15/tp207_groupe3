package service;

import database.DatabaseConnection;
import model.Enseignant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service dédié à la gestion des Enseignants (CRUD).
 */
public class EnseignantService {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean ajouter(Enseignant e) {
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

    public List<Enseignant> getAll() {
        List<Enseignant> list = new ArrayList<>();
        String sql = "SELECT * FROM teachers ORDER BY nom";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Enseignant(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture enseignants : " + e.getMessage());
        }
        return list;
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression enseignant : " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM teachers";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count enseignants : " + e.getMessage());
        }
        return 0;
    }
}
