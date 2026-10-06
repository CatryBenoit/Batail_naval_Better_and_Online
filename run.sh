#!/usr/bin/env bash
# Compile et lance la Bataille Navale (Java + JavaFX).
# Usage : ./run.sh            (utilise $JAVAFX_HOME/lib ou le SDK JavaFX trouvé par défaut)
#         JAVAFX_HOME=/chemin/vers/javafx-sdk ./run.sh
set -e
cd "$(dirname "$0")"

FX_LIB="${JAVAFX_HOME:-$HOME/Téléchargements/openjfx-25.0.1_linux-x64_bin-sdk/javafx-sdk-25.0.1}/lib"
if [ ! -f "$FX_LIB/javafx.controls.jar" ]; then
    echo "SDK JavaFX introuvable dans $FX_LIB" >&2
    echo "Télécharge-le sur https://gluonhq.com/products/javafx/ puis : JAVAFX_HOME=/chemin/javafx-sdk ./run.sh" >&2
    exit 1
fi

OUT=build
rm -rf "$OUT"
mkdir -p "$OUT"
javac --module-path "$FX_LIB" --add-modules javafx.controls,javafx.media \
      -d "$OUT" $(find src -name "*.java")
# Les images sont chargées depuis le classpath (url('/Images/...'))
cp -r src/Images src/Elements "$OUT"/

# Lancé depuis la racine du projet : l'audio est lu via le chemin relatif src/Elements/Audio/
java --module-path "$FX_LIB" --add-modules javafx.controls,javafx.media \
     --enable-native-access=javafx.graphics \
     -cp "$OUT" Ship
