package service;

import model.Planning;
import model.Planning.TypeSession;
import util.SessionManager;

import java.awt.Desktop;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service d'export du planning en HTML.
 *
 * <p>
 * Génère un fichier HTML complet et stylisé (dark mode) puis
 * l'ouvre dans le navigateur par défaut. L'utilisateur peut
 * ensuite l'imprimer en PDF via <code>Ctrl+P → Enregistrer en PDF</code>.
 * </p>
 *
 * <p>
 * Aucune dépendance externe requise.
 * </p>
 */
public class ExportService {

    /** Jours de la semaine affichés dans l'ordre */
    private static final String[] JOURS = {
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"
    };

    /**
     * Palette de couleurs (même que CalendrierController, index = courseId % 15)
     */
    private static final String[] PALETTE = {
            "#6366f1", "#8b5cf6", "#ec4899", "#ef4444", "#f97316",
            "#eab308", "#10b981", "#14b8a6", "#3b82f6", "#06b6d4",
            "#a855f7", "#f43f5e", "#84cc16", "#0ea5e9", "#d946ef"
    };

    // =====================================================
    // POINT D'ENTRÉE PRINCIPAL
    // =====================================================

    /**
     * Exporte la liste de sessions en HTML et l'ouvre dans le navigateur.
     *
     * @param sessions     liste des sessions à exporter
     * @param titreFichier titre affiché dans le document
     * @return true si l'ouverture a réussi
     */
    public boolean exporterHtml(List<Planning> sessions, String titreFichier) {
        try {
            // Dossier temporaire
            File fichier = File.createTempFile("planifyedu_export_", ".html");
            fichier.deleteOnExit();

            try (PrintWriter pw = new PrintWriter(fichier, "UTF-8")) {
                pw.print(genererHtml(sessions, titreFichier));
            }

            // Ouvrir dans le navigateur
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(fichier.toURI());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Erreur export HTML : " + e.getMessage());
        }
        return false;
    }

    // =====================================================
    // GÉNÉRATION DU HTML
    // =====================================================

    private String genererHtml(List<Planning> sessions, String titre) {
        String dateExport = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String utilisateur = SessionManager.getInstance().getAffichageComplet();

        // Regrouper par jour
        Map<String, List<Planning>> parJour = new LinkedHashMap<>();
        for (String jour : JOURS)
            parJour.put(jour, new ArrayList<>());
        for (Planning p : sessions) {
            if (parJour.containsKey(p.getJour())) {
                parJour.get(p.getJour()).add(p);
            }
        }
        // Trier chaque jour par heure de début
        for (List<Planning> liste : parJour.values()) {
            liste.sort(Comparator.comparing(p -> p.getHeureDebut() != null ? p.getHeureDebut() : ""));
        }

        // Couleurs des cours
        Map<Integer, String> courseColors = new HashMap<>();
        for (Planning p : sessions) {
            courseColors.computeIfAbsent(p.getCourseId(),
                    id -> PALETTE[Math.abs(id % PALETTE.length)]);
        }

        StringBuilder html = new StringBuilder();

        // ---- HEAD ----
        html.append("""
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Planning — PlanifyEdu</title>
                  <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #0f1117; color: #e2e8f0; }

                    /* ===== EN-TÊTE ===== */
                    .header { background: linear-gradient(135deg, #1a1f2e, #252b3b);
                              padding: 28px 40px; border-bottom: 1px solid #2d3748;
                              display: flex; justify-content: space-between; align-items: center; }
                    .logo   { font-size: 28px; font-weight: 800; color: #fff;
                              text-shadow: 0 0 16px rgba(99,102,241,.7); }
                    .meta   { font-size: 12px; color: #64748b; text-align: right; line-height: 1.8; }
                    .meta strong { color: #94a3b8; }

                    /* ===== Corps ===== */
                    .content { max-width: 1280px; margin: 32px auto; padding: 0 32px 64px; }
                    h2 { font-size: 18px; font-weight: 700; color: #e2e8f0;
                         border-left: 3px solid #6366f1; padding-left: 12px; margin: 32px 0 12px; }

                    /* ===== TABLEAU ===== */
                    table { width: 100%; border-collapse: collapse; border-radius: 12px; overflow: hidden;
                            box-shadow: 0 4px 16px rgba(0,0,0,0.4); margin-bottom: 8px; }
                    thead { background: #1a2030; }
                    thead th { padding: 12px 16px; text-align: left; font-size: 11px;
                               font-weight: 700; color: #94a3b8; text-transform: uppercase;
                               letter-spacing: .5px; }
                    tbody tr:nth-child(odd)  { background: #0f1117; }
                    tbody tr:nth-child(even) { background: #111827; }
                    tbody tr:hover { background: #1e2433; }
                    tbody td { padding: 12px 16px; font-size: 13px; color: #e2e8f0;
                               border-bottom: 1px solid #1e2433; vertical-align: middle; }

                    /* ===== BADGES TYPE ===== */
                    .badge { display: inline-block; padding: 2px 8px; border-radius: 4px;
                             font-size: 11px; font-weight: 700; }
                    .badge-cm { background: rgba(99,102,241,.25); color: #818cf8; border: 1px solid #6366f1; }
                    .badge-td { background: rgba(16,185,129,.25); color: #34d399; border: 1px solid #10b981; }
                    .badge-tp { background: rgba(245,158,11,.25);  color: #fbbf24; border: 1px solid #f59e0b; }

                    /* ===== DOT COULEUR ===== */
                    .dot { display: inline-block; width: 8px; height: 8px;
                           border-radius: 50%; margin-right: 8px; flex-shrink: 0; }

                    /* ===== LÉGENDE ===== */
                    .legende { display: flex; flex-wrap: wrap; gap: 12px; margin: 16px 0 32px; }
                    .leg-item { display: flex; align-items: center; gap: 8px; padding: 4px 12px;
                                background: rgba(0,0,0,.3); border-radius: 6px; font-size: 12px; color: #94a3b8; }

                    /* ===== STATS ===== */
                    .stats { display: flex; gap: 16px; margin: 0 0 32px; flex-wrap: wrap; }
                    .stat-card { background: #1e2433; border-radius: 10px; padding: 16px 24px;
                                 border: 1px solid #2d3748; text-align: center; flex: 1; min-width: 120px; }
                    .stat-num  { font-size: 28px; font-weight: 800; color: #6366f1; }
                    .stat-lbl  { font-size: 12px; color: #64748b; margin-top: 4px; }

                    /* ===== VIDE ===== */
                    .empty { color: #475569; font-style: italic; padding: 16px; text-align: center; }

                    /* ===== PIED DE PAGE ===== */
                    footer { background: #0a0d14; border-top: 1px solid #2d3748;
                             padding: 16px 40px; font-size: 11px; color: #374151; text-align: center; }

                    /* ===== PRINT ===== */
                    @media print {
                      body { background: #fff; color: #111; }
                      .header  { background: #1a1f2e; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                      table, thead, tbody tr:nth-child(odd), tbody tr:nth-child(even),
                      .stat-card, .leg-item, footer { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                    }
                  </style>
                </head>
                <body>
                """);

        // ---- HEADER ----
        html.append("<div class=\"header\">")
                .append("<div>")
                .append("<div class=\"logo\">🎓 PlanifyEdu</div>")
                .append("<div style=\"color:#64748b;font-size:13px;margin-top:4px;\">Système de gestion de planification universitaire</div>")
                .append("</div>")
                .append("<div class=\"meta\">")
                .append("<strong>").append(titre).append("</strong><br>")
                .append("Exporté le : ").append(dateExport).append("<br>")
                .append("Par : ").append(utilisateur)
                .append("</div>")
                .append("</div>\n");

        // ---- CONTENU ----
        html.append("<div class=\"content\">\n");

        // Statistiques globales
        long nbCM = sessions.stream().filter(p -> p.getTypeSession() == TypeSession.CM).count();
        long nbTD = sessions.stream().filter(p -> p.getTypeSession() == TypeSession.TD).count();
        long nbTP = sessions.stream().filter(p -> p.getTypeSession() == TypeSession.TP).count();
        long nbMatieres = sessions.stream().map(Planning::getCourseId).distinct().count();

        html.append("<div class=\"stats\">")
                .append(statCard(String.valueOf(sessions.size()), "Sessions totales"))
                .append(statCard(String.valueOf(nbCM), "Cours Magistraux"))
                .append(statCard(String.valueOf(nbTD), "Travaux Dirigés"))
                .append(statCard(String.valueOf(nbTP), "Travaux Pratiques"))
                .append(statCard(String.valueOf(nbMatieres), "Matières"))
                .append("</div>\n");

        // Légende des matières
        html.append("<div class=\"legende\">\n");
        Set<Integer> seen = new LinkedHashSet<>();
        for (Planning p : sessions) {
            if (!seen.add(p.getCourseId()))
                continue;
            String color = courseColors.getOrDefault(p.getCourseId(), "#6366f1");
            html.append("<div class=\"leg-item\">")
                    .append("<span class=\"dot\" style=\"background:").append(color).append(";\"></span>")
                    .append(p.getCoursNom() != null ? p.getCoursNom() : "?")
                    .append("</div>\n");
        }
        html.append("</div>\n");

        // Tableau par jour
        for (Map.Entry<String, List<Planning>> entry : parJour.entrySet()) {
            String jour = entry.getKey();
            List<Planning> liste = entry.getValue();

            html.append("<h2>").append(jour).append("</h2>\n");

            if (liste.isEmpty()) {
                html.append("<div class=\"empty\">Aucune session ce jour.</div>\n");
                continue;
            }

            html.append("""
                    <table>
                      <thead>
                        <tr>
                          <th>Matière</th><th>Type</th><th>Horaire</th>
                          <th>Enseignant</th><th>Salle</th><th>Groupe</th><th>Récurrence</th>
                        </tr>
                      </thead>
                      <tbody>
                    """);

            for (Planning p : liste) {
                String color = courseColors.getOrDefault(p.getCourseId(), "#6366f1");
                String typeBadge = buildTypeBadge(p.getTypeSession());

                html.append("<tr>")
                        .append("<td><span class=\"dot\" style=\"background:").append(color).append(";\"></span>")
                        .append(safe(p.getCoursNom())).append("</td>")
                        .append("<td>").append(typeBadge).append("</td>")
                        .append("<td><strong>").append(safe(p.getHeureDebut())).append("</strong>")
                        .append(" – ").append(safe(p.getHeureFin())).append("</td>")
                        .append("<td>").append(safe(p.getEnseignantNom())).append("</td>")
                        .append("<td>").append(safe(p.getSalleNom())).append("</td>")
                        .append("<td>").append(safe(p.getGroupeNom())).append("</td>")
                        .append("<td>").append(p.getRecurrenceLabel()).append("</td>")
                        .append("</tr>\n");
            }

            html.append("</tbody></table>\n");
        }

        html.append("</div>\n"); // fin .content

        // ---- FOOTER ----
        html.append("<footer>PlanifyEdu — Document généré automatiquement le ")
                .append(dateExport).append(" | Confidentiel</footer>\n");

        html.append("</body></html>");
        return html.toString();
    }

    // =====================================================
    // UTILITAIRES HTML
    // =====================================================

    private String statCard(String valeur, String libelle) {
        return "<div class=\"stat-card\"><div class=\"stat-num\">" + valeur
                + "</div><div class=\"stat-lbl\">" + libelle + "</div></div>\n";
    }

    private String buildTypeBadge(TypeSession type) {
        if (type == null)
            return "<span class=\"badge\">—</span>";
        return switch (type) {
            case CM -> "<span class=\"badge badge-cm\">CM</span>";
            case TD -> "<span class=\"badge badge-td\">TD</span>";
            case TP -> "<span class=\"badge badge-tp\">TP</span>";
        };
    }

    /** Retourne "—" si la valeur est null ou vide */
    private String safe(String val) {
        return (val == null || val.isBlank() || val.equals("—")) ? "—" : val;
    }
}
