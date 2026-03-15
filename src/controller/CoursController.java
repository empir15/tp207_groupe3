package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Cours;
import model.Enseignant;
import model.Salle;
import service.PlanningService;

public class CoursController {

    // ---- Onglet Cours ----
    @FXML
    private TextField fieldNomCours;
    @FXML
    private TextArea fieldDescCours;
    @FXML
    private TableView<Cours> tableCours;
    @FXML
    private TableColumn<Cours, Integer> colCoursId;
    @FXML
    private TableColumn<Cours, String> colCoursNom;
    @FXML
    private TableColumn<Cours, String> colCoursDesc;

    // ---- Onglet Enseignants ----
    @FXML
    private TextField fieldNomEns;
    @FXML
    private TextField fieldPrenomEns;
    @FXML
    private TextField fieldEmailEns;
    @FXML
    private TableView<Enseignant> tableEnseignants;
    @FXML
    private TableColumn<Enseignant, Integer> colEnsId;
    @FXML
    private TableColumn<Enseignant, String> colEnsNom;
    @FXML
    private TableColumn<Enseignant, String> colEnsPrenom;
    @FXML
    private TableColumn<Enseignant, String> colEnsEmail;

    // ---- Onglet Salles ----
    @FXML
    private TextField fieldNomSalle;
    @FXML
    private TextField fieldCapacite;
    @FXML
    private TableView<Salle> tableSalles;
    @FXML
    private TableColumn<Salle, Integer> colSalleId;
    @FXML
    private TableColumn<Salle, String> colSalleNom;
    @FXML
    private TableColumn<Salle, Integer> colSalleCap;

    @FXML
    private Label statusLabel;

    private final PlanningService service = new PlanningService();
    private DashboardController dashboard;

    public void setDashboard(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    public void initialize() {
        // Cours
        colCoursId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCoursNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCoursDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Enseignants
        colEnsId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEnsNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEnsPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEnsEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Salles
        colSalleId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSalleNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSalleCap.setCellValueFactory(new PropertyValueFactory<>("capacite"));

        chargerTout();
    }

    private void chargerTout() {
        chargerCours();
        chargerEnseignants();
        chargerSalles();
    }

    private void chargerCours() {
        ObservableList<Cours> data = FXCollections.observableArrayList(service.getAllCours());
        tableCours.setItems(data);
    }

    private void chargerEnseignants() {
        ObservableList<Enseignant> data = FXCollections.observableArrayList(service.getAllEnseignants());
        tableEnseignants.setItems(data);
    }

    private void chargerSalles() {
        ObservableList<Salle> data = FXCollections.observableArrayList(service.getAllSalles());
        tableSalles.setItems(data);
    }

    @FXML
    private void ajouterCours() {
        String nom = fieldNomCours.getText().trim();
        String desc = fieldDescCours.getText().trim();
        if (nom.isEmpty()) {
            setStatus("⚠ Veuillez saisir un nom de cours.", "error");
            return;
        }
        if (service.ajouterCours(new Cours(nom, desc))) {
            setStatus("✔ Cours ajouté avec succès.", "success");
            fieldNomCours.clear();
            fieldDescCours.clear();
            chargerCours();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("✘ Erreur lors de l'ajout.", "error");
        }
    }

    @FXML
    private void supprimerCours() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("⚠ Sélectionnez un cours à supprimer.", "error");
            return;
        }
        if (service.supprimerCours(selected.getId())) {
            setStatus("✔ Cours supprimé.", "success");
            chargerCours();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("✘ Impossible de supprimer.", "error");
        }
    }

    @FXML
    private void ajouterEnseignant() {
        String nom = fieldNomEns.getText().trim();
        String prenom = fieldPrenomEns.getText().trim();
        String email = fieldEmailEns.getText().trim();
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            setStatus("⚠ Tous les champs enseignant sont requis.", "error");
            return;
        }
        if (service.ajouterEnseignant(new Enseignant(nom, prenom, email))) {
            setStatus("✔ Enseignant ajouté avec succès.", "success");
            fieldNomEns.clear();
            fieldPrenomEns.clear();
            fieldEmailEns.clear();
            chargerEnseignants();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("✘ Erreur lors de l'ajout.", "error");
        }
    }

    @FXML
    private void supprimerEnseignant() {
        Enseignant selected = tableEnseignants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("⚠ Sélectionnez un enseignant à supprimer.", "error");
            return;
        }
        if (service.supprimerEnseignant(selected.getId())) {
            setStatus("✔ Enseignant supprimé.", "success");
            chargerEnseignants();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("✘ Impossible de supprimer.", "error");
        }
    }

    @FXML
    private void ajouterSalle() {
        String nom = fieldNomSalle.getText().trim();
        String capStr = fieldCapacite.getText().trim();
        if (nom.isEmpty() || capStr.isEmpty()) {
            setStatus("⚠ Tous les champs salle sont requis.", "error");
            return;
        }
        try {
            int cap = Integer.parseInt(capStr);
            if (service.ajouterSalle(new Salle(nom, cap))) {
                setStatus("✔ Salle ajoutée avec succès.", "success");
                fieldNomSalle.clear();
                fieldCapacite.clear();
                chargerSalles();
                if (dashboard != null)
                    dashboard.refreshStats();
            } else {
                setStatus("✘ Erreur lors de l'ajout.", "error");
            }
        } catch (NumberFormatException e) {
            setStatus("⚠ La capacité doit être un nombre entier.", "error");
        }
    }

    @FXML
    private void supprimerSalle() {
        Salle selected = tableSalles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("⚠ Sélectionnez une salle à supprimer.", "error");
            return;
        }
        if (service.supprimerSalle(selected.getId())) {
            setStatus("✔ Salle supprimée.", "success");
            chargerSalles();
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
