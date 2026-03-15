package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Cours;
import model.Examen;
import model.Salle;
import service.PlanningService;

public class ExamenController {

    @FXML
    private ComboBox<Cours> comboCours;
    @FXML
    private ComboBox<Salle> comboSalle;
    @FXML
    private DatePicker dateExamen;
    @FXML
    private ComboBox<String> comboHeure;

    @FXML
    private TableView<Examen> tableExamens;
    @FXML
    private TableColumn<Examen, Integer> colId;
    @FXML
    private TableColumn<Examen, String> colCours;
    @FXML
    private TableColumn<Examen, String> colDate;
    @FXML
    private TableColumn<Examen, String> colHeure;
    @FXML
    private TableColumn<Examen, String> colSalle;

    @FXML
    private Label statusLabel;

    private final PlanningService service = new PlanningService();
    private DashboardController dashboard;

    public void setDashboard(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCours.setCellValueFactory(new PropertyValueFactory<>("coursNom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salleNom"));

        // Heures
        var heures = FXCollections.<String>observableArrayList();
        for (int h = 8; h <= 19; h++) {
            heures.add(String.format("%02d:00", h));
            heures.add(String.format("%02d:30", h));
        }
        comboHeure.setItems(heures);

        chargerDonnees();
    }

    private void chargerDonnees() {
        comboCours.setItems(FXCollections.observableArrayList(service.getAllCours()));
        comboSalle.setItems(FXCollections.observableArrayList(service.getAllSalles()));
        chargerExamens();
    }

    private void chargerExamens() {
        tableExamens.setItems(FXCollections.observableArrayList(service.getAllExamens()));
    }

    @FXML
    private void ajouterExamen() {
        Cours cours = comboCours.getValue();
        Salle salle = comboSalle.getValue();
        String heure = comboHeure.getValue();

        if (cours == null || salle == null || dateExamen.getValue() == null || heure == null) {
            setStatus("⚠ Veuillez remplir tous les champs.", "error");
            return;
        }

        String dateStr = dateExamen.getValue().toString();
        Examen ex = new Examen(cours.getId(), dateStr, heure, salle.getId());

        if (service.ajouterExamen(ex)) {
            setStatus("✔ Examen planifié avec succès.", "success");
            comboCours.setValue(null);
            comboSalle.setValue(null);
            dateExamen.setValue(null);
            comboHeure.setValue(null);
            chargerExamens();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("✘ Erreur lors de la planification.", "error");
        }
    }

    @FXML
    private void supprimerExamen() {
        Examen selected = tableExamens.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("⚠ Sélectionnez un examen à supprimer.", "error");
            return;
        }
        if (service.supprimerExamen(selected.getId())) {
            setStatus("✔ Examen supprimé.", "success");
            chargerExamens();
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
