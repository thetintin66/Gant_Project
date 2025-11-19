# Gantt Panel Zoom - Java

## Description

`GanttPanelZoom` est un composant Swing en Java permettant d'afficher un **diagramme de Gantt** pour visualiser des tâches d'un projet.  
Il supporte plusieurs échelles de temps (`DAY`, `WEEK`, `MONTH`, `YEAR`) et permet de zoomer sur la timeline.

Le composant met en évidence :

- Les tâches avec un rectangle coloré selon leur priorité (`haute`, `normale`, `basse`)  
- Lignes verticales pour chaque unité de temps (jours, semaines, mois, années)  
- Tous les lundis affichés sur l’échelle `DAY`  
- Fond alterné pour améliorer la lisibilité

---

## Fonctionnalités

- Affichage multi-échelle (`DAY`, `WEEK`, `MONTH`, `YEAR`)  
- Zoom possible via le facteur `zoomFactor`  
- Couleur des tâches selon la priorité  
- Affichage automatique du nom des tâches  
- Supporte des périodes de projet dynamiques selon les dates des tâches  
- Texte des dates formaté pour les lundis (`dd/MM`), sans année

---

## Installation

1. Ajouter la classe `GanttPanelZoom.java` à votre projet Java.
2. Assurez-vous d’avoir une classe `Task` avec au minimum :
   ```java
   public class Task {
       private String name;
       private LocalDate startDate;
       private int duration; // en jours
       private String priority;

       // Getters
       public String getName() { return name; }
       public LocalDate getStartDate() { return startDate; }
       public int getDuration() { return duration; }
       public String getPriority() { return priority; }
   }
