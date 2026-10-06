<!-- PORTFOLIO_CONFIG
system: Scolaire
tech: Java / JavaFX
desc: Jeu de Bataille Navale en Java/JavaFX, projet scolaire repris et amélioré : nouveaux graphismes, IA à deux niveaux et mode en ligne via un petit serveur HTTP sans dépendance.
color: #0f766e
-->

# Bataille Navale — Better & Online

Le jeu de plateau Bataille Navale en Java avec JavaFX.

Le projet de départ est un projet scolaire, réalisé à 4 d'avril à juin 2024. Je l'ai repris ensuite pour :

- **refaire les graphismes** : mer texturée, navires vus de dessus, bombe, explosion et plouf animés ;
- **ajouter le jeu en ligne** : un lobby pour créer ou rejoindre une partie contre un joueur sur une autre machine ;
- **ajouter des tests** pour le réseau et le serveur.

## Fonctionnalités

- Grilles de 10 × 10, placement des navires à la souris.
- Deux modes de jeu :
  - **Mode 1** : 5 navires (5, 4, 3, 3 et 2 cases) ;
  - **Mode 2** : 10 navires (4, 3, 3, 2, 2, 2, 1, 1, 1 et 1 case).
- Chaque joueur peut être **humain**, **IA facile** ou **IA difficile**, ce qui permet aussi de faire jouer deux IA l'une contre l'autre.
- Choix du premier joueur (joueur 1, joueur 2 ou au hasard) et du nombre de missiles.
- Partie **en ligne** à deux joueurs, via un serveur de lobby.
- Effets sonores et statistiques des joueurs.

## Prérequis

- **JDK 21 ou plus récent**
- **SDK JavaFX**, uniquement pour le jeu : <https://gluonhq.com/products/javafx/>

## Lancer le jeu

```bash
JAVAFX_HOME=/chemin/vers/javafx-sdk ./run.sh
```

`run.sh` compile toutes les sources dans `build/`, copie les images et les sons, puis lance le jeu (classe principale `Ship`).

## Jouer en ligne

Un petit serveur HTTP en Java, sans aucune dépendance, sert de lobby et relaie les messages entre les deux joueurs. Chaque joueur garde sa flotte sur sa propre machine : seuls les tirs et leurs résultats passent par le serveur.

### 1. Démarrer le serveur

Lancez-le sur une machine que les deux joueurs peuvent joindre. Le port par défaut est `8765` : vous pouvez le passer en argument, ou par la variable `PORT`.

```bash
./serveur.sh            # compile et lance sur le port 8765
./serveur.sh 9000       # sur un autre port
./serveur.sh --jar      # fabrique seulement build-serveur/serveur.jar
```

Le jar se lance ensuite sur n'importe quelle machine équipée d'un JRE 21 : `java -jar serveur.jar [port]`.

**Avec Docker** (l'image ne contient que le serveur, sans le jeu) :

```bash
docker build -t bataille-navale-serveur .
docker run -p 8765:8765 bataille-navale-serveur
```

L'image lit le port dans la variable `PORT`. Elle peut donc être déployée telle quelle sur un hébergeur comme Render ou Railway.

### 2. Rejoindre une partie

1. Dans le jeu, cliquez sur **Play Online**.
2. Indiquez l'adresse du serveur (par exemple `localhost:8765`, `192.168.1.20:8765` ou `https://mon-serveur.exemple.com`) et un pseudo.
3. Le premier joueur choisit le mode, le premier tireur et le nombre de missiles, puis clique sur **Créer une partie**.
4. Le second joueur sélectionne cette partie dans la liste et clique sur **Rejoindre**.

### API du serveur

Toutes les requêtes et réponses sont en JSON.

| Requête | Rôle |
|---|---|
| `POST /games` `{pseudo, mode, missiles, premier}` | créer une partie |
| `GET /games` | lister les parties en attente |
| `POST /games/{id}/join` `{pseudo}` | rejoindre une partie |
| `POST /games/{id}/messages` (en-tête `X-Token`) | envoyer un message à l'adversaire |
| `GET /games/{id}/messages?apres=N` (en-tête `X-Token`) | recevoir les messages (long-polling, attente jusqu'à 20 s) |
| `DELETE /games/{id}` (en-tête `X-Token`) | quitter la partie |

Un joueur qui n'a envoyé aucune requête depuis 45 s est considéré comme parti. Le serveur fait le ménage dans les parties abandonnées toutes les 5 s.

## Structure du projet

```
src/
├── Ship.java          point d'entrée du jeu
├── boardifier/        mini-framework de jeu de plateau (modèle / vue / contrôleur), fourni pour le projet scolaire
├── model/             règles : grilles, navires, missiles, configuration de la partie
├── view/              pages JavaFX (accueil, sélection, lobby, plateau) et rendu des sprites
├── control/           contrôleurs, IA (BattleShipDecider), partie en ligne, audio
├── reseau/            client HTTP et petit lecteur/écrivain JSON (partagés avec le serveur)
├── serveur/           serveur de lobby et de relais (ServeurBatailleNavale)
├── Images/            fonds, navires et effets
└── Elements/Audio/    sons
Test/
├── TestModel/         tests unitaires du modèle
└── TestReseau/        tests du JSON et du serveur (lance un vrai serveur et deux clients)
outils/                scripts de génération des images
```

## Tests

Les tests utilisent **JUnit 5**. Le projet n'a pas d'outil de build (Maven ou Gradle) : on les lance depuis l'IDE, par exemple IntelliJ IDEA, en marquant `Test/` comme dossier de tests.

## Régénérer les images

Les navires, la mer, la bombe, l'explosion et le plouf sont des images générées (vue de dessus, sur fond vert), rangées dans `outils/sources/`. Le script `outils/generer_images.sh` les transforme en sprites du jeu dans `src/Images/navires/` et `src/Images/effets/`. Il a besoin de Python et de Pillow.

```bash
./outils/generer_images.sh
```

Le script s'appuie sur deux outils :

- `outils/decouper_navire.py` détoure un navire et le découpe en cases, en le raccourcissant pour les petits bateaux ;
- `outils/preparer_effets.py` rend la texture de mer répétable sans raccord et détoure les effets.
