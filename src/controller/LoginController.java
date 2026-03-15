package controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.AuthService;

public class LoginController {

    @FXML
    private TextField fieldLogin;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Label labelErreur;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Focus automatique sur le champ login
        javafx.application.Platform.runLater(() -> fieldLogin.requestFocus());
    }

    /** Gère la touche Entrée sur les champs de formulaire */
    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            seConnecter();
        }
    }

    /** Tentative de connexion */
    @FXML
    private void seConnecter() {
        String login = fieldLogin.getText();
        String mdp = fieldPassword.getText();

        // Validation basique
        if (login.isBlank() || mdp.isBlank()) {
            afficherErreur("Veuillez saisir votre identifiant et votre mot de passe.");
            return;
        }

        if (authService.connecter(login, mdp)) {
            // Connexion réussie → ouvrir le Dashboard
            ouvrirDashboard();
        } else {
            afficherErreur("Identifiant ou mot de passe incorrect.");
            secouerFormulaire();
            fieldPassword.clear();
            fieldPassword.requestFocus();
        }
    }

    /** Affiche / met à jour le message d'erreur */
    private void afficherErreur(String message) {
        labelErreur.setText("⚠  " + message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    /** Animation "shake" sur le champ mot de passe en cas d'erreur */
    private void secouerFormulaire() {
        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(fieldPassword.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(60), new KeyValue(fieldPassword.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(120), new KeyValue(fieldPassword.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(180), new KeyValue(fieldPassword.translateXProperty(), -8)),
                new KeyFrame(Duration.millis(240), new KeyValue(fieldPassword.translateXProperty(), 8)),
                new KeyFrame(Duration.millis(300), new KeyValue(fieldPassword.translateXProperty(), 0)));
        shake.play();
    }

    /** Ouvre le Dashboard en remplaçant la scène de connexion */
    private void ouvrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) fieldLogin.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 780);
            stage.setScene(scene);
            stage.setTitle("PlanifyEdu — Tableau de bord");
            stage.centerOnScreen();
        } catch (Exception e) {
            afficherErreur("Erreur lors du chargement de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
