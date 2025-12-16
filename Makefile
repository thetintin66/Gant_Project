# Dossiers
SRC_DIR = srcs
OBJ_DIR = objs

# Tous les fichiers .java dans srcs
SRC = $(wildcard $(SRC_DIR)/*.java)

# Cible par défaut : compiler tout
all: $(OBJ_DIR)
	javac -d $(OBJ_DIR) $(SRC)
	@echo "Compilation terminée !"

# Crée le dossier objs si il n'existe pas
$(OBJ_DIR):
	mkdir -p $(OBJ_DIR)

# Supprimer les .class
clean:
	rm -rf $(OBJ_DIR)
	@echo "Objets supprimés !"

# Recompiler à neuf ET lancer le programme
re: clean all
	java -cp $(OBJ_DIR) GanttZoomMain
