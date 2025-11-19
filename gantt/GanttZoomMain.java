package gantt;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GanttZoomMain {
    public static void main(String[] args) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Ton", LocalDate.of(2025,11,1),10,0,"Normale"));
        tasks.add(new Task("grand", LocalDate.of(2026,11,3),15,0,"Élevée"));
        tasks.add(new Task("pere", LocalDate.of(2025,11,5),20,0,"Normale"));
        tasks.add(new Task("le", LocalDate.of(2027,11,2),7,0,"Basse"));
        tasks.add(new Task("chauve", LocalDate.of(2028,11,10),12,0,"Haute"));
        tasks.add(new Task("Tâche 6", LocalDate.of(2025,11,25),18,0,"Normale"));

        GanttPanelZoom ganttPanel = new GanttPanelZoom(tasks);
        JScrollPane scrollPane = new JScrollPane(ganttPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JFrame frame = new JFrame("Gantt Multi-Échelle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200,500);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Échelle :"));
        JComboBox<GanttPanelZoom.ScaleType> scaleCombo = new JComboBox<>(GanttPanelZoom.ScaleType.values());
        scaleCombo.addActionListener(e -> ganttPanel.setScaleType((GanttPanelZoom.ScaleType) scaleCombo.getSelectedItem()));
        topPanel.add(scaleCombo);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
