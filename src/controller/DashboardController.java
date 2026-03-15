package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.PlanningService;

public class DashboardController {

    @FXML
    private Label labelCours;
    @FXML
    private Label labelEnseignants;
    @FXML
    private Label labelSalles;
    @FXML
    private Label labelPlannings;
    @FXML
    private Label labelExamens;

    private final PlanningService service = new PlanningService();

    @FXML
    public void initialize() {
        refreshStats();
    }

    public void refreshStats() {
        labelCours.setText(String.valueOf(service.countCours()));
        labelEnseignants.setText(String.valueOf(service.countEnseignants()));
        labelSalles.setText(String.valueOf(service.countSalles()));
        labelPlannings.setText(String.valueOf(service.countPlannings()));
        labelExamens.setText(String.valueOf(service.countExamens()));
    }

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

    private void ouvrirFenetre(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Passer la référence du dashboard pour rafraîchir les stats
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
