package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ExportService;
import service.PlanningService;
import util.SessionManager;

public class DashboardController {

    // ---- Stats labels ----
    @FXML
    private Label labelCours;
    @FXML
    private Label labelEnseignants;
    @FXML
    private Label labelSalles;
    @FXML
    private Label labelGroupes;
    @FXML
    private Label labelPlannings;
    @FXML
    private Label labelExamens;

    // ---- Infos utilisateur connecté ----
    @FXML
    private Label labelUtilisateur;
    @FXML
    private Label labelRole;

    // ---- Cartes d'action (pour restriction par rôle) ----
    @FXML
    private VBox cardAjouterCours;
    @FXML
    private VBox cardAjouterEnseignant;
    @FXML
    private VBox cardAjouterSalle;
    @FXML
    private VBox cardCreerHoraire;
    @FXML
    private VBox cardPlanifierExamen;

    private final PlanningService service = new PlanningService();
    private final ExportService exportService = new ExportService();
    private final SessionManager session = SessionManager.getInstance();

    @FXML
    public void initialize() {
        refreshStats();
        appliquerRestrictionRoles();
    }

    /** Rafraîchit les compteurs de statistiques */
    public void refreshStats() {
        labelCours.setText(String.valueOf(service.countCours()));
        labelEnseignants.setText(String.valueOf(service.countEnseignants()));
        labelSalles.setText(String.valueOf(service.countSalles()));
        if (labelGroupes != null)
            labelGroupes.setText(String.valueOf(service.countGroupes()));
        labelPlannings.setText(String.valueOf(service.countPlannings()));
        labelExamens.setText(String.valueOf(service.countExamens()));

        // Info utilisateur
        if (labelUtilisateur != null && session.isConnecte())
            labelUtilisateur.setText(session.getPrenomUtilisateur() + " " + session.getNomUtilisateur());
        if (labelRole != null && session.getRole() != null)
            labelRole.setText(session.getRole().getIcone() + "  " + session.getRole().getLibelle());
    }

    /**
     * Restreint les cartes d'action selon le rôle de l'utilisateur connecté.
     * Seul l'ADMIN peut ajouter/modifier.
     */
    private void appliquerRestrictionRoles() {
        boolean peutModifier = session.peutModifier();

        setCardDisabled(cardAjouterCours, !peutModifier);
        setCardDisabled(cardAjouterEnseignant, !peutModifier);
        setCardDisabled(cardAjouterSalle, !peutModifier);
        setCardDisabled(cardCreerHoraire, !peutModifier);
        setCardDisabled(cardPlanifierExamen, !peutModifier);
    }

    /** Désactive visuellement une carte d'action */
    private void setCardDisabled(VBox card, boolean disabled) {
        if (card == null)
            return;
        card.setOpacity(disabled ? 0.35 : 1.0);
        card.setDisable(disabled);
        if (disabled) {
            card.setStyle(card.getStyle() + " -fx-cursor: default;");
        }
    }

    // =====================================================
    // ACTIONS DES CARTES
    // =====================================================

    @FXML
    private void ouvrirAjouterCours() {
        ouvrirFenetre("/ui/AjouterCours.fxml", "Ajouter un Cours");
    }

    @FXML
    private void ouvrirAjouterEnseignant() {
        ouvrirFenetre("/ui/AjouterEnseignant.fxml", "Ajouter un Enseignant");
    }

    @FXML
    private void ouvrirAjouterSalle() {
        ouvrirFenetre("/ui/AjouterSalle.fxml", "Ajouter une Salle");
    }

    @FXML
    private void ouvrirCreerHoraire() {
        ouvrirFenetre("/ui/PlanningView.fxml", "Gestion des Horaires");
    }

    @FXML
    private void ouvrirPlanifierExamen() {
        ouvrirFenetre("/ui/ExamScheduler.fxml", "Planifier un Examen");
    }

    @FXML
    private void ouvrirVoirHoraires() {
        ouvrirFenetre("/ui/PlanningView.fxml", "Visualiser les Horaires");
    }

    @FXML
    private void ouvrirVoirExamens() {
        ouvrirFenetre("/ui/ExamScheduler.fxml", "Visualiser les Examens");
    }

    @FXML
    private void ouvrirCalendrier() {
        ouvrirFenetre("/ui/CalendrierView.fxml", "Calendrier Hebdomadaire");
    }

    /** Exporte le planning complet en HTML → navigateur → impression PDF */
    @FXML
    private void exporterPdf() {
        boolean ok = exportService.exporterHtml(
                service.getAllPlannings(),
                "Planning général complet");
        if (!ok) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export impossible");
            alert.setHeaderText("Impossible d'ouvrir le navigateur");
            alert.setContentText("Vérifiez qu'un navigateur est installé sur ce système.");
            alert.showAndWait();
        }
    }

    /** Déconnecte et revient à l'écran de login */
    @FXML
    private void seDeconnecter() {
        SessionManager.getInstance().deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) labelCours.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle("PlanifyEdu — Connexion");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // FENÊTRES MODALES
    // =====================================================

    private void ouvrirFenetre(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Passer la référence du dashboard aux sous-contrôleurs
            Object ctrl = loader.getController();
            if (ctrl instanceof CoursController cc)
                cc.setDashboard(this);
            else if (ctrl instanceof PlanningController pc)
                pc.setDashboard(this);
            else if (ctrl instanceof ExamenController ec)
                ec.setDashboard(this);

            Stage stage = new Stage();
            stage.setTitle("PlanifyEdu — " + titre);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur ouverture fenêtre : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
