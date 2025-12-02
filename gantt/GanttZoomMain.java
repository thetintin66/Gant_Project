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

        // Créer le view complet avec JSplitPane
        JSplitPane ganttView = GanttPanelZoom.createGanttView(tasks);
        
        // Récupérer le panneau Gantt pour les contrôles
        GanttPanelZoom ganttPanel = (GanttPanelZoom) 
            ((JScrollPane) ganttView.getRightComponent()).getViewport().getView();

        JFrame frame = new JFrame("Gantt Multi-Échelle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 600);

        // Panel des contrôles (en haut)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Contrôle de l'échelle
        topPanel.add(new JLabel("Échelle :"));
        JComboBox<GanttPanelZoom.ScaleType> scaleCombo = new JComboBox<>(GanttPanelZoom.ScaleType.values());
        scaleCombo.setSelectedItem(GanttPanelZoom.ScaleType.DAY);
        scaleCombo.addActionListener(e -> ganttPanel.setScaleType((GanttPanelZoom.ScaleType) scaleCombo.getSelectedItem()));
        topPanel.add(scaleCombo);
        
        // Contrôle du zoom
        topPanel.add(new JLabel("Zoom :"));
        JSlider zoomSlider = new JSlider(50, 300, 100);
        zoomSlider.setPreferredSize(new Dimension(150, 40));
        zoomSlider.addChangeListener(e -> {
            double zoomValue = zoomSlider.getValue() / 100.0;
            ganttPanel.setZoomFactor(zoomValue);
        });
        topPanel.add(zoomSlider);
        topPanel.add(new JLabel("(50% - 300%)"));

        frame.setLayout(new BorderLayout());
        frame.add(ganttView, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}