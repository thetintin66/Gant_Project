package gantt;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

/**
 * Composant JPanel pour afficher un diagramme de Gantt interactif avec support du zoom.
 * Version refactorisée : les noms des tâches sont séparés du diagramme.
 */
public class GanttPanelZoom extends JPanel {

    public enum ScaleType { 
        YEAR, MONTH, WEEK, DAY
    }

    private List<Task> tasks;
    private LocalDate projectStart;
    private LocalDate projectEnd;
    private ScaleType scaleType = ScaleType.DAY;
    private double zoomFactor = 1.0;

    // CONSTANTES DE MISE EN PAGE
    private static final int TASK_HEIGHT = 22;
    private static final int TASK_SPACING = 14;
    private static final int Y_OFFSET = 80;
    private static final int LEFT_MARGIN = 0; // Pas de marge à gauche (les noms sont ailleurs)
    private static final int RIGHT_PADDING = 50;
    private static final int HEADER_HEIGHT = 40;
    private static final int MIN_TASK_WIDTH = 3;
    private static final int BORDER_RADIUS = 6;

    // CONSTANTES DE COULEURS
    private static final Color GRID_COLOR = new Color(200, 200, 200);
    private static final Color ALTERNATE_BG = new Color(220, 220, 220, 200);
    private static final Color TASK_BORDER = Color.DARK_GRAY;
    private static final Color TEXT_COLOR = Color.BLACK;

    // FORMATTEURS DE DATE
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    /**
     * Constructeur du panneau Gantt (diagramme uniquement, sans les noms).
     */
    public GanttPanelZoom(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("La liste de tâches ne peut pas être vide");
        }
        
        this.tasks = tasks;
        calculateProjectBounds();
        setBackground(Color.WHITE);
    }

    private void calculateProjectBounds() {
        projectStart = tasks.stream()
                .map(Task::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        projectEnd = tasks.stream()
                .map(t -> t.getStartDate().plusDays(t.getDuration()))
                .max(LocalDate::compareTo)
                .orElse(projectStart);
    }

    public void setScaleType(ScaleType type) {
        this.scaleType = type;
        repaint();
    }

    public void setZoomFactor(double factor) {
        this.zoomFactor = Math.max(0.5, Math.min(factor, 3.0));
        repaint();
    }

    private double getBaseUnitWidth() {
        double baseWidth;
        switch (scaleType) {
            case YEAR:  baseWidth = 50; break;
            case MONTH: baseWidth = 40; break;
            case WEEK:  baseWidth = 20; break;
            default:    baseWidth = 10; break;
        }
        return baseWidth * zoomFactor;
    }

    private Color getColorForPriority(String priority) {
        if (priority == null) return new Color(128, 128, 128);
        
        switch (priority.toLowerCase()) {
            case "haute":
            case "élevée":
            case "high":
                return new Color(220, 53, 69);
            case "normale":
            case "normal":
            case "medium":
                return new Color(0, 123, 255);
            case "basse":
            case "low":
                return new Color(40, 167, 69);
            default:
                return new Color(128, 128, 128);
        }
    }

    private DateRange calculateVisibleRange() {
        LocalDate start = projectStart;
        LocalDate end;

        switch (scaleType) {
            case DAY:
                end = start.plusWeeks(3);
                break;
            case WEEK:
                end = start.plusMonths(6);
                while (end.getDayOfWeek().getValue() != 7) {
                    end = end.plusDays(1);
                }
                break;
            case MONTH:
                end = start.plusYears(1).withMonth(12).withDayOfMonth(31);
                break;
            case YEAR:
                end = start.plusYears(3).withMonth(12).withDayOfMonth(31);
                break;
            default:
                end = start.plusWeeks(3);
        }

        return new DateRange(start, end);
    }

    private long calculateTotalUnits(DateRange range) {
        switch (scaleType) {
            case YEAR:
                return ChronoUnit.YEARS.between(
                    range.start.withDayOfYear(1), 
                    range.end.withDayOfYear(1)
                );
            case MONTH:
                return ChronoUnit.MONTHS.between(
                    range.start.withDayOfMonth(1),
                    range.end.withDayOfMonth(1)
                );
            case WEEK:
                return ChronoUnit.WEEKS.between(range.start, range.end);
            default:
                return ChronoUnit.DAYS.between(range.start, range.end);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        DateRange visibleRange = calculateVisibleRange();
        long totalUnits = calculateTotalUnits(visibleRange);
        double unitWidth = calculateAdjustedUnitWidth(totalUnits);

        drawAlternateBackground(g2, totalUnits, unitWidth);
        drawTimeAxis(g2, visibleRange, totalUnits, unitWidth);
        drawTasks(g2, visibleRange, unitWidth);

        updatePanelSize(totalUnits, unitWidth);
    }

    private double calculateAdjustedUnitWidth(long totalUnits) {
        double unitWidth = getBaseUnitWidth();
        int availableWidth = getWidth() - LEFT_MARGIN - RIGHT_PADDING;
        double totalWidth = totalUnits * unitWidth;

        if (totalWidth < availableWidth) {
            unitWidth = (double) availableWidth / totalUnits;
        }

        return unitWidth;
    }

    private void drawAlternateBackground(Graphics2D g2, long totalUnits, double unitWidth) {
        g2.setColor(ALTERNATE_BG);
        
        int step = getAlternateStep();
        int width = getAlternateWidth();

        for (int i = 0; i < totalUnits; i += step * 2) {
            int x = LEFT_MARGIN + (int) Math.round(i * unitWidth);
            int w = (int) Math.round(unitWidth * width);
            g2.fillRect(x, Y_OFFSET - HEADER_HEIGHT, w, getHeight());
        }
    }

    private int getAlternateStep() {
        switch (scaleType) {
            case DAY:   return 7;
            case WEEK:  return 5;
            case MONTH: return 1;
            case YEAR:  return 1;
            default:    return 1;
        }
    }

    private int getAlternateWidth() {
        switch (scaleType) {
            case DAY:   return 7;
            case WEEK:  return 5;
            case MONTH: return 1;
            case YEAR:  return 1;
            default:    return 1;
        }
    }

    private void drawTimeAxis(Graphics2D g2, DateRange range, long totalUnits, double unitWidth) {
        LocalDate cursor = range.start;
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        g2.setFont(new Font("Arial", Font.PLAIN, 11));

        for (int i = 0; i < totalUnits; i++) {
            int x = LEFT_MARGIN + (int) Math.round(i * unitWidth);
            String label = getTimeLabel(cursor, range.end, weekFields);

            if (!label.isEmpty()) {
                g2.setColor(TEXT_COLOR);
                int labelX = (scaleType == ScaleType.DAY) ? x + 10 : x + 2;
                g2.drawString(label, labelX, Y_OFFSET - HEADER_HEIGHT + 15);
            }

            g2.setColor(GRID_COLOR);
            g2.drawLine(x, Y_OFFSET - HEADER_HEIGHT + 20, x, getHeight());

            cursor = advanceCursor(cursor);
        }
    }

    private String getTimeLabel(LocalDate cursor, LocalDate visibleEnd, WeekFields weekFields) {
        switch (scaleType) {
            case YEAR:
                return cursor.getYear() <= visibleEnd.getYear() 
                    ? String.valueOf(cursor.getYear()) 
                    : "";
            
            case MONTH:
                if (cursor.withDayOfMonth(1).isAfter(visibleEnd.withDayOfMonth(1))) {
                    return "";
                }
                return cursor.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH) 
                    + " " + cursor.getYear();
            
            case WEEK:
                if (cursor.isAfter(visibleEnd)) return "";
                int weekNum = cursor.get(weekFields.weekOfWeekBasedYear());
                return "S" + weekNum;
            
            default:
                return cursor.format(DAY_FORMATTER);
        }
    }

    private LocalDate advanceCursor(LocalDate cursor) {
        switch (scaleType) {
            case YEAR:  return cursor.plusYears(1);
            case MONTH: return cursor.plusMonths(1);
            case WEEK:  return cursor.plusWeeks(1);
            default:    return cursor.plusDays(1);
        }
    }

    private void drawTasks(Graphics2D g2, DateRange visibleRange, double unitWidth) {
        int y = Y_OFFSET;

        for (Task task : tasks) {
            if (isTaskVisible(task, visibleRange)) {
                drawTask(g2, task, visibleRange, unitWidth, y);
            }
            y += TASK_HEIGHT + TASK_SPACING;
        }
    }

    private boolean isTaskVisible(Task task, DateRange range) {
        LocalDate end = task.getStartDate().plusDays(task.getDuration());
        return !end.isBefore(range.start) && !task.getStartDate().isAfter(range.end);
    }

    private void drawTask(Graphics2D g2, Task task, DateRange range, double unitWidth, int y) {
        LocalDate start = task.getStartDate();
        LocalDate end = start.plusDays(task.getDuration());

        TaskPosition position = calculateTaskPosition(start, end, range, unitWidth);

        double visibleLeftX = LEFT_MARGIN;
        double visibleRightX = LEFT_MARGIN + calculateTotalUnits(range) * unitWidth;
        
        double taskX = position.x;
        double taskWidth = position.width;
        
        if (taskX + taskWidth > visibleRightX) {
            taskWidth = visibleRightX - taskX;
        }
        
        if (taskX < visibleLeftX) {
            taskWidth -= (visibleLeftX - taskX);
            taskX = visibleLeftX;
        }
        
        if (taskWidth < MIN_TASK_WIDTH) {
            return;
        }

        g2.setColor(getColorForPriority(task.getPriority()));
        g2.fillRoundRect(
            (int) Math.round(taskX), y,
            (int) Math.round(taskWidth), TASK_HEIGHT,
            BORDER_RADIUS, BORDER_RADIUS
        );

        g2.setColor(TASK_BORDER);
        g2.drawRoundRect(
            (int) Math.round(taskX), y,
            (int) Math.round(taskWidth), TASK_HEIGHT,
            BORDER_RADIUS, BORDER_RADIUS
        );

        if (taskWidth > 50) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            
            String duration = task.getDuration() + "j";
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(duration);
            int textX = (int) Math.round(taskX + (taskWidth - textWidth) / 2);
            
            g2.drawString(duration, textX, y + 14);
        }
    }

    private TaskPosition calculateTaskPosition(LocalDate start, LocalDate end, DateRange range, double unitWidth) {
        double taskX = LEFT_MARGIN;
        double taskWidth;

        switch (scaleType) {
            case YEAR:
                taskX += calculateYearPosition(start, range.start, unitWidth);
                taskWidth = calculateYearWidth(start, end, unitWidth);
                break;

            case MONTH:
                taskX += calculateMonthPosition(start, range.start, unitWidth);
                taskWidth = calculateMonthWidth(start, end, unitWidth);
                break;

            case WEEK:
                taskX += calculateWeekPosition(start, range.start, unitWidth);
                taskWidth = calculateWeekWidth(start, end, unitWidth);
                break;

            default:
                long daysOffset = ChronoUnit.DAYS.between(range.start, start);
                taskX += daysOffset * unitWidth;
                taskWidth = ChronoUnit.DAYS.between(start, end) * unitWidth;
        }

        taskWidth = Math.max(MIN_TASK_WIDTH, taskWidth);
        return new TaskPosition((int) Math.round(taskX), (int) Math.round(taskWidth));
    }

    private double calculateYearPosition(LocalDate start, LocalDate visibleStart, double unitWidth) {
        long yearsOffset = ChronoUnit.YEARS.between(
            visibleStart.withDayOfYear(1),
            start.withDayOfYear(1)
        );
        
        long daysInYear = start.lengthOfYear();
        long dayOffset = ChronoUnit.DAYS.between(start.withDayOfYear(1), start);
        double adjustedWidth = Math.min(unitWidth, 200 * zoomFactor);
        
        return (yearsOffset + (double) dayOffset / daysInYear) * adjustedWidth;
    }

    private double calculateYearWidth(LocalDate start, LocalDate end, double unitWidth) {
        long daysInYear = start.lengthOfYear();
        double adjustedWidth = Math.min(unitWidth, 200 * zoomFactor);
        return (ChronoUnit.DAYS.between(start, end) / (double) daysInYear) * adjustedWidth;
    }

    private double calculateMonthPosition(LocalDate start, LocalDate visibleStart, double unitWidth) {
        long monthsOffset = ChronoUnit.MONTHS.between(
            visibleStart.withDayOfMonth(1),
            start.withDayOfMonth(1)
        );
        
        long daysInMonth = start.lengthOfMonth();
        long dayOffset = start.getDayOfMonth() - 1;
        
        return (monthsOffset + (double) dayOffset / daysInMonth) * unitWidth;
    }

    private double calculateMonthWidth(LocalDate start, LocalDate end, double unitWidth) {
        long daysInMonth = start.lengthOfMonth();
        return (ChronoUnit.DAYS.between(start, end) / (double) daysInMonth) * unitWidth;
    }

    private double calculateWeekPosition(LocalDate start, LocalDate visibleStart, double unitWidth) {
        long weeksOffset = ChronoUnit.WEEKS.between(visibleStart, start);
        long dayOffset = ChronoUnit.DAYS.between(visibleStart.plusWeeks(weeksOffset), start);
        return (weeksOffset + dayOffset / 7.0) * unitWidth;
    }

    private double calculateWeekWidth(LocalDate start, LocalDate end, double unitWidth) {
        return (ChronoUnit.DAYS.between(start, end) / 7.0) * unitWidth;
    }

    private void updatePanelSize(long totalUnits, double unitWidth) {
        int panelWidth = LEFT_MARGIN + (int) Math.round(totalUnits * unitWidth) + RIGHT_PADDING;
        int panelHeight = Y_OFFSET + (tasks.size() * (TASK_HEIGHT + TASK_SPACING)) + 50;
        
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        revalidate();
    }

    // CLASSES INTERNES
    private static class DateRange {
        final LocalDate start;
        final LocalDate end;

        DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class TaskPosition {
        final int x;
        final int width;

        TaskPosition(int x, int width) {
            this.x = x;
            this.width = width;
        }
    }

    // ========== PANNEAU DES NOMS (CLASSE SÉPARÉE) ==========
    
    /**
     * Panneau qui affiche uniquement les noms des tâches (fixe, ne scroll pas horizontal)
     */
    public static class TaskNamesPanel extends JPanel {
        private List<Task> tasks;
        private static final int TASK_HEIGHT = 22;
        private static final int TASK_SPACING = 14;
        private static final int Y_OFFSET = 80;
        private static final int NAMES_WIDTH = 150;

        public TaskNamesPanel(List<Task> tasks) {
            this.tasks = tasks;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(NAMES_WIDTH, 500));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Dessiner le fond d'en-tête
            g2.setColor(new Color(240, 240, 240));
            g2.fillRect(0, 0, getWidth(), Y_OFFSET);
            
            // Bordure
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g2.drawLine(0, Y_OFFSET, getWidth(), Y_OFFSET);

            // Afficher les noms des tâches
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            
            int y = Y_OFFSET;
            for (Task task : tasks) {
                g2.drawString(task.getName(), 10, y + 16);
                y += TASK_HEIGHT + TASK_SPACING;
            }

            // Mettre à jour la hauteur du panneau
            int panelHeight = Y_OFFSET + (tasks.size() * (TASK_HEIGHT + TASK_SPACING)) + 50;
            setPreferredSize(new Dimension(NAMES_WIDTH, panelHeight));
            revalidate();
        }
    }

    // ========== MÉTHODE UTILE POUR CRÉER LE LAYOUT COMPLET ==========
    
    /**
     * Crée un JSplitPane avec les noms à gauche et le diagramme à droite
     */
    public static JSplitPane createGanttView(List<Task> tasks) {
        TaskNamesPanel namesPanel = new TaskNamesPanel(tasks);
        GanttPanelZoom ganttPanel = new GanttPanelZoom(tasks);
        
        JScrollPane ganttScrollPane = new JScrollPane(ganttPanel);
        ganttScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Synchroniser le scroll vertical entre les deux panneaux
        ganttScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            // Quand on scroll le diagramme, rien ne change (les noms restent fixes)
        });
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, namesPanel, ganttScrollPane);
        splitPane.setDividerLocation(150);
        splitPane.setOneTouchExpandable(true);
        
        return splitPane;
    }
}