#!/bin/bash

echo "============================================"
echo "        TEST DU CONTROLLER AVEC HandleUrl"
echo "============================================"

# === Définition des chemins ===
SRC="src"
BIN="bin"
LIB="WEB-INF/lib"
FRAMEWORK_JAR="$LIB/framework.jar"
REFLECTIONS_JAR="$LIB/reflections-0.9.10.jar"
GUAVA_JAR="$LIB/guava-31.1-jre.jar"
JAVASSIST_jar="$LIB/javassist-3.29.2-GA.jar"

# === Nettoyage de l'ancien dossier bin ===
if [ -d "$BIN" ]; then
    echo "Suppression de l'ancien dossier bin..."
    rm -rf "$BIN"
fi
mkdir -p "$BIN"

# === Compilation de tous les fichiers Java ===
echo "Compilation des classes..."
javac -d "$BIN" -cp "$FRAMEWORK_JAR:$REFLECTIONS_JAR:$GUAVA_JAR:$JAVASSIST_jar" $(find "$SRC" -name "*.java")

if [ $? -ne 0 ]; then
    echo
    echo "❌ Erreur de compilation !"
    read -p "Appuyez sur Entrée pour quitter..."
    exit 1
fi

# === Exécution du test ===
echo
echo "============================================"
echo "        EXECUTION DU SCANNER DE CONTROLLER"
echo "============================================"
echo

java -cp "$BIN:$FRAMEWORK_JAR:$REFLECTIONS_JAR:$GUAVA_JAR:$JAVASSIST_jar" com.app.Main

echo
echo "============================================"
echo "             TEST TERMINE"
echo "============================================"
read -p "Appuyez sur Entrée pour terminer..."
