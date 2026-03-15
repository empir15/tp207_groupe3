package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseConnection {

    private static final String DB_DIR = "database";
    private static final String DB_PATH = DB_DIR + File.separator + "planning.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        initDatabase();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void initDatabase() {
        // Créer le répertoire si nécessaire
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL);
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite non trouvé : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {

        // --- Tables de ressources (inchangées) ---
        String createCourses = """
                    CREATE TABLE IF NOT EXISTS courses (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom         TEXT NOT NULL,
                        description TEXT
                    );
                """;

        String createTeachers = """
                    CREATE TABLE IF NOT EXISTS teachers (
                        id      INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom     TEXT NOT NULL,
                        prenom  TEXT NOT NULL,
                        email   TEXT NOT NULL
                    );
                """;

        String createClassrooms = """
                    CREATE TABLE IF NOT EXISTS classrooms (
                        id       INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom      TEXT NOT NULL,
                        capacite INTEGER NOT NULL
                    );
                """;

        // --- NOUVEAU : Table des groupes d'étudiants ---
        String createGroupes = """
                    CREATE TABLE IF NOT EXISTS groupes (
                        id            INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom           TEXT NOT NULL,
                        niveau        TEXT NOT NULL,
                        annee_scolaire TEXT NOT NULL
                    );
                """;

        // --- Table planning enrichie ---
        String createPlanning = """
                    CREATE TABLE IF NOT EXISTS planning (
                        id           INTEGER PRIMARY KEY AUTOINCREMENT,
                        course_id    INTEGER NOT NULL,
                        teacher_id   INTEGER NOT NULL,
                        classroom_id INTEGER NOT NULL,
                        groupe_id    INTEGER,
                        jour         TEXT NOT NULL,
                        heure_debut  TEXT NOT NULL,
                        heure_fin    TEXT NOT NULL,
                        type_session TEXT NOT NULL DEFAULT 'CM',
                        recurrence   TEXT NOT NULL DEFAULT 'HEBDOMADAIRE',
                        FOREIGN KEY (course_id)    REFERENCES courses(id),
                        FOREIGN KEY (teacher_id)   REFERENCES teachers(id),
                        FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
                        FOREIGN KEY (groupe_id)    REFERENCES groupes(id)
                    );
                """;

        String createExams = """
                    CREATE TABLE IF NOT EXISTS exams (
                        id           INTEGER PRIMARY KEY AUTOINCREMENT,
                        course_id    INTEGER NOT NULL,
                        date         TEXT NOT NULL,
                        heure        TEXT NOT NULL,
                        classroom_id INTEGER NOT NULL,
                        FOREIGN KEY (course_id)    REFERENCES courses(id),
                        FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
                    );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCourses);
            stmt.execute(createTeachers);
            stmt.execute(createClassrooms);
            stmt.execute(createGroupes);
            stmt.execute(createPlanning);
            stmt.execute(createExams);
        }

        // Migration douce : ajouter les colonnes si la BDD existait déjà
        migrateIfNeeded();
    }

    /**
     * Migration douce pour les bases existantes.
     * SQLite ne supporte pas ALTER COLUMN, mais supporte ADD COLUMN.
     * On tente d'ajouter les nouvelles colonnes ; si elles existent déjà,
     * l'exception est silencieusement ignorée.
     */
    private void migrateIfNeeded() {
        String[] migrations = {
                "ALTER TABLE planning ADD COLUMN groupe_id    INTEGER",
                "ALTER TABLE planning ADD COLUMN heure_debut  TEXT NOT NULL DEFAULT '08:00'",
                "ALTER TABLE planning ADD COLUMN heure_fin    TEXT NOT NULL DEFAULT '10:00'",
                "ALTER TABLE planning ADD COLUMN type_session TEXT NOT NULL DEFAULT 'CM'",
                "ALTER TABLE planning ADD COLUMN recurrence   TEXT NOT NULL DEFAULT 'HEBDOMADAIRE'"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : migrations) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ignored) {
                    // Colonne déjà existante — normal lors des lancements suivants
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur de migration : " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de reconnexion : " + e.getMessage());
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture : " + e.getMessage());
        }
    }
}
