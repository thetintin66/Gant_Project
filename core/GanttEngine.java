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

	/**
	 * Calcule les bornes temporelles du projet.
	 *
	 * - projectStart : date de début du projet, déterminée comme la date de début
	 *   la plus tôt parmi toutes les tâches. Si la liste de tâches est vide, utilise
	 *   la date du jour.
	 * - projectEnd : date de fin du projet, déterminée comme la date de fin la plus tardive
	 *   parmi toutes les tâches (startDate + duration). Si la liste de tâches est vide,
	 *   utilise projectStart.
	 *
	**/
    private void calculateProjectBounds() {
        projectStart = tasks.stream()
            .map(Task::getStartDate) // récupère la date de début de chaque tâche
            .min(LocalDate::compareTo) // trouve la plus petite date
            .orElse(LocalDate.now());  // si pas de tâche, date du jour

        projectEnd = tasks.stream()
          .map(t -> t.getStartDate().plusDays(t.getDuration())) // date de fin de chaque tâche
          .max(LocalDate::compareTo) // prend la date la plus tardive
          .orElse(projectStart);    // si pas de tâche, prend projectStart
    }

	/**
	 * Calcule la plage de dates visible dans le diagramme de Gantt
	 * en fonction de l'échelle de temps choisie (DAY, WEEK, MONTH, YEAR).
	 *
	 * @return un objet DateRange représentant la période visible
	 */
	private DateRange calculateVisibleRange() {
		// La date de début de la plage visible correspond au début du projet
		LocalDate start = projectStart;
		LocalDate end; // La date de fin sera déterminée selon l'échelle

		// Détermination de la date de fin en fonction du type d'échelle
		switch (scaleType) {
			case DAY:
				// Pour l'échelle JOUR, on affiche 3 semaines à partir du début
				end = start.plusWeeks(3);
				break;
			case WEEK:
				// Pour l'échelle SEMAINE, on affiche 6 mois complets
				end = start.plusMonths(6);
				// On ajuste pour que la fin tombe toujours un dimanche
				while (end.getDayOfWeek().getValue() != 7) {
					end = end.plusDays(1);
				}
				break;
			case MONTH:
				// Pour l'échelle MOIS, on affiche jusqu'à la fin de l'année suivante
				end = start.plusYears(1).withMonth(12).withDayOfMonth(31);
				break;
			case YEAR:
				// Pour l'échelle ANNEE, on affiche 3 ans complets
				end = start.plusYears(3).withMonth(12).withDayOfMonth(31);
				break;
			default:
				// Par défaut, on considère 3 semaines (comme pour DAY)
				end = start.plusWeeks(3);
		}

		// Retourne la plage de dates visible
		return new DateRange(start, end);
	}


/**
 * Calcule le nombre total d'unités (jours, semaines, mois, années) dans la plage visible.
 */
private long calculateTotalUnits(DateRange range) {
    switch (scaleType) {
        case YEAR:
            // Nombre d'années complètes entre le début et la fin de la plage visible
            return ChronoUnit.YEARS.between(
                range.start.withDayOfYear(1), 
                range.end.withDayOfYear(1)
            );
        case MONTH:
            // Nombre de mois complets entre le début et la fin de la plage visible
            return ChronoUnit.MONTHS.between(
                range.start.withDayOfMonth(1),
                range.end.withDayOfMonth(1)
            );
        case WEEK:
            // Nombre de semaines complètes entre le début et la fin de la plage visible
            return ChronoUnit.WEEKS.between(range.start, range.end);
        default:
            // Par défaut, on utilise le nombre de jours
            return ChronoUnit.DAYS.between(range.start, range.end);
    }
}

/**
 * Calcule la largeur d'une unité en pixels, éventuellement ajustée pour remplir l'espace disponible.
 */
private double calculateAdjustedUnitWidth(long totalUnits) {
    double unitWidth = getBaseUnitWidth(); // largeur de base par unité
    int availableWidth = getWidth() - LEFT_MARGIN - RIGHT_PADDING; // largeur disponible pour le diagramme
    double totalWidth = totalUnits * unitWidth; // largeur totale avec la largeur de base

    // Ajuste la largeur de l'unité si le total est inférieur à l'espace disponible
    if (totalWidth < availableWidth) {
        unitWidth = (double) availableWidth / totalUnits;
    }

    return unitWidth;
}

/**
 * Calcule la position X et la largeur d'une tâche dans le diagramme de Gantt.
 */
private TaskPosition calculateTaskPosition(LocalDate start, LocalDate end, DateRange range, double unitWidth) {
    double taskX = LEFT_MARGIN; // position X initiale
    double taskWidth;           // largeur de la tâche

    // Calcul selon l'échelle choisie
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
            // Pour l'échelle JOUR, calcul basé sur le nombre de jours depuis le début de la plage
            long daysOffset = ChronoUnit.DAYS.between(range.start, start);
            taskX += daysOffset * unitWidth;
            taskWidth = ChronoUnit.DAYS.between(start, end) * unitWidth;
    }

    // On s'assure que la largeur minimale d'une tâche est respectée
    taskWidth = Math.max(MIN_TASK_WIDTH, taskWidth);

    return new TaskPosition((int) Math.round(taskX), (int) Math.round(taskWidth));
}

/**
 * Calcule la position horizontale d'une tâche en échelle ANNEE.
 */
private double calculateYearPosition(LocalDate start, LocalDate visibleStart, double unitWidth) {
    long yearsOffset = ChronoUnit.YEARS.between(
        visibleStart.withDayOfYear(1),
        start.withDayOfYear(1)
    );

    long daysInYear = start.lengthOfYear(); // nombre de jours dans l'année
    long dayOffset = ChronoUnit.DAYS.between(start.withDayOfYear(1), start); // jours écoulés depuis le début de l'année
    double adjustedWidth = Math.min(unitWidth, 200 * zoomFactor); // ajuste largeur en fonction du zoom

    return (yearsOffset + (double) dayOffset / daysInYear) * adjustedWidth;
}

	/**
	 * Calcule la largeur d'une tâche en échelle ANNEE.
	 */
	private double calculateYearWidth(LocalDate start, LocalDate end, double unitWidth) {
		long daysInYear = start.lengthOfYear();
		double adjustedWidth = Math.min(unitWidth, 200 * zoomFactor);
		return (ChronoUnit.DAYS.between(start, end) / (double) daysInYear) * adjustedWidth;
	}

	/**
	 * Calcule la position horizontale d'une tâche en échelle MOIS.
	 */
	private double calculateMonthPosition(LocalDate start, LocalDate visibleStart, double unitWidth) {
		long monthsOffset = ChronoUnit.MONTHS.between(
			visibleStart.withDayOfMonth(1),
			start.withDayOfMonth(1)
		);

		long daysInMonth = start.lengthOfMonth();
		long dayOffset = start.getDayOfMonth() - 1; // jours écoulés depuis le début du mois

		return (monthsOffset + (double) dayOffset / daysInMonth) * unitWidth;
	}

	/**
	 * Calcule la largeur d'une tâche en échelle MOIS.
	 */
	private double calculateMonthWidth(LocalDate start, LocalDate end, double unitWidth) {
		long daysInMonth = start.lengthOfMonth();
		return (ChronoUnit.DAYS.between(start, end) / (double) daysInMonth) * unitWidth;
	}

	/**
	 * Calcule la position horizontale d'une tâche en échelle SEMAINE.
	 */
	private double calculateWeekPosition(LocalDate start, LocalDate visibleStart, double unitWidth) {
		long weeksOffset = ChronoUnit.WEEKS.between(visibleStart, start);
		long dayOffset = ChronoUnit.DAYS.between(visibleStart.plusWeeks(weeksOffset), start);
		return (weeksOffset + dayOffset / 7.0) * unitWidth;
	}

	/**
	 * Calcule la largeur d'une tâche en échelle SEMAINE.
	 */
	private double calculateWeekWidth(LocalDate start, LocalDate end, double unitWidth) {
		return (ChronoUnit.DAYS.between(start, end) / 7.0) * unitWidth;
	}

	/**
	 * Vérifie si une tâche est visible dans la plage de dates affichée.
	 */
	private boolean isTaskVisible(Task task, DateRange range) {
		LocalDate end = task.getStartDate().plusDays(task.getDuration()); // date de fin de la tâche
		// une tâche est visible si elle chevauche la plage affichée
		return !end.isBefore(range.start) && !task.getStartDate().isAfter(range.end);
	}

}