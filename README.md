<!-- PORTFOLIO_CONFIG
system: Scolaire
tech: Java / JavaFX
desc: Projet scolaire de développement du jeu Bataille Navale en Java avec gestion des grilles, des bateaux, des tirs et des interactions joueur via une architecture orientée objet.
color: #0f766e
-->


# bataille navale

Projet consistant à recréer le jeu de plateau bataille navale avec Java.
Réalisé en groupe de 4 de avril à juin 2024.

/!\ selon la version java peux ne pas fonctionner 

## Lancer le jeu

Il faut un JDK 21+ et le SDK JavaFX (https://gluonhq.com/products/javafx/).

```bash
JAVAFX_HOME=/chemin/vers/javafx-sdk ./run.sh
```

## Jouer en ligne

Un petit serveur (Java, sans dépendance) sert de lobby et relaie les messages entre les deux joueurs.
Chaque joueur garde sa flotte chez lui : seuls les tirs et leurs résultats passent par le serveur.

1. Lancer le serveur, sur une machine joignable par les deux joueurs (port par défaut : 8765) :
   ```bash
   ./serveur.sh            # ou : ./serveur.sh 9000
   ./serveur.sh --jar      # fabrique build-serveur/serveur.jar, à lancer ailleurs avec : java -jar serveur.jar
   ```
2. Dans le jeu : **Play Online**, indiquer l'adresse du serveur (ex. `localhost:8765`, `192.168.1.20:8765`
   ou `https://mon-serveur.exemple.com`) et un pseudo.
3. Le premier joueur clique sur **Créer une partie** ; le second la choisit dans la liste et clique sur **Rejoindre**.

API du serveur (JSON) :

| Requête | Rôle |
|---|---|
| `POST /games` `{pseudo, mode, missiles, premier}` | créer une partie |
| `GET /games` | lister les parties en attente |
| `POST /games/{id}/join` `{pseudo}` | rejoindre une partie |
| `POST /games/{id}/messages` (en-tête `X-Token`) | envoyer un message à l'adversaire |
| `GET /games/{id}/messages?apres=N` (en-tête `X-Token`) | recevoir les messages (attend jusqu'à 20 s) |
| `DELETE /games/{id}` (en-tête `X-Token`) | quitter la partie |

## Images du plateau

Les navires, la mer, la bombe, l'explosion et le plouf viennent d'images générées (vue de dessus, fond vert)
rangées dans `outils/sources/` :

- `outils/decouper_navire.py` détoure un navire et le découpe en cases (en le raccourcissant pour les petits bateaux) ;
- `outils/preparer_effets.py` rend la texture de mer répétable sans raccord et détoure les effets ;
- `outils/generer_images.sh` régénère toutes les images du jeu dans `src/Images/navires/` et `src/Images/effets/`
  (Pillow requis).

```bash
./outils/generer_images.sh
```
