package gantt;

import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public class GanttPanelZoom extends JPanel {

    // Types d’échelles possibles pour l’affichage
    public enum ScaleType { YEAR, MONTH, WEEK, DAY }

    private List<Task> tasks;             // Liste des tâches à afficher
    private LocalDate projectStart;       // Date du début du projet
    private LocalDate projectEnd;         // Date de fin du projet
    private ScaleType scaleType = ScaleType.DAY; // Échelle actuelle
    private double zoomFactor = 1.0;      // Facteur de zoom

    // Dimensions et marges
    private final int taskHeight = 22;
    private final int taskSpacing = 14;
    private final int yOffset = 80;
    private final int leftMargin = 100;

    public GanttPanelZoom(List<Task> tasks) {
        this.tasks = tasks;

        // Définir la date de début minimale
        projectStart = tasks.stream()
                .map(Task::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        // Date de fin maximale
        projectEnd = tasks.stream()
                .map(t -> t.getStartDate().plusDays(t.getDuration()))
                .max(LocalDate::compareTo)
                .orElse(projectStart);

        setBackground(Color.WHITE);
    }

    // Modifier l’échelle d’affichage
    public void setScaleType(ScaleType type) {
        this.scaleType = type;
        revalidate();
        repaint();
    }

    // Largeur de base en fonction de l’échelle + zoom
    private double getBaseUnitWidth() {
        switch (scaleType) {
            case YEAR: return 50 * zoomFactor;
            case MONTH: return 40 * zoomFactor;
            case WEEK: return 20 * zoomFactor;
            default: return 10 * zoomFactor; // DAY
        }
    }

    // Couleurs en fonction de la priorité
    private Color getColorForPriority(String priority) {
        if (priority == null) return Color.GRAY;
        switch (priority.toLowerCase()) {
            case "haute": case "élevée": return Color.RED;
            case "normale": return Color.BLUE;
            case "basse": return Color.GREEN.darker();
            default: return Color.GRAY;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Police lissée
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        double unitWidth = getBaseUnitWidth();

        // --- Définir la période visible en fonction de l’échelle ---
        LocalDate visibleStart = projectStart;
        LocalDate visibleEnd = projectStart;

        switch (scaleType) {
            case DAY:
                // Affichage sur ~3 mois
                visibleEnd = visibleStart.plusMonths(3).withDayOfMonth(visibleStart.plusMonths(3).lengthOfMonth());
                break;

            case WEEK:
                // Affichage sur ~6 mois
                visibleEnd = visibleStart.plusMonths(6);
                // Ajuster à la fin de semaine
                while (visibleEnd.getDayOfWeek().getValue() != 7) visibleEnd = visibleEnd.plusDays(1);
                break;

            case MONTH:
                // 1 an
                visibleEnd = visibleStart.plusYears(1).withMonth(12).withDayOfMonth(31);
                break;

            case YEAR:
                // 3 ans
                visibleEnd = visibleStart.plusYears(3).withMonth(12).withDayOfMonth(31);
                break;
        }

        // --- Calcul du nombre d’unités en fonction de l’échelle ---
        long totalUnits;
        switch (scaleType) {
            case YEAR:
                totalUnits = ChronoUnit.YEARS.between(visibleStart.withDayOfYear(1), visibleEnd.withDayOfYear(1)) + 1;
                break;

            case MONTH:
                totalUnits = ChronoUnit.MONTHS.between(
                        LocalDate.of(visibleStart.getYear(), visibleStart.getMonth(), 1),
                        LocalDate.of(visibleEnd.getYear(), visibleEnd.getMonth(), 1)) + 1;
                break;

            case WEEK:
                totalUnits = ChronoUnit.WEEKS.between(visibleStart, visibleEnd) + 1;
                break;

            default: // DAY
                totalUnits = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
        }

        // Ajustement de la largeur totale
        int availableWidth = getWidth() - leftMargin - 50;
        double totalWidth = totalUnits * unitWidth;

        if (totalWidth < availableWidth) {
            unitWidth = (double) availableWidth / totalUnits;
        }

        // --- Fond alterné pour améliorer la lisibilité ---
        if (scaleType == ScaleType.DAY) {
            // Une semaine sur deux
            for (int i = 0; i < totalUnits; i += 14) {
                int x = leftMargin + (int)Math.round(i * unitWidth);
                g2.setColor(new Color(200, 200, 200, 100));
                g2.fillRect(x, yOffset - 30, (int)Math.round(unitWidth * 7), getHeight());
            }
        } else if (scaleType == ScaleType.YEAR) {
            // Une année sur deux
            for (int i = 0; i < totalUnits; i += 2) {
                int x = leftMargin + (int)Math.round(i * unitWidth);
                g2.setColor(new Color(200, 200, 200, 100));
                g2.fillRect(x, yOffset - 30, (int)Math.round(unitWidth), getHeight());
            }
        }

        // --- Axe du temps ---
        LocalDate cursor = visibleStart;
        WeekFields wf = WeekFields.of(Locale.getDefault());
        g2.setFont(new Font("Arial", Font.PLAIN, 12));

        for (int i = 0; i <= totalUnits; i++) {
            int x = leftMargin + (int)Math.round(i * unitWidth);
            String label = "";

            // Déterminer quoi afficher
            switch (scaleType) {
                case YEAR:
                    label = String.valueOf(cursor.getYear());
                    break;

                case MONTH:
                    label = cursor.getMonth().toString().substring(0,3) + " " + cursor.getYear();
                    break;

                case WEEK:
                    label = "S" + cursor.get(wf.weekOfWeekBasedYear());
                    break;

                default: // DAY
                    // Afficher uniquement les lundis
                    // Format sans année : dd/MM
                    if (cursor.getDayOfWeek().getValue() == 1)
                        label = cursor.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
                        //System.out.print(cursor.getDayOfWeek().getValue());
            }

            // Éviter les collisions de texte
            if (!label.isEmpty()) {
                g2.setColor(Color.BLACK);
                g2.drawString(label, x - 20, yOffset - 40);
            }

            // Ligne verticale
            g2.setColor(new Color(200,200,200));
            g2.drawLine(x, yOffset - 30, x, getHeight());

            // Avancer le curseur
            switch (scaleType) {
                case YEAR: cursor = cursor.plusYears(1); break;
                case MONTH: cursor = cursor.plusMonths(1); break;
                case WEEK: cursor = cursor.plusWeeks(1); break;
                default: cursor = cursor.plusDays(1);
            }
        }

        // --- Dessiner chaque tâche ---
        int y = yOffset;

        for (Task t : tasks) {
            LocalDate start = t.getStartDate();
            LocalDate end = start.plusDays(t.getDuration());

            // Ne pas afficher si hors zone visible
            if (end.isBefore(visibleStart) || start.isAfter(visibleEnd))
                continue;

            double taskX = leftMargin;
            double taskWidth;

            // Calcul de position selon échelle
            switch (scaleType) {

                case YEAR: {
                    long yearsOffset = ChronoUnit.YEARS.between(
                            visibleStart.withDayOfYear(1),
                            start.withDayOfYear(1)
                    );

                    long daysInYear = start.lengthOfYear();
                    long dayOffset = ChronoUnit.DAYS.between(start.withDayOfYear(1), start);

                    double adjustedUnitWidth = Math.min(unitWidth, 200 * zoomFactor);

                    taskX += (yearsOffset + (double) dayOffset / daysInYear) * adjustedUnitWidth;
                    taskWidth = (ChronoUnit.DAYS.between(start,end) / (double)daysInYear) * adjustedUnitWidth;
                    break;
                }

                case MONTH: {
                    long monthsOffset = ChronoUnit.MONTHS.between(
                            LocalDate.of(visibleStart.getYear(), visibleStart.getMonth(), 1),
                            LocalDate.of(start.getYear(), start.getMonth(), 1)
                    );

                    long daysInMonth = start.lengthOfMonth();
                    long dayOffset = ChronoUnit.DAYS.between(start.withDayOfMonth(1), start);

                    taskX += (monthsOffset + (double) dayOffset / daysInMonth) * unitWidth;
                    taskWidth = (ChronoUnit.DAYS.between(start,end) / (double) daysInMonth) * unitWidth;
                    break;
                }

                case WEEK: {
                    long weeksOffset = ChronoUnit.WEEKS.between(visibleStart, start);
                    long dayOffset = ChronoUnit.DAYS.between(visibleStart.plusWeeks(weeksOffset), start);

                    taskX += (weeksOffset + (double) dayOffset / 7.0) * unitWidth;
                    taskWidth = (ChronoUnit.DAYS.between(start,end) / 7.0) * unitWidth;
                    break;
                }

                default: { // DAY
                    long daysOffset = ChronoUnit.DAYS.between(visibleStart, start);
                    taskX += daysOffset * unitWidth;
                    taskWidth = ChronoUnit.DAYS.between(start,end) * unitWidth;
                }
            }

            // Largeur minimale
            taskWidth = Math.max(3, taskWidth);

            // Dessin du rectangle de tâche
            g2.setColor(getColorForPriority(t.getPriority()));
            g2.fillRoundRect((int)Math.round(taskX), y, (int)Math.round(taskWidth), taskHeight,6,6);

            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect((int)Math.round(taskX), y, (int)Math.round(taskWidth), taskHeight,6,6);

            // Nom de la tâche
            g2.setColor(Color.BLACK);
            g2.drawString(t.getName(), 10, y + 16);

            y += taskHeight + taskSpacing;
        }

        // Ajuster la taille du panel
        int panelWidth = leftMargin + (int)Math.round(totalUnits * unitWidth) + 50;
        int panelHeight = y + 50;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        revalidate();
    }
}