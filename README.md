# Gantt Chart Multi-Échelle 📊

Un composant Java Swing pour visualiser des diagrammes de Gantt interactifs avec support du zoom et de plusieurs échelles temporelles.

## Fonctionnalités 🎯

- **4 échelles d'affichage** : Année, Mois, Semaine, Jour
- **Zoom configurable** : De 50% à 300%
- **Code couleur par priorité** : Haute (rouge), Normale (bleu), Basse (vert)
- **Interface responsive** : Ajustement automatique aux contenus
- **Séparation noms/diagramme** : Les noms restent fixes à gauche
- **Synchronisation intelligente** : Les noms s'affichent uniquement si la tâche est visible

## Architecture 🏗️

### Classes principales

#### `GanttPanelZoom`
Composant principal pour afficher le diagramme Gantt.

**Méthodes publiques** :
- `setScaleType(ScaleType type)` - Change l'échelle d'affichage
- `setZoomFactor(double factor)` - Ajuste le zoom (0.5 à 3.0)
- `createGanttView(List<Task> tasks)` - Crée la vue complète avec JSplitPane

**Énumération ScaleType** :
```java
YEAR   // Vue annuelle sur 3 ans
MONTH  // Vue mensuelle sur 1 an
WEEK   // Vue hebdomadaire sur 6 mois
DAY    // Vue quotidienne sur 3 semaines
```

#### `TaskNamesPanel`
Panneau interne qui affiche les noms des tâches (fixe horizontalement).
- Synchronisé automatiquement avec le diagramme
- Affiche les noms uniquement si la tâche est visible
- Ne scroll pas horizontalement

#### `Task`
Modèle de données représentant une tâche.

**Constructeur** :
```java
Task(String name, LocalDate startDate, int duration, int progress, String priority)
```

**Attributs** :
- `name` : Nom de la tâche
- `startDate` : Date de début
- `duration` : Durée en jours
- `progress` : Progression (0-100)
- `priority` : Priorité ("Haute", "Normale", "Basse")

## Utilisation 🚀

### Exemple simple

```java
import gantt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GanttZoomMain {
    public static void main(String[] args) {
        // Créer des tâches
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Ton", LocalDate.of(2025, 10, 1), 40, 0, "Élevée"));
        tasks.add(new Task("grand", LocalDate.of(2026, 11, 3), 15, 0, "Élevée"));
        tasks.add(new Task("pere", LocalDate.of(2025, 11, 5), 20, 0, "Normale"));

        // Créer la vue Gantt
        JSplitPane ganttView = GanttPanelZoom.createGanttView(tasks);

        // Afficher dans une fenêtre
        JFrame frame = new JFrame("Gantt Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 500);
        frame.add(ganttView, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
```

## Constantes et Personnalisation ⚙️

### Mise en page
- `TASK_HEIGHT = 22` : Hauteur d'une barre de tâche (px)
- `TASK_SPACING = 14` : Espacement vertical entre tâches (px)
- `Y_OFFSET = 80` : Hauteur de l'en-tête (px)
- `HEADER_HEIGHT = 40` : Hauteur de la zone temporelle (px)

### Couleurs
- Priorité haute : `RGB(220, 53, 69)` - Rouge vif
- Priorité normale : `RGB(0, 123, 255)` - Bleu standard
- Priorité basse : `RGB(40, 167, 69)` - Vert
- Grille : `RGB(200, 200, 200)` - Gris clair

### Échelles temporelles

| Échelle | Largeur base | Période visible |
|---------|-------------|-----------------|
| YEAR | 50 px | 3 ans |
| MONTH | 40 px | 1 an |
| WEEK | 20 px | 6 mois |
| DAY | 10 px | 3 semaines |

## Code couleur par priorité 🎨

```java
// Supports français et anglais
"Haute", "Élevée", "High"      → Rouge
"Normale", "Normal", "Medium"  → Bleu
"Basse", "Low"                 → Vert
null, autre                    → Gris
```

## Points clés de la synchronisation 🔄

1. **Noms visibles uniquement si tâche visible** : Le panneau des noms vérifie la plage temporelle visible et n'affiche que les tâches concernées.

2. **Alignement vertical** : Même si une tâche n'est pas visible, son espace vertical est réservé pour maintenir l'alignement.

3. **Mise à jour automatique** : Chaque fois que l'échelle ou le zoom change, les deux panneaux se synchronisent.

## Exemple avec contrôles d'échelle

```java
GanttPanelZoom ganttPanel = new GanttPanelZoom(tasks);

// Combo box pour changer l'échelle
JComboBox<GanttPanelZoom.ScaleType> scaleCombo = 
    new JComboBox<>(GanttPanelZoom.ScaleType.values());
scaleCombo.addActionListener(e -> 
    ganttPanel.setScaleType((GanttPanelZoom.ScaleType) scaleCombo.getSelectedItem())
);

// Slider pour le zoom
JSlider zoomSlider = new JSlider(50, 300, 100);
zoomSlider.addChangeListener(e -> 
    ganttPanel.setZoomFactor(zoomSlider.getValue() / 100.0)
);
```

## Structure des fichiers 📁

```
gantt/
├── GanttPanelZoom.java      # Composant principal
├── Task.java                # Modèle de tâche
└── GanttZoomMain.java       # Exemple d'utilisation
```

## Améliorations possibles 🚀

- Édition des tâches (drag & drop)
- Export en PNG/PDF
- Gestion des dépendances entre tâches
- Affichage du chemin critique
- Historique des modifications
- Mode sombre

## Auteur 👨‍💻

Quentin Humblot

## Licence 📜

Libre d'utilisation