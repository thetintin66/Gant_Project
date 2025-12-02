package gantt;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GanttZoomMain {
    public static void main(String[] args) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Ton", LocalDate.of(2025, 10, 1), 40, 0, "Élevée"));
        tasks.add(new Task("grand", LocalDate.of(2026, 11, 3), 15, 0, "Élevée"));
        tasks.add(new Task("pere", LocalDate.of(2025, 11, 5), 20, 0, "Normale"));
        tasks.add(new Task("le", LocalDate.of(2027, 11, 2), 7, 0, "Basse"));
        tasks.add(new Task("chauve", LocalDate.of(2028, 11, 10), 12, 0, "Haute"));
        tasks.add(new Task("Tâche 6", LocalDate.of(2025, 11, 25), 18, 0, "Normale"));

        JSplitPane ganttView = GanttPanelZoom.createGanttView(tasks);

        JFrame frame = new JFrame("Gantt Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 500);
        frame.add(ganttView, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}