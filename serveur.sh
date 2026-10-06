#!/usr/bin/env bash
# Compile et lance le serveur du jeu en ligne (lobby + relais des messages).
# Pas besoin de JavaFX : seul le JDK (17 ou plus récent, 21+ conseillé) est nécessaire.
# Usage : ./serveur.sh [port]        (port par défaut : 8765, ou variable d'environnement PORT)
#         ./serveur.sh --jar         (fabrique seulement build-serveur/serveur.jar, à copier sur une machine)
set -e
cd "$(dirname "$0")"

OUT=build-serveur
rm -rf "$OUT"
mkdir -p "$OUT/classes"
javac -d "$OUT/classes" src/reseau/*.java src/serveur/*.java
jar --create --file "$OUT/serveur.jar" --main-class serveur.ServeurBatailleNavale -C "$OUT/classes" .

if [ "$1" = "--jar" ]; then
    echo "Serveur prêt : $OUT/serveur.jar  (lancement : java -jar serveur.jar [port])"
    exit 0
fi
java -jar "$OUT/serveur.jar" "$@"
