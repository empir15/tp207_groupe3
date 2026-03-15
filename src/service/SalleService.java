package service;

import database.DatabaseConnection;
import model.Salle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service dédié à la gestion des Salles (CRUD).
 */
public class SalleService {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean ajouter(Salle salle) {
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

    public List<Salle> getAll() {
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

    public boolean supprimer(int id) {
        String sql = "DELETE FROM classrooms WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression salle : " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM classrooms";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count salles : " + e.getMessage());
        }
        return 0;
    }
}
