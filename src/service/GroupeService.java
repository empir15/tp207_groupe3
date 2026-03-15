package service;

import database.DatabaseConnection;
import model.Groupe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service dédié à la gestion des Groupes d'étudiants (CRUD).
 */
public class GroupeService {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean ajouter(Groupe groupe) {
        String sql = "INSERT INTO groupes (nom, niveau, annee_scolaire) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, groupe.getNom());
            ps.setString(2, groupe.getNiveau());
            ps.setString(3, groupe.getAnneeScolaire());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout groupe : " + e.getMessage());
            return false;
        }
    }

    public List<Groupe> getAll() {
        List<Groupe> list = new ArrayList<>();
        String sql = "SELECT * FROM groupes ORDER BY niveau, nom";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Groupe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("niveau"),
                        rs.getString("annee_scolaire")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture groupes : " + e.getMessage());
        }
        return list;
    }

    public List<Groupe> getByNiveau(String niveau) {
        List<Groupe> list = new ArrayList<>();
        String sql = "SELECT * FROM groupes WHERE niveau = ? ORDER BY nom";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, niveau);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Groupe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("niveau"),
                        rs.getString("annee_scolaire")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture groupes par niveau : " + e.getMessage());
        }
        return list;
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM groupes WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression groupe : " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM groupes";
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count groupes : " + e.getMessage());
        }
        return 0;
    }
}
