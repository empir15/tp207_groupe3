package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Groupe;
import model.Planning;
import service.GroupeService;
import service.PlanningService;

import java.util.*;

/**
 * Contrôleur du calendrier hebdomadaire.
 *
 * <p>
 * Construit dynamiquement une grille de planning semainier dans un GridPane.
 * Chaque matière reçoit une couleur unique de la palette prédéfinie.
 * Les sessions sont placées dans les bonnes cellules avec rowSpan calculé
 * depuis heure_debut et heure_fin.
 * </p>
 */
public class CalendrierController {

    // ---- FXML Injections ----
    @FXML
    private ComboBox<String> comboFiltre;
    @FXML
    private ComboBox<Groupe> comboGroupe;
    @FXML
    private ScrollPane scrollCalendrier;
    @FXML
    private HBox legendeBox;
    @FXML
    private Label labelInfo;

    // ---- Services ----
    private final PlanningService planningService = new PlanningService();
    private final GroupeService groupeService = new GroupeService();

    // ---- Constantes de la grille ----
    private static final String[] JOURS = { "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" };
    private static final int HEURE_DEBUT = 8;
    private static final int HEURE_FIN = 20;
    private static final int SLOTS = (HEURE_FIN - HEURE_DEBUT) * 2; // 24 créneaux de 30 min
    private static final double SLOT_HEIGHT = 46.0;
    private static final double COL_WIDTH = 175.0;
    private static final double TIME_COL_W = 65.0;

    /**
     * Palette de couleurs "dark mode" harmonieuse pour les matières.
     * Chaque cours reçoit une couleur selon : PALETTE[courseId % PALETTE.length]
     */
    private static final String[] PALETTE = {
            "#6366f1", // indigo
            "#8b5cf6", // violet
            "#ec4899", // rose
            "#ef4444", // rouge
            "#f97316", // orange
            "#eab308", // jaune
            "#10b981", // émeraude
            "#14b8a6", // teal
            "#3b82f6", // bleu
            "#06b6d4", // cyan
            "#a855f7", // purple
            "#f43f5e", // rose foncé
            "#84cc16", // lime
            "#0ea5e9", // bleu ciel
            "#d946ef" // fuchsia
    };

    // ---- Mapping courseId → couleur ----
    private final Map<Integer, String> courseColors = new LinkedHashMap<>();

    // ========================================================
    // INITIALISATION
    // ========================================================

    @FXML
    public void initialize() {
        // Options de filtre
        comboFiltre.setItems(FXCollections.observableArrayList(
                "Tout le planning", "Par groupe"));
        comboFiltre.setValue("Tout le planning");

        // Groupes disponibles
        comboGroupe.setItems(FXCollections.observableArrayList(groupeService.getAll()));
        comboGroupe.setPromptText("Choisir un groupe...");
        comboGroupe.setDisable(true);

        // Réagir au changement de filtre
        comboFiltre.setOnAction(e -> {
            boolean parGroupe = "Par groupe".equals(comboFiltre.getValue());
            comboGroupe.setDisable(!parGroupe);
            rafraichir();
        });

        comboGroupe.setOnAction(e -> rafraichir());

        // Premier chargement
        rafraichir();
    }

    // ========================================================
    // CHARGEMENT & FILTRAGE
    // ========================================================

    @FXML
    public void rafraichir() {
        List<Planning> sessions = planningService.getAllPlannings();

        // Filtre par groupe si sélectionné
        if ("Par groupe".equals(comboFiltre.getValue()) && comboGroupe.getValue() != null) {
            int groupeId = comboGroupe.getValue().getId();
            sessions = sessions.stream()
                    .filter(p -> p.getGroupeId() == groupeId)
                    .toList();
        }

        // Réassigner les couleurs (déterministe par courseId)
        courseColors.clear();
        for (Planning p : sessions) {
            courseColors.computeIfAbsent(
                    p.getCourseId(),
                    id -> PALETTE[Math.abs(id % PALETTE.length)]);
        }

        construireCalendrier(sessions);

        // Mise à jour du label d'info
        if (labelInfo != null) {
            labelInfo.setText(sessions.size() + " session(s) affichée(s)");
        }
    }

    // ========================================================
    // CONSTRUCTION DE LA GRILLE
    // ========================================================

    private void construireCalendrier(List<Planning> sessions) {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color: #0f1117;");
        grid.setHgap(0);
        grid.setVgap(0);

        // ---- Colonnes ----
        ColumnConstraints timeCC = new ColumnConstraints(TIME_COL_W);
        timeCC.setMinWidth(TIME_COL_W);
        grid.getColumnConstraints().add(timeCC);

        for (int j = 0; j < JOURS.length; j++) {
            ColumnConstraints dayCC = new ColumnConstraints(COL_WIDTH);
            dayCC.setHgrow(Priority.ALWAYS);
            dayCC.setMinWidth(COL_WIDTH);
            grid.getColumnConstraints().add(dayCC);
        }

        // ---- Lignes ----
        RowConstraints headerRC = new RowConstraints(50);
        grid.getRowConstraints().add(headerRC);
        for (int s = 0; s < SLOTS; s++) {
            RowConstraints slotRC = new RowConstraints(SLOT_HEIGHT);
            slotRC.setMinHeight(SLOT_HEIGHT);
            grid.getRowConstraints().add(slotRC);
        }

        // ---- En-tête colonne "Heure" ----
        Label timeHeader = new Label("Horaire");
        styleTimeLabelHeader(timeHeader);
        grid.add(timeHeader, 0, 0);

        // ---- En-têtes des jours ----
        for (int j = 0; j < JOURS.length; j++) {
            grid.add(createDayHeader(JOURS[j]), j + 1, 0);
        }

        // ---- Cellules de fond (labels horaires + fond des cases) ----
        for (int s = 0; s < SLOTS; s++) {
            int hour = HEURE_DEBUT + s / 2;
            int min = (s % 2) * 30;
            boolean isHourMark = (min == 0);
            String timeStr = String.format("%02d:%02d", hour, min);

            // Label de l'heure
            Label timeLbl = new Label(isHourMark ? timeStr : "");
            styleTimeCell(timeLbl, isHourMark);
            grid.add(timeLbl, 0, s + 1);

            // Cellule vide pour chaque jour
            for (int j = 0; j < JOURS.length; j++) {
                Pane cell = new Pane();
                styleDayCell(cell, s);
                grid.add(cell, j + 1, s + 1);
            }
        }

        // ---- Regrouper les sessions par jour ----
        Map<String, List<Planning>> byDay = new HashMap<>();
        for (String jour : JOURS)
            byDay.put(jour, new ArrayList<>());
        for (Planning p : sessions) {
            if (byDay.containsKey(p.getJour())) {
                byDay.get(p.getJour()).add(p);
            }
        }

        // ---- Placer les blocs de sessions ----
        for (int j = 0; j < JOURS.length; j++) {
            for (Planning p : byDay.get(JOURS[j])) {
                int startRow = timeToRow(p.getHeureDebut());
                int endRow = timeToRow(p.getHeureFin());
                int rowSpan = Math.max(1, endRow - startRow);

                if (startRow < 1 || startRow > SLOTS)
                    continue;
                if (startRow + rowSpan - 1 > SLOTS) {
                    rowSpan = SLOTS - startRow + 1;
                }

                String color = courseColors.getOrDefault(p.getCourseId(), "#6366f1");
                VBox block = createSessionBlock(p, color, rowSpan);
                GridPane.setRowSpan(block, rowSpan);
                GridPane.setMargin(block, new Insets(2, 3, 2, 3));
                grid.add(block, j + 1, startRow);
            }
        }

        // ---- Injecter dans le ScrollPane ----
        scrollCalendrier.setContent(grid);

        // ---- Construire la légende des matières ----
        construireLegendeCours(sessions);
    }

    // ========================================================
    // CRÉATION DES ÉLÉMENTS VISUELS
    // ========================================================

    /** En-tête de colonne jour (Lundi, Mardi, ...) */
    private Label createDayHeader(String jour) {
        Label lbl = new Label(jour.toUpperCase());
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setMaxHeight(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle(
                "-fx-text-fill: #cbd5e1; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: #1a2030; " +
                        "-fx-border-color: #374151 #374151 #4b5563 #374151; " +
                        "-fx-border-width: 0 1 1 1; " +
                        "-fx-padding: 14 8 14 8;");
        return lbl;
    }

    /** En-tête de la colonne "Horaire" */
    private void styleTimeLabelHeader(Label lbl) {
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setMaxHeight(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle(
                "-fx-text-fill: #64748b; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: #1a2030; " +
                        "-fx-border-color: #374151 #4b5563 #4b5563 transparent; " +
                        "-fx-border-width: 0 1 1 0; " +
                        "-fx-padding: 14 8 14 8;");
    }

    /** Cellule de l'heure (colonne temps) */
    private void styleTimeCell(Label lbl, boolean isHourMark) {
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setMaxHeight(Double.MAX_VALUE);
        lbl.setAlignment(Pos.TOP_CENTER);
        lbl.setStyle(
                "-fx-text-fill: " + (isHourMark ? "#64748b" : "#374151") + "; " +
                        "-fx-font-size: 10px; " +
                        "-fx-background-color: #151c28; " +
                        "-fx-border-color: transparent #4b5563 " + (isHourMark ? "#374151" : "#1e2433")
                        + " transparent; " +
                        "-fx-border-width: 0 1 1 0; " +
                        "-fx-padding: 4 6 0 6;");
    }

    /** Cellule de fond d'un jour (alternance paire/impaire) */
    private void styleDayCell(Pane cell, int slotIndex) {
        boolean isHourMark = (slotIndex % 2 == 0);
        cell.setStyle(
                "-fx-background-color: " + (isHourMark ? "#0f1117" : "#111827") + "; " +
                        "-fx-border-color: transparent transparent " + (isHourMark ? "#2d3748" : "#1e2433")
                        + " #2d3748; " +
                        "-fx-border-width: 0 0 1 1;");
    }

    /**
     * Crée un bloc coloré représentant une session dans la grille.
     *
     * @param p       la session Planning
     * @param color   couleur hex de la matière
     * @param rowSpan nombre de slots de 30min que la session occupe
     */
    private VBox createSessionBlock(Planning p, String color, int rowSpan) {
        VBox block = new VBox(2);
        block.setMaxWidth(Double.MAX_VALUE);
        block.setMaxHeight(Double.MAX_VALUE);
        block.setFillWidth(true);

        // Couleur de fond semi-transparente + bordure gauche colorée
        String rgba = hexToRgba(color, 0.20);
        String hoverRgba = hexToRgba(color, 0.35);

        block.setStyle(
                "-fx-background-color: " + rgba + "; " +
                        "-fx-border-color: " + color + " transparent transparent transparent; " +
                        "-fx-border-width: 0 0 0 3; " +
                        "-fx-border-radius: 0 4 4 0; " +
                        "-fx-background-radius: 0 4 4 0; " +
                        "-fx-padding: 5 7 5 9; " +
                        "-fx-cursor: hand;");

        // --- Badge type (CM / TD / TP) ---
        String typeStr = p.getTypeSession() != null ? p.getTypeSession().name() : "";
        String typeBgColor = switch (typeStr) {
            case "CM" -> "rgba(99,102,241,0.35)";
            case "TD" -> "rgba(16,185,129,0.35)";
            case "TP" -> "rgba(245,158,11,0.35)";
            default -> "rgba(100,116,139,0.35)";
        };
        String typeTextColor = switch (typeStr) {
            case "CM" -> "#818cf8";
            case "TD" -> "#34d399";
            case "TP" -> "#fbbf24";
            default -> "#94a3b8";
        };

        // --- Nom du cours (toujours visible) ---
        HBox headerRow = new HBox(5);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        if (!typeStr.isEmpty()) {
            Label typeBadge = new Label(typeStr);
            typeBadge.setStyle(
                    "-fx-background-color: " + typeBgColor + "; " +
                            "-fx-text-fill: " + typeTextColor + "; " +
                            "-fx-font-size: 9px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 3; " +
                            "-fx-padding: 1 5 1 5;");
            headerRow.getChildren().add(typeBadge);
        }

        Label coursLabel = new Label(p.getCoursNom() != null ? p.getCoursNom() : "");
        coursLabel.setStyle(
                "-fx-text-fill: " + color + "; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;");
        coursLabel.setWrapText(true);
        coursLabel.setMaxWidth(Double.MAX_VALUE);
        headerRow.getChildren().add(coursLabel);
        HBox.setHgrow(coursLabel, Priority.ALWAYS);

        block.getChildren().add(headerRow);

        // --- Détails additionnels (si assez de place : rowSpan >= 2) ---
        if (rowSpan >= 2) {
            Label ensLabel = new Label("👤 " + (p.getEnseignantNom() != null ? p.getEnseignantNom() : ""));
            ensLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
            ensLabel.setWrapText(true);
            block.getChildren().add(ensLabel);

            Label salleLabel = new Label("[Salle] " + (p.getSalleNom() != null ? p.getSalleNom() : ""));
            salleLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
            block.getChildren().add(salleLabel);
        }

        // --- Groupe si disponible et si assez de place ---
        if (rowSpan >= 3 && p.getGroupeNom() != null && !p.getGroupeNom().equals("—")) {
            Label grpLabel = new Label("👥 " + p.getGroupeNom());
            grpLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
            block.getChildren().add(grpLabel);
        }

        // --- Tooltip complet (toujours visible au survol) ---
        String tooltipText = buildTooltip(p);
        Tooltip tip = new Tooltip(tooltipText);
        tip.setWrapText(true);
        tip.setPrefWidth(280);
        tip.setStyle(
                "-fx-background-color: #1e2433; " +
                        "-fx-text-fill: #e2e8f0; " +
                        "-fx-font-size: 12px; " +
                        "-fx-border-color: " + color + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 10;");
        Tooltip.install(block, tip);

        // --- Effet hover ---
        final String baseStyle = block.getStyle();
        final String hoverStyle = baseStyle.replace(rgba, hoverRgba);
        block.setOnMouseEntered(e -> block.setStyle(hoverStyle));
        block.setOnMouseExited(e -> block.setStyle(baseStyle));

        return block;
    }

    /** Construit le texte du tooltip d'une session */
    private String buildTooltip(Planning p) {
        StringBuilder sb = new StringBuilder();
        sb.append("📚 ").append(p.getCoursNom() != null ? p.getCoursNom() : "N/A").append("\n");
        if (p.getTypeSession() != null)
            sb.append("  Type  : ").append(p.getTypeSession().name()).append("\n");
        sb.append("📅 ").append(p.getJour()).append("  ").append(p.getHeureDebut())
                .append(" → ").append(p.getHeureFin()).append("\n");
        if (p.getEnseignantNom() != null)
            sb.append("👤 ").append(p.getEnseignantNom()).append("\n");
        if (p.getSalleNom() != null)
            sb.append("[Salle] ").append(p.getSalleNom()).append("\n");
        if (p.getGroupeNom() != null && !p.getGroupeNom().equals("—"))
            sb.append("👥 ").append(p.getGroupeNom()).append("\n");
        if (p.getRecurrence() != null)
            sb.append("[Recurrence] ").append(p.getRecurrenceLabel());
        return sb.toString().trim();
    }

    // ========================================================
    // LÉGENDE DES MATIÈRES
    // ========================================================

    private void construireLegendeCours(List<Planning> sessions) {
        legendeBox.getChildren().clear();

        Label title = new Label("Matières : ");
        title.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
        legendeBox.getChildren().add(title);

        Set<Integer> seen = new LinkedHashSet<>();
        for (Planning p : sessions) {
            if (!seen.add(p.getCourseId()))
                continue;

            String color = courseColors.getOrDefault(p.getCourseId(), "#6366f1");

            // Point coloré
            Pane dot = new Pane();
            dot.setPrefSize(10, 10);
            dot.setMinSize(10, 10);
            dot.setMaxSize(10, 10);
            dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");

            Label name = new Label(p.getCoursNom() != null ? p.getCoursNom() : "?");
            name.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

            HBox item = new HBox(5, dot, name);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.2); " +
                            "-fx-background-radius: 4; " +
                            "-fx-border-color: " + color + "; " +
                            "-fx-border-radius: 4; " +
                            "-fx-border-width: 1; " +
                            "-fx-padding: 3 8 3 6;");

            legendeBox.getChildren().add(item);
        }

        if (seen.isEmpty()) {
            Label empty = new Label("Aucune session dans cette vue.");
            empty.setStyle("-fx-text-fill: #475569; -fx-font-style: italic; -fx-font-size: 11px;");
            legendeBox.getChildren().add(empty);
        }
    }

    // ========================================================
    // UTILITAIRES
    // ========================================================

    /**
     * Convertit "HH:MM" en index de ligne dans la grille.
     * Ligne 1 = 08:00, Ligne 2 = 08:30, etc.
     */
    private int timeToRow(String time) {
        if (time == null || time.isBlank())
            return -1;
        try {
            String[] parts = time.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            return ((h - HEURE_DEBUT) * 2 + (m / 30)) + 1;
        } catch (Exception e) {
            System.err.println("Erreur parsing heure : " + time);
            return -1;
        }
    }

    /**
     * Convertit une couleur hexadécimale en rgba() CSS avec opacité.
     */
    private String hexToRgba(String hex, double alpha) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return String.format("rgba(%d,%d,%d,%.2f)", r, g, b, alpha);
    }
}
