package control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.control.Logger;
import boardifier.model.*;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import javafx.scene.control.Alert;
import model.BattleShipStageModel;
import model.ConfigPartie;
import model.Missille;
import model.Ship;
import model.shipPart;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class BattleShipControler extends Controller {

    // niveau de l'IA de chaque joueur (0 = humain, 1 = IA facile, 2 = IA difficile)
    int[] levelbot = new int[2];
    // joueur qui tire en premier une fois les bateaux placés (0 = joueur 1, 1 = joueur 2)
    private int premierTireur;
    // false pendant la phase de placement des bateaux
    private boolean tirCommence;
    // vrai quand le joueur courant a tiré pendant son tour (modifié par le thread de l'IA)
    private volatile boolean tirEffectue;
    // action à exécuter pour revenir à la page d'accueil (menu "Intro")
    private Runnable retourAccueil;
    private final AudioController audio = new AudioController();
    // partie en ligne en cours, null pour une partie locale
    private PartieEnLigne enLigne;

    // mémoire des IA (J1 = IA du joueur 1, J2 = IA du joueur 2), remise à zéro à chaque partie
    Set<Point> potentialTargetsJ1;
    Set<Point> potentialTargetsJ2;
    boolean targetModeJ1;
    boolean targetModeJ2;
    boolean lineModeJ1;
    boolean lineModeJ2;
    int[][] gridJ1;
    int[][] gridJ2;
    List<Point> lineTargetsJ1;
    List<Point> lineTargetsJ2;
    List<Point> hitJ1;
    List<Point> hitJ2;
    List<Point> shipPartJ1;
    List<Point> shipPartJ2;


    public BattleShipControler(Model model, View view) {
        super(model, view);
        setControlKey(new ControllerBattleShipkey(model, view, this));
        setControlMouse(new ControllerBatleShipMouse(model, view, this));
        setControlAction (new ControllerBattleShipAction(model, view, this));
        resetIA();
    }

    private void resetIA() {
        potentialTargetsJ1 = new HashSet<>();
        potentialTargetsJ2 = new HashSet<>();
        targetModeJ1 = false;
        targetModeJ2 = false;
        lineModeJ1 = false;
        lineModeJ2 = false;
        gridJ1 = new int[10][10];
        gridJ2 = new int[10][10];
        lineTargetsJ1 = new ArrayList<>();
        lineTargetsJ2 = new ArrayList<>();
        hitJ1 = new ArrayList<>();
        hitJ2 = new ArrayList<>();
        shipPartJ1 = new ArrayList<>();
        shipPartJ2 = new ArrayList<>();
    }

    public void setRetourAccueil(Runnable retourAccueil) {
        this.retourAccueil = retourAccueil;
    }

    public void retourAccueil() {
        if (retourAccueil != null) retourAccueil.run();
    }

    public void setEnLigne(PartieEnLigne enLigne) {
        this.enLigne = enLigne;
    }

    public boolean estEnLigne() {
        return enLigne != null;
    }


    /*
     * Démarre (ou redémarre, ex : bouton "New Game" de fin de partie) une partie
     * avec les options choisies sur la page de sélection.
     */
    @Override
    public void startGame() throws GameException {
        // les joueurs sont créés par la page de sélection : sans eux on ne peut pas jouer
        if (model.getPlayers().size() != 2) return;
        // en ligne, pas de "nouvelle partie" depuis le menu : il faut repasser par le lobby
        if (enLigne != null && model.isStageStarted()) return;

        resetIA();
        tirCommence = false;
        tirEffectue = false;
        ((ControllerBatleShipMouse) controlMouse).reset();

        levelbot[0] = ConfigPartie.niveauIA[0];
        levelbot[1] = ConfigPartie.niveauIA[1];
        premierTireur = (ConfigPartie.premierTireur == 2) ? new Random().nextInt(2) : ConfigPartie.premierTireur;

        super.startGame();
        if (enLigne != null) {
            // en ligne, les deux joueurs placent leurs bateaux en même temps, chacun chez soi
            model.setIdPlayer(enLigne.monId);
        }
        majInfo();
        lancerIASiBesoin();
    }


    public void endOfTurn() {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        // partie arrêtée entre-temps (menu "Intro")
        if (stageModel == null) return;
        int id = model.getIdPlayer();
        // une fin de tour n'est valable que si le joueur courant a vraiment joué : sinon elle vient
        // d'une action d'une partie précédente (arrêtée ou relancée depuis le menu) et on l'ignore
        boolean aJoue = tirCommence ? tirEffectue : stageModel.flottePlacee(id);
        if (!aJoue) return;
        tirEffectue = false;

        if (!tirCommence && enLigne != null) {
            // ma flotte est placée : on prévient l'adversaire, on tire quand il est prêt aussi
            enLigne.envoyerPret();
            if (stageModel.flottePlacee(enLigne.idAdversaire)) {
                demarrerTir();
            } else {
                majInfo();
            }
            return;
        }

        if (!tirCommence) {
            // le joueur vient de finir de placer ses bateaux : on les cache à un adversaire humain
            if (estHumain(1 - id)) {
                stageModel.setinvisiblebateau(stageModel.getShips(id));
            }
            if (stageModel.phaseDeTir()) {
                tirCommence = true;
                model.setIdPlayer(premierTireur);
            } else {
                model.setNextPlayer();
            }
        } else {
            model.setNextPlayer();
            // si ce joueur n'a plus de missiles, l'autre continue de tirer
            if (stageModel.getMissilesRestants(model.getIdPlayer()) <= 0) {
                model.setNextPlayer();
            }
        }

        majInfo();
        lancerIASiBesoin();
        if (enLigne != null) {
            // l'adversaire a peut-être déjà tiré pendant que mon tir s'affichait
            enLigne.traiterTirEnAttente();
        }
    }

    // arrêt de la partie synchronisé avec le tour de l'IA (voir BattleShipDecider.decide)
    @Override
    public synchronized void stopGame() {
        if (enLigne != null) {
            // en ligne : on quitte la partie sur le serveur, l'adversaire est prévenu
            enLigne.quitter();
            enLigne = null;
        }
        super.stopGame();
    }

    // le joueur courant a joué son tour sans tirer (cas extrême : plus aucune case disponible)
    void tourJoue() {
        tirEffectue = true;
    }

    private boolean estHumain(int idJoueur) {
        return model.getPlayers().get(idJoueur).getType() == Player.HUMAN;
    }

    private void lancerIASiBesoin() {
        if (model.isEndGame() || model.isEndStage() || enLigne != null) return;
        Player p = model.getCurrentPlayer();
        if (p.getType() == Player.COMPUTER) {
            int id = model.getIdPlayer();
            Logger.debug("COMPUTER PLAYS");
            BattleShipDecider decider = new BattleShipDecider(model, this, id, levelbot[id]);
            ActionPlayer play = new ActionPlayer(model, this, decider, null);
            play.start();
        }
        else {
            Logger.debug("PLAYER PLAYS");
        }
    }


    /*
     * Met à jour le texte d'information sous les grilles (qui joue, quoi faire, score)
     */
    public void majInfo() {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        if (stageModel == null || stageModel.getInfoPartie() == null) return;
        int id = model.getIdPlayer();
        String joueur = model.getCurrentPlayerName();
        String texte;
        String score = "    |    missiles : J1 = " + stageModel.getPlayer1ToPlay() + ", J2 = " + stageModel.getPlayer2ToPlay()
                + "    |    touches : J1 = " + stageModel.getTouches(0) + ", J2 = " + stageModel.getTouches(1);
        if (!stageModel.flottePlacee(id)) {
            Ship ship = stageModel.getShips(id)[stageModel.getNbShipPlaced(id)];
            texte = joueur + " : placez votre bateau de " + ship.getTaille() + " case(s) sur la grille "
                    + (id == 0 ? "de gauche" : "de droite")
                    + " (cliquez sa première case, puis une case dans la direction voulue)";
        } else if (!tirCommence) {
            texte = "En attente de " + (enLigne != null ? enLigne.pseudoAdversaire : "l'adversaire")
                    + " : il place encore ses bateaux...";
        } else if (enLigne != null && id == enLigne.idAdversaire) {
            texte = "Au tour de " + enLigne.pseudoAdversaire + "..." + score;
        } else if (enLigne != null && enLigne.attendResultat()) {
            texte = "Tir envoyé, en attente de la réponse de " + enLigne.pseudoAdversaire + "..." + score;
        } else {
            texte = joueur + " : tirez sur la grille " + (id == 0 ? "de droite" : "de gauche") + score;
        }
        stageModel.getInfoPartie().setText(texte);
    }


    /*
     * Actions qui posent toutes les parties d'un bateau (dont les coordonnées sont déjà fixées)
     * sur la grille de son joueur, en une seule liste (donc un seul ActionPlayer).
     */
    public ActionList actionsPlacerBateau(int idJoueur, Ship ship) {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        String boardName = stageModel.getBoard(idJoueur).getName();
        ActionList actions = new ActionList();
        for (shipPart part : ship.getshippart()) {
            actions.addAll(ActionFactory.generatePutInContainer(this, model, part, boardName, part.getcordonneY(), part.getcordonneX()));
        }
        return actions;
    }


    /*
     * Prépare le tir du joueur idJoueur sur la case (ligne, colonne) de la grille adverse.
     * Renvoie null si le tir est impossible (plus de missile ou case déjà visée).
     */
    public ActionList tirer(int idJoueur, int ligne, int colonne) {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        if (stageModel.getMissilesRestants(idJoueur) <= 0 || stageModel.dejaTire(idJoueur, ligne, colonne)) return null;
        Missille missile = (Missille) stageModel.getStockMissile(idJoueur).getElement(0, 0);
        if (missile == null) return null;

        stageModel.marquerTir(idJoueur, ligne, colonne);
        tirEffectue = true;
        if (stageModel.toucheroupas(stageModel.getShipsAdverse(idJoueur), colonne, ligne)) {
            missile.setColor(2);
            stageModel.ajouterTouche(idJoueur);
        } else {
            audio.playMiss();
        }
        ActionList actions = ActionFactory.generatePutInContainer(this, model, missile, stageModel.getBoardAdverse(idJoueur).getName(), ligne, colonne);
        actions.setDoEndOfTurn(true);
        return actions;
    }

  

    // ================================================================ partie en ligne

    // l'adversaire a fini de placer ses bateaux
    void adversairePret() {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        if (stageModel == null || enLigne == null || tirCommence) return;
        stageModel.marquerFlottePlacee(enLigne.idAdversaire);
        if (stageModel.flottePlacee(enLigne.monId)) {
            demarrerTir();
        } else {
            majInfo();
        }
    }

    // les deux flottes sont placées : début des tirs
    private void demarrerTir() {
        tirCommence = true;
        tirEffectue = false;
        model.setIdPlayer(premierTireur);
        majInfo();
    }

    boolean tourDeLAdversaire() {
        return enLigne != null && tirCommence && model.getGameStage() != null && !model.isEndGame()
                && model.getIdPlayer() == enLigne.idAdversaire;
    }

    // je tire sur la grille adverse : le résultat arrivera par le réseau (voir appliquerResultat)
    void tirerEnLigne(int ligne, int colonne) {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        int moi = enLigne.monId;
        if (enLigne.attendResultat() || stageModel.getMissilesRestants(moi) <= 0 || stageModel.dejaTire(moi, ligne, colonne)) return;
        stageModel.marquerTir(moi, ligne, colonne);
        // plus de clics jusqu'à la réponse (l'ActionPlayer qui affichera le tir les réactivera)
        model.setCaptureEvents(false);
        enLigne.tirer(ligne, colonne);
        majInfo();
    }

    /*
     * Réponse de l'adversaire à mon tir.
     * cases : {ligne, colonne} de chaque partie du bateau touché s'il est coulé, sinon null
     */
    void appliquerResultat(int ligne, int colonne, boolean touche, int[][] cases) {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        if (stageModel == null) return;
        int moi = enLigne.monId;
        int adversaire = enLigne.idAdversaire;
        Missille missile = (Missille) stageModel.getStockMissile(moi).getElement(0, 0);
        if (missile == null) return;

        ActionList actions = new ActionList();
        if (touche) {
            missile.setColor(2);
            stageModel.ajouterTouche(moi);
        } else {
            audio.playMiss();
        }
        if (cases != null) {
            // bateau coulé : on le pose (révélé, en vert) sur la grille adverse
            Ship ship = stageModel.bateauNonCouleDeTaille(stageModel.getShips(adversaire), cases.length);
            if (ship != null) {
                for (int i = 0; i < cases.length; i++) {
                    shipPart part = ship.getshippart()[i];
                    part.setCordoner(cases[i][1], cases[i][0]);
                    part.setToucher(true);
                }
                ship.orienterParties();
                ship.verifcouler();
                actions.addAll(actionsPlacerBateau(adversaire, ship));
            }
        }
        actions.addAll(ActionFactory.generatePutInContainer(this, model, missile, stageModel.getBoardAdverse(moi).getName(), ligne, colonne));
        actions.setDoEndOfTurn(true);
        tirEffectue = true;
        new ActionPlayer(model, this, actions).start();
    }

    /*
     * L'adversaire tire sur ma grille : on l'affiche et on renvoie le message RESULTAT à lui envoyer
     * (null si le tir est invalide).
     */
    Map<String, Object> recevoirTir(int ligne, int colonne) {
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        int t = BattleShipStageModel.TAILLE_GRILLE;
        if (stageModel == null || ligne < 0 || ligne >= t || colonne < 0 || colonne >= t) return null;
        ActionList actions = tirer(enLigne.idAdversaire, ligne, colonne);
        if (actions == null) return null;

        Ship ship = stageModel.bateauA(stageModel.getShips(enLigne.monId), colonne, ligne);
        boolean coule = ship != null && ship.getcouler();
        List<Object> cases = new ArrayList<>();
        if (coule) {
            for (shipPart part : ship.getshippart()) {
                cases.add(new int[]{part.getcordonneY(), part.getcordonneX()});
            }
        }
        new ActionPlayer(model, this, actions).start();
        return reseau.Json.objet("type", "RESULTAT", "ligne", ligne, "colonne", colonne,
                "touche", ship != null, "coule", coule, "cases", cases);
    }

    // l'adversaire est parti ou la connexion est perdue
    void partieInterrompue(String raison) {
        enLigne = null;
        stopGame();
        view.resetView();
        retourAccueil();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(view.getStage());
        alert.setHeaderText("Partie interrompue");
        alert.setContentText(raison);
        alert.show();
    }

    // fin de partie : en ligne, pas de "New Game" (il faut repasser par le lobby)
    @Override
    public void endGame() {
        if (enLigne == null) {
            super.endGame();
            return;
        }
        enLigne.marquerFinie();
        model.setCaptureEvents(false);
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        int gagnant = model.getIdWinner();
        String message;
        if (gagnant == -1) {
            message = "Match nul";
        } else if (gagnant == enLigne.monId) {
            message = "Vous avez gagné !";
        } else {
            message = enLigne.pseudoAdversaire + " a gagné...";
        }
        String score = "";
        if (stageModel != null) {
            score = "Touches : vous " + stageModel.getTouches(enLigne.monId)
                    + ", " + enLigne.pseudoAdversaire + " " + stageModel.getTouches(enLigne.idAdversaire);
            stageModel.getInfoPartie().setText(message + "    |    " + score);
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(view.getStage());
        alert.setHeaderText(message);
        alert.setContentText(score);
        // on ne quitte la partie qu'après la fermeture du message : l'adversaire a ainsi le temps
        // d'afficher son propre résultat avant d'être prévenu de notre départ.
        // (show + setOnHidden plutôt que showAndWait : pas de boucle d'événements imbriquée)
        alert.setOnHidden(e -> {
            stopGame();
            view.resetView();
            retourAccueil();
        });
        alert.show();
    }
}
