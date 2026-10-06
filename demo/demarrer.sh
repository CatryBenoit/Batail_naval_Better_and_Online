#!/bin/sh
# Lance l'écran virtuel, le serveur VNC, noVNC, puis le jeu (relancé s'il est fermé).
set -e
export DISPLAY=:0
# arrêt du conteneur (par Sablier) : on arrête tout de suite au lieu d'attendre que Docker tue le conteneur
trap 'kill $(jobs -p) 2>/dev/null || true; exit 0' TERM INT

# Sablier redémarre le même conteneur : on enlève le verrou de l'écran laissé par le lancement précédent
rm -f /tmp/.X0-lock /tmp/.X11-unix/X0
Xvfb :0 -screen 0 "${RESOLUTION}x24" -nolisten tcp &
while [ ! -e /tmp/.X11-unix/X0 ]; do sleep 0.1; done

# gestionnaire de fenêtres minimal : il applique le plein écran demandé par le jeu
matchbox-window-manager -use_titlebar no &

# VNC sans mot de passe mais seulement en local : on n'y accède que par noVNC.
# -bg : x11vnc passe en arrière-plan une fois prêt, donc noVNC (et le healthcheck) ne démarre qu'après
x11vnc -display :0 -localhost -rfbport 5900 -forever -shared -nopw -noxdamage -quiet -bg
websockify --web /opt/novnc 6080 localhost:5900 &

while true; do
    java --module-path lib --add-modules javafx.controls,javafx.media \
         --enable-native-access=javafx.graphics \
         -Dprism.order=sw -Dbataille.pleinEcran=true -cp classes Ship &
    wait $! || true
    sleep 1
done
