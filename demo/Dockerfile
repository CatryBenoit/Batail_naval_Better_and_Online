# Démo du jeu dans le navigateur : le jeu JavaFX tourne sur un écran virtuel (Xvfb)
# et s'affiche dans une page web grâce à noVNC.
#
#   docker build -f demo/Dockerfile -t bataille-navale-demo .
#   docker run -p 6080:6080 bataille-navale-demo      puis http://localhost:6080/

# --- étape 1 : compilation (le bytecode est le même quelle que soit l'architecture)
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
ARG TARGETARCH
ARG JAVAFX=21.0.5
WORKDIR /build
# JavaFX contient du code natif : on prend les jars de l'architecture de l'image finale
RUN set -e; \
    case "$TARGETARCH" in arm64) plateforme=linux-aarch64 ;; *) plateforme=linux ;; esac; \
    mkdir lib; \
    for m in base graphics controls media; do \
      curl -fsSL -o lib/javafx-$m.jar \
        https://repo1.maven.org/maven2/org/openjfx/javafx-$m/$JAVAFX/javafx-$m-$JAVAFX-$plateforme.jar; \
    done
COPY src ./src
RUN javac -cp "lib/*" -d classes $(find src -name '*.java') \
 && cp -r src/Images src/Elements classes/

# --- étape 2 : écran virtuel + serveur VNC + noVNC
FROM eclipse-temurin:21-jre
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      xvfb matchbox-window-manager x11vnc novnc websockify libgtk-3-0t64 libgl1 libxtst6 fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/* \
 && cp -r /usr/share/novnc /opt/novnc
COPY demo/index.html /opt/novnc/index.html
COPY demo/demarrer.sh /usr/local/bin/demarrer.sh
COPY --from=build /build/lib /app/lib
COPY --from=build /build/classes /app/classes
# dossier des sockets X : Xvfb ne peut pas le créer sans être root
RUN chmod +x /usr/local/bin/demarrer.sh \
 && mkdir -p /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix \
 && useradd --system --create-home joueur
USER joueur
WORKDIR /app
# taille de l'écran virtuel (le jeu est mis à l'échelle pour le remplir)
ENV RESOLUTION=1280x800
EXPOSE 6080
CMD ["demarrer.sh"]
