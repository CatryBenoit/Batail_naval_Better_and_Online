# Serveur du jeu en ligne (lobby + relais des messages). Le jeu lui-même (JavaFX) n'est pas dans l'image.
#
#   docker build -t bataille-navale-serveur .
#   docker run -p 8765:8765 bataille-navale-serveur
#
# Le port d'écoute vient de la variable PORT (fournie automatiquement par Render, Railway, etc.).

# --- étape 1 : compilation (seuls les paquets reseau et serveur sont nécessaires)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY src/reseau ./src/reseau
COPY src/serveur ./src/serveur
RUN javac -d classes src/reseau/*.java src/serveur/*.java \
 && jar --create --file serveur.jar --main-class serveur.ServeurBatailleNavale -C classes .

# --- étape 2 : image d'exécution, plus légère (JRE seul)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/serveur.jar .
# pas besoin des droits root pour faire tourner le serveur
RUN useradd --system --no-create-home serveur
USER serveur
ENV PORT=8765
EXPOSE 8765
CMD ["java", "-jar", "serveur.jar"]
