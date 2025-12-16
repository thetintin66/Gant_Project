import java.awt.*;
import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.format.TextStyle;
//import java.time.temporal.ChronoUnit;
//import java.time.temporal.WeekFields;
import java.util.List;
//import java.util.Locale;
import javax.swing.*;

public class GanttEngine extends JPanel {

    private List<Task> tasks;
    private LocalDate projectStart;
    private LocalDate projectEnd;
    //private ScaleType scaleType = ScaleType.DAY;
    //private double zoomFactor = 1.0;

    public GanttEngine(List<Task> tasks) {
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


}