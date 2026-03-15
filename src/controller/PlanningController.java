package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Conflit;
import model.Cours;
import model.Enseignant;
import model.Groupe;
import model.Planning;
import model.Planning.TypeSession;
import model.Planning.Recurrence;
import model.Salle;
import service.ConflitService;
import service.PlanningService;

import java.util.List;

public class PlanningController {

    // --- Formulaire ---
    @FXML
    private ComboBox<Cours> comboCours;
    @FXML
    private ComboBox<Enseignant> comboEnseignant;
    @FXML
    private ComboBox<Salle> comboSalle;
    @FXML
    private ComboBox<Groupe> comboGroupe;
    @FXML
    private ComboBox<String> comboJour;
    @FXML
    private ComboBox<String> comboHeureDebut;
    @FXML
    private ComboBox<String> comboHeureFin;
    @FXML
    private ComboBox<TypeSession> comboTypeSession;
    @FXML
    private ComboBox<Recurrence> comboRecurrence;

    // --- Tableau ---
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
    private TableColumn<Planning, String> colGroupe;
    @FXML
    private TableColumn<Planning, String> colJour;
    @FXML
    private TableColumn<Planning, String> colHeure;
    @FXML
    private TableColumn<Planning, String> colType;
    @FXML
    private TableColumn<Planning, String> colRecurrence;

    @FXML
    private Label statusLabel;

    private final PlanningService service = new PlanningService();
    private DashboardController dashboard;

    public void setDashboard(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    public void initialize() {
        // --- Colonnes tableau ---
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCours.setCellValueFactory(new PropertyValueFactory<>("coursNom"));
        colEnseignant.setCellValueFactory(new PropertyValueFactory<>("enseignantNom"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salleNom"));
        colGroupe.setCellValueFactory(new PropertyValueFactory<>("groupeNom"));
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeSessionLabel"));
        colRecurrence.setCellValueFactory(new PropertyValueFactory<>("recurrenceLabel"));

        // Colorer les lignes selon le type de session
        tablePlanning.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Planning p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null || p.getTypeSession() == null) {
                    setStyle("");
                } else {
                    String color = switch (p.getTypeSession()) {
                        case CM -> "-fx-background-color: rgba(99,102,241,0.12);"; // indigo
                        case TD -> "-fx-background-color: rgba(16,185,129,0.12);"; // vert
                        case TP -> "-fx-background-color: rgba(245,158,11,0.12);"; // ambre
                    };
                    setStyle(isSelected() ? "" : color);
                }
            }
        });

        // --- Jours ---
        comboJour.setItems(FXCollections.observableArrayList(
                "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"));

        // --- Heures (8h00 → 20h00 par tranches de 30 min) ---
        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int h = 8; h <= 20; h++) {
            heures.add(String.format("%02d:00", h));
            if (h < 20)
                heures.add(String.format("%02d:30", h));
        }
        comboHeureDebut.setItems(heures);
        comboHeureFin.setItems(heures);

        // Pré-sélection auto de heureFin quand heureDebut change
        comboHeureDebut.setOnAction(e -> preselectHeureFin());

        // --- Types de session ---
        comboTypeSession.setItems(FXCollections.observableArrayList(TypeSession.values()));
        comboTypeSession.setValue(TypeSession.CM);

        // --- Récurrences ---
        comboRecurrence.setItems(FXCollections.observableArrayList(Recurrence.values()));
        comboRecurrence.setValue(Recurrence.HEBDOMADAIRE);

        chargerDonnees();
    }

    /** Pré-sélectionne heureFin = heureDebut + 2h automatiquement */
    private void preselectHeureFin() {
        String debut = comboHeureDebut.getValue();
        if (debut == null)
            return;
        try {
            String[] parts = debut.split(":");
            int heure = Integer.parseInt(parts[0]);
            int min = Integer.parseInt(parts[1]);
            heure += 2; // +2 heures par défaut
            if (heure <= 20) {
                comboHeureFin.setValue(String.format("%02d:%02d", heure, min));
            }
        } catch (Exception ignored) {
        }
    }

    private void chargerDonnees() {
        comboCours.setItems(FXCollections.observableArrayList(service.getAllCours()));
        comboEnseignant.setItems(FXCollections.observableArrayList(service.getAllEnseignants()));
        comboSalle.setItems(FXCollections.observableArrayList(service.getAllSalles()));
        comboGroupe.setItems(FXCollections.observableArrayList(service.getAllGroupes()));
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
        String heureDebut = comboHeureDebut.getValue();
        String heureFin = comboHeureFin.getValue();
        TypeSession type = comboTypeSession.getValue();
        Recurrence recurrence = comboRecurrence.getValue();
        Groupe groupe = comboGroupe.getValue();

        // ---- 1. Validation des champs obligatoires ----
        if (cours == null || ens == null || salle == null || jour == null
                || heureDebut == null || heureFin == null) {
            setStatus("[!] Veuillez remplir tous les champs obligatoires.", "error");
            return;
        }

        // ---- 2. Validation de la cohérence horaire ----
        if (heureDebut.compareTo(heureFin) >= 0) {
            setStatus("[!] L'heure de fin doit etre strictement apres l'heure de debut.", "error");
            return;
        }

        int groupeId = (groupe != null) ? groupe.getId() : 0;
        Planning p = new Planning(
                cours.getId(), ens.getId(), salle.getId(), groupeId,
                jour, heureDebut, heureFin, type, recurrence);

        // ---- 3. Détection de conflits ----
        List<Conflit> conflits = service.ajouterPlanningAvecVerification(p);

        if (!conflits.isEmpty()) {
            // Des conflits ont été détectés → afficher une alerte détaillée
            afficherAlerteConflits(conflits);
            setStatus("[X] " + conflits.size() + " conflit(s) detecte(s) - session non enregistree.", "error");
        } else {
            // Succès
            setStatus("[OK] Session planifiee avec succes.", "success");
            resetFormulaire();
            chargerPlannings();
            if (dashboard != null)
                dashboard.refreshStats();
        }
    }

    /**
     * Affiche une boîte de dialogue détaillée listant tous les conflits détectés.
     */
    private void afficherAlerteConflits(List<Conflit> conflits) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("[!] Conflits de Planning Detectes");
        alert.setHeaderText(conflits.size() + " conflit(s) empeche(nt) la planification");

        // Contenu détaillé dans un ScrollPane
        VBox contenu = new VBox(10);
        contenu.setPadding(new Insets(10));

        for (Conflit c : conflits) {
            VBox bloc = new VBox(4);
            bloc.setStyle("-fx-background-color: rgba(239,68,68,0.1); "
                    + "-fx-border-color: rgba(239,68,68,0.4); "
                    + "-fx-border-radius: 6; "
                    + "-fx-background-radius: 6; "
                    + "-fx-padding: 10;");

            // Type de conflit (titre)
            Label titre = new Label(c.getIcone() + "  Conflit " + c.getType().name());
            titre.setFont(Font.font("System", FontWeight.BOLD, 13));

            // Message principal
            Label msg = new Label(c.getMessage());
            msg.setWrapText(true);

            bloc.getChildren().addAll(titre, msg);

            // Créneau conflictuel (si disponible)
            if (c.getCreneauExistant() != null && !c.getCreneauExistant().isEmpty()) {
                Label detail = new Label("↳ Conflit avec : " + c.getCreneauExistant());
                detail.setWrapText(true);
                detail.setStyle("-fx-text-fill: #ef4444; -fx-font-style: italic;");
                bloc.getChildren().add(detail);
            }

            contenu.getChildren().add(bloc);
        }

        // Conseil
        Label conseil = new Label("[i] Choisissez un autre creneau, enseignant ou salle.");
        conseil.setStyle("-fx-font-style: italic; -fx-text-fill: #94a3b8;");
        conseil.setPadding(new Insets(8, 0, 0, 0));
        contenu.getChildren().add(conseil);

        ScrollPane scroll = new ScrollPane(contenu);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(400);
        scroll.setStyle("-fx-background-color: transparent;");

        alert.getDialogPane().setContent(scroll);
        alert.getDialogPane().setPrefWidth(560);
        alert.showAndWait();
    }

    @FXML
    private void supprimerPlanning() {
        Planning selected = tablePlanning.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("[!] Selectionnez une session a supprimer.", "error");
            return;
        }
        if (service.supprimerPlanning(selected.getId())) {
            setStatus("[OK] Session supprimee.", "success");
            chargerPlannings();
            if (dashboard != null)
                dashboard.refreshStats();
        } else {
            setStatus("[X] Impossible de supprimer.", "error");
        }
    }

    private void resetFormulaire() {
        comboCours.setValue(null);
        comboEnseignant.setValue(null);
        comboSalle.setValue(null);
        comboGroupe.setValue(null);
        comboJour.setValue(null);
        comboHeureDebut.setValue(null);
        comboHeureFin.setValue(null);
        comboTypeSession.setValue(TypeSession.CM);
        comboRecurrence.setValue(Recurrence.HEBDOMADAIRE);
    }

    private void setStatus(String msg, String type) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("success-label", "error-label");
        statusLabel.getStyleClass().add(type.equals("success") ? "success-label" : "error-label");
    }
}
