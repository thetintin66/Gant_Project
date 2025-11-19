package gantt;

import java.time.LocalDate;

public class Task {
    private String name;
    private LocalDate startDate;
    private int duration; // en jours
    private int progress; // 0 à 100
    private String priority; // "Basse", "Normale", "Élevée", "Haute"

    public Task(String name, LocalDate startDate, int duration, int progress, String priority) {
        this.name = name;
        this.startDate = startDate;
        this.duration = duration;
        this.progress = progress;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getDuration() {
        return duration;
    }

    public int getPriorityLevel() {
        switch (priority.toLowerCase()) {
            case "basse": return 1;
            case "normale": return 2;
            case "élevée": 
            case "haute": return 3;
            default: return 2;
        }
    }

    public String getPriority() {
        return priority;
    }

    public int getProgress() {
        return progress;
    }
}
