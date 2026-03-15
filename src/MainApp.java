import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.DatabaseConnection;

/**
 * Point d'entrée de PlanifyEdu.
 * Lance l'écran de connexion en premier.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialiser la base de données au démarrage
        DatabaseConnection.getInstance();

        // Démarrer sur l'écran de connexion
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 640);

        primaryStage.setTitle("PlanifyEdu — Connexion");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(500);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Fermer la connexion à la base de données proprement
        DatabaseConnection.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
