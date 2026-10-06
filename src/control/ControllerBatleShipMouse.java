package control;

import boardifier.control.*;
import boardifier.model.*;
import boardifier.model.action.ActionList;
import boardifier.view.GridLook;
import boardifier.view.View;
import javafx.event.*;
import javafx.scene.input.MouseEvent;
import model.BattleBoard;
import model.BattleShipStageModel;
import model.Ship;

import java.util.List;

public class ControllerBatleShipMouse extends ControllerMouse implements EventHandler<MouseEvent> {

    // première case cliquée pour placer un bateau {ligne, colonne}, null si pas encore cliquée
    private int[] anciantclic;

    public ControllerBatleShipMouse(Model model, View view, Controller control) {
        super(model, view, control);
    }

    // appelé au début de chaque partie
    public void reset() {
        anciantclic = null;
    }

    public void handle(MouseEvent event) {
        if (!model.isCaptureMouseEvent()) return;
        BattleShipStageModel stageModel = (BattleShipStageModel) model.getGameStage();
        if (stageModel == null) return;
        // pendant le tour d'une IA, les clics sont ignorés
        if (model.getCurrentPlayer().getType() == Player.COMPUTER) return;

        Coord2D clic = new Coord2D(event.getSceneX(), event.getSceneY());
        int idJoueur = model.getIdPlayer();

        if (!stageModel.flottePlacee(idJoueur)) {
            placement(stageModel, idJoueur, clic);
        } else if (stageModel.phaseDeTir()) {
            tir(stageModel, idJoueur, clic);
        }
    }


    /*
     * Placement : 1er clic = première case du bateau, 2e clic = direction (horizontale ou verticale).
     * Chaque joueur place ses bateaux sur sa propre grille.
     */
    private void placement(BattleShipStageModel stageModel, int idJoueur, Coord2D clic) {
        int[] caseCliquee = caseCliquee(stageModel.getBoard(idJoueur), clic);
        if (caseCliquee == null) return;

        if (anciantclic == null) {
            anciantclic = caseCliquee;
            return;
        }
        int ligne = anciantclic[0];
        int colonne = anciantclic[1];
        char sens = sens(anciantclic, caseCliquee);
        anciantclic = null;

        Ship[] ships = stageModel.getShips(idJoueur);
        Ship ship = ships[stageModel.getNbShipPlaced(idJoueur)];
        if (!stageModel.Verifpeutetreposer(ships, colonne, ligne, ship.getTaille(), sens)) {
            System.out.println("impossible de placer le bateau ici");
            return;
        }
        ship.setCordonnerShip(ligne, colonne, sens);
        stageModel.shipPlaced(idJoueur);

        ActionList actions = ((BattleShipControler) control).actionsPlacerBateau(idJoueur, ship);
        // quand toute la flotte est placée, c'est au tour de l'autre joueur
        actions.setDoEndOfTurn(stageModel.flottePlacee(idJoueur));
        jouer(actions);
        if (!stageModel.flottePlacee(idJoueur)) {
            ((BattleShipControler) control).majInfo();
        }
    }


    /*
     * Tir : le joueur 1 tire sur la grille du joueur 2 et inversement.
     */
    private void tir(BattleShipStageModel stageModel, int idJoueur, Coord2D clic) {
        int[] cible = caseCliquee(stageModel.getBoardAdverse(idJoueur), clic);
        if (cible == null) return;
        BattleShipControler controleur = (BattleShipControler) control;
        if (controleur.estEnLigne()) {
            // en ligne : le résultat dépend de la flotte de l'adversaire, il arrivera par le réseau
            controleur.tirerEnLigne(cible[0], cible[1]);
            return;
        }
        ActionList actions = ((BattleShipControler) control).tirer(idJoueur, cible[0], cible[1]);
        if (actions == null) return; // case déjà visée
        jouer(actions);
    }


    private void jouer(ActionList actions) {
        // bloque les clics tout de suite (sinon un double-clic rapide pourrait jouer deux fois
        // avant que l'ActionPlayer ne démarre) ; l'ActionPlayer les réactive à la fin
        model.setCaptureEvents(false);
        ActionPlayer play = new ActionPlayer(model, control, actions);
        play.start();
    }


    /*
     * Renvoie {ligne, colonne} de la case cliquée sur la grille donnée, ou null si le clic est ailleurs
     */
    private int[] caseCliquee(BattleBoard board, Coord2D clic) {
        List<GameElement> list = control.elementsAt(clic);
        if (!list.contains(board)) return null;
        GridLook lookBoard = (GridLook) control.getElementLook(board);
        // null si le clic est sur le bord de la grille (lettres, chiffres, cadre)
        return lookBoard.getCellFromSceneLocation(clic);
    }


    /*
     * méthode permetant de conaitre le sens du bateau à partir de deux cases {ligne, colonne}
     */
    public char sens(int[] clic1, int[] clic2) {
        int deltaLignes = Math.abs(clic1[0] - clic2[0]);
        int deltaColonnes = Math.abs(clic1[1] - clic2[1]);

        if (deltaColonnes >= deltaLignes) {
            return 'H'; // horizontal si on s'est plus déplacé en colonnes qu'en lignes
        } else {
            return 'V'; // vertical sinon
        }

    }



}
