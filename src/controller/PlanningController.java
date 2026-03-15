package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Cours;
import model.Enseignant;
import model.Planning;
import model.Salle;
import service.PlanningService;

public class PlanningController {

    @FXML
    private ComboBox<Cours> comboCours;
    @FXML
    private ComboBox<Enseignant> comboEnseignant;
    @FXML
    private ComboBox<Salle> comboSalle;
    @FXML
    private ComboBox<String> comboJour;
    @FXML
    private ComboBox<String> comboHeure;

    @FXML
    private TableView<Planning> tablePlanning;
    @FXML
    private TableColumn<Planning, Integer> colId;
    @FXML
    private TableColumn<Planning, String> colCours;
    @FXML
    private TableColumn<Planning, String> colEnseignant;
    @FXML
    private TableColumn<Planning, String> colSalle;
    @FXML
    private TableColumn<Planning, String> colJour;
    @FXML
    private TableColumn<Planning, String> colHeure;

    @FXML
    private Label statusLabel;

    private final PlanningService service = new PlanningService();
    private DashboardController dashboard;

    public void setDashboard(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    public void initialize() {
        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCours.setCellValueFactory(new PropertyValueFactory<>("coursNom"));
        colEnseignant.setCellValueFactory(new PropertyValueFactory<>("enseignantNom"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salleNom"));
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));

        // Jours
        comboJour.setItems(FXCollections.observableArrayList(
                "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"));

        // Heures
        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int h = 8; h <= 19; h++) {
            heures.add(String.format("%02d:00", h));
            heures.add(String.format("%02d:30", h));
        }
        comboHeure.setItems(heures);

        chargerDonnees();
    }

    private void chargerDonnees() {
        comboCours.setItems(FXCollections.observableArrayList(service.getAllCours()));
        comboEnseignant.setItems(FXCollections.observableArrayList(service.getAllEnseignants()));
        comboSalle.setItems(FXCollections.observableArrayList(service.getAllSalles()));
        chargerPlannings();
    }

    private void chargerPlannings() {
        tablePlanning.setItems(FXCollections.observableArrayList(service.getAllPlannings()));
    }

    @FXML
    private void ajouterPlanning() {
        Cours cours = comboCours.getValue();
        Enseignant ens = comboEnseignant.getValue();
        Salle salle = comboSalle.getValue();
        String jour = comboJour.getValue();
        String heure = comboHeure.getValue();

        if (cours == null || ens == null || salle == null || jour == null || heure == null) {
            setStatus("⚠ Veuillez remplir tous les champs.", "error");
            return;
        }

        Planning p = new Planning(cours.getId(), ens.getId(), salle.getId(), jour, heure);
        if (service.ajouterPlanning(p)) {
            setStatus("✔ Horaire créé avec succès.", "success");
            comboCours.setValue(null);
            comboEnseignant.setValue(null);
            comboSalle.setValue(null);
            comboJour.setValue(null);
            comboHeure.setValue(null);
            chargerPlannings();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("✘ Erreur lors de la création.", "error");
        }
    }

    @FXML
    private void supprimerPlanning() {
        Planning selected = tablePlanning.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("⚠ Sélectionnez un horaire à supprimer.", "error");
            return;
        }
        if (service.supprimerPlanning(selected.getId())) {
            setStatus("✔ Horaire supprimé.", "success");
            chargerPlannings();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("✘ Impossible de supprimer.", "error");
        }
    }

    private void setStatus(String msg, String type) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("success-label", "error-label");
        statusLabel.getStyleClass().add(type.equals("success") ? "success-label" : "error-label");
    }
}
