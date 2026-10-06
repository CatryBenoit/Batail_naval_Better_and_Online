package control;

import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.BattleShipStageModel;
import model.Ship;
import model.shipPart;

import java.awt.Point;
import java.util.*;

public class BattleShipDecider extends Decider {
    private int[][] gridP1;
    private int[][] gridP2;
    private Random random;
    private BattleShipStageModel battleShipStageModel;
    private BattleShipControler battleShipControler;
    private int id_Bot;
    private int levelBot;

    public BattleShipDecider(Model model, Controller control, int id, int level) {
        super(model, control);
        this.id_Bot = id;
        this.battleShipStageModel = (BattleShipStageModel) model.getGameStage();
        this.battleShipControler = (BattleShipControler) control;
        this.levelBot = level;
        gridP1 = battleShipControler.gridJ1;
        gridP2 = battleShipControler.gridJ2;
        random = new Random();
    }

    public void markMiss(Point p) {
        gridP1[p.y][p.x] = 2; // Mark as miss
    }
    public void markMiss2(Point p) {
        gridP2[p.y][p.x] = 2; // Mark as miss
    }

    public void markHit(Point p) {
        gridP1[p.y][p.x] = 3; // Mark as hit
        battleShipControler.hitJ1.add(p);
        updatePotentialTargets(p);
        battleShipControler.targetModeJ1 = true; // Switch to target mode
    }

    public void markHit2(Point p) {
        gridP2[p.y][p.x] = 3; // Mark as hit
        battleShipControler.hitJ2.add(p);
        updatePotentialTargets2(p);
        battleShipControler.targetModeJ2 = true; // Switch to target mode
    }

    public void markSunk(Point p) {
        for (Point hit : battleShipControler.hitJ1) {
            if (gridP1[hit.y][hit.x] == 3) {
                gridP1[hit.y][hit.x] = 4; // Mark as sunk
            }
        }
        battleShipControler.hitJ1.clear(); // Clear hits

        getSurroundingPoints(battleShipControler.shipPartJ1, gridP1);
        battleShipControler.shipPartJ1.clear();

        battleShipControler.targetModeJ1 = false; // Switch back to random mode
        battleShipControler.lineModeJ1 = false; // Switch off-line mode
        battleShipControler.potentialTargetsJ1.clear(); // Clear potential targets
        battleShipControler.lineTargetsJ1.clear(); // Clear line targets
    }
    public void markSunk2(Point p) {
        for (Point hit : battleShipControler.hitJ2) {
            if (gridP2[hit.y][hit.x] == 3) {
                gridP2[hit.y][hit.x] = 4; // Mark as sunk
            }
        }
        battleShipControler.hitJ2.clear(); // Clear hits

        getSurroundingPoints(battleShipControler.shipPartJ2, gridP2);
        battleShipControler.shipPartJ2.clear();

        battleShipControler.targetModeJ2 = false; // Switch back to random mode
        battleShipControler.lineModeJ2 = false; // Switch off-line mode
        battleShipControler.potentialTargetsJ2.clear(); // Clear potential targets
        battleShipControler.lineTargetsJ2.clear(); // Clear line targets
    }

    private void updatePotentialTargets(Point p) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            Point adjacent = new Point(p.x + dir[0], p.y + dir[1]);
            if (isValidPosition(adjacent) && gridP1[adjacent.y][adjacent.x] == 0) {
                battleShipControler.potentialTargetsJ1.add(adjacent);
            }
        }
    }
    private void updatePotentialTargets2(Point p) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            Point adjacent = new Point(p.x + dir[0], p.y + dir[1]);
            if (isValidPosition(adjacent) && gridP2[adjacent.y][adjacent.x] == 0) {
                battleShipControler.potentialTargetsJ2.add(adjacent);
            }
        }
    }
    private Point getLineModeShot() {
        while (!battleShipControler.lineTargetsJ1.isEmpty()) {
            Point p = battleShipControler.lineTargetsJ1.remove(0);
            if (gridP1[p.y][p.x] == 0) return p;
        }
        battleShipControler.lineModeJ1 = false;
        battleShipControler.targetModeJ1 = true; // Switch back to target mode
        return getTargetModeShot();
    }
    private Point getLineModeShot2() {
        while (!battleShipControler.lineTargetsJ2.isEmpty()) {
            Point p = battleShipControler.lineTargetsJ2.remove(0);
            if (gridP2[p.y][p.x] == 0) return p;
        }
        battleShipControler.lineModeJ2 = false;
        battleShipControler.targetModeJ2 = true; // Switch back to target mode
        return getTargetModeShot2();
    }
    /*
    get next target permet de cibler un point sur la grille en fonction des modes de tirs
     */
    public Point getNextTarget() {
        if (battleShipControler.lineModeJ1) {
            return getLineModeShot(); // mode ligne
        }else if (battleShipControler.targetModeJ1) {
            return getTargetModeShot(); // mode target
        } else {
            Point randomTarget = getRandomTarget(); // mode random
            if (randomTarget != null) {
                return randomTarget;
            } else {
                return getRandomTarget();
            }
        }
    }

    public Point getNextTarget2() {
        if (battleShipControler.lineModeJ2) {
            return getLineModeShot2();
        }else if (battleShipControler.targetModeJ2) {
            return getTargetModeShot2();
        } else {
            Point randomTarget = getRandomTarget2();
            if (randomTarget != null) {
                return randomTarget;
            } else {
                return getRandomTarget2();
            }
        }
    }
    /*
    getRandomTarget permet d'obtenir un point random sur la grille
     */
    private Point getRandomTarget() {
        List<Point> availableTargets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (gridP1[i][j] == 0) {
                    availableTargets.add(new Point(j, i));
                }
            }
        }
        if (availableTargets.isEmpty()) {
            return null;
        }
        return availableTargets.get(random.nextInt(availableTargets.size()));
    }
    private Point getRandomTarget2() {
        List<Point> availableTargets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (gridP2[i][j] == 0) {
                    availableTargets.add(new Point(j, i));
                }
            }
        }
        if (availableTargets.isEmpty()) {
            return null;
        }
        return availableTargets.get(random.nextInt(availableTargets.size()));
    }
    /*
    getTargetModeShot permet d'obtenir un point selon le mode target
     */
    private Point getTargetModeShot() {
        while (!battleShipControler.potentialTargetsJ1.isEmpty()) {
            Point nextTarget = battleShipControler.potentialTargetsJ1.iterator().next();
            battleShipControler.potentialTargetsJ1.remove(nextTarget);
            if (gridP1[nextTarget.y][nextTarget.x] == 0) return nextTarget;
        }
        battleShipControler.targetModeJ1 = false; // No potential targets, switch back to random mode
        return getRandomTarget();
    }
    private Point getTargetModeShot2() {
        while (!battleShipControler.potentialTargetsJ2.isEmpty()) {
            Point nextTarget = battleShipControler.potentialTargetsJ2.iterator().next();
            battleShipControler.potentialTargetsJ2.remove(nextTarget);
            if (gridP2[nextTarget.y][nextTarget.x] == 0) return nextTarget;
        }
        battleShipControler.targetModeJ2 = false; // No potential targets, switch back to random mode
        return getRandomTarget2();
    }

    private boolean isValidPosition(Point p) {
        return p.x >= 0 && p.x < 10 && p.y >= 0 && p.y < 10;
    }

    public void printGrid() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.print(gridP1[i][j] + " ");
            }
            System.out.println();
        }
    }
    public void pointSurLigneJ2(){
        Point lastHit = battleShipControler.hitJ2.get(battleShipControler.hitJ2.size() - 1);
        Point secondLastHit = battleShipControler.hitJ2.get(battleShipControler.hitJ2.size() - 2);
        if (lastHit.x == secondLastHit.x) {
            // Vertical line
            if (isValidPosition(new Point(lastHit.x, lastHit.y - 1)) && gridP2[lastHit.y - 1][lastHit.x] == 0) {
                battleShipControler.lineTargetsJ2.add(new Point(lastHit.x, lastHit.y - 1));
            }
            if (isValidPosition(new Point(lastHit.x, lastHit.y + 1)) && gridP2[lastHit.y + 1][lastHit.x] == 0) {
                battleShipControler.lineTargetsJ2.add(new Point(lastHit.x, lastHit.y + 1));
            }
        } else if (lastHit.y == secondLastHit.y) {
            // Horizontal line
            if (isValidPosition(new Point(lastHit.x - 1, lastHit.y)) && gridP2[lastHit.y][lastHit.x - 1] == 0) {
                battleShipControler.lineTargetsJ2.add(new Point(lastHit.x - 1, lastHit.y));
            }
            if (isValidPosition(new Point(lastHit.x + 1, lastHit.y)) && gridP2[lastHit.y][lastHit.x + 1] == 0) {
                battleShipControler.lineTargetsJ2.add(new Point(lastHit.x + 1, lastHit.y));
            }
        }
        if (!battleShipControler.lineTargetsJ2.isEmpty()) {
            battleShipControler.lineModeJ2 = true;
            battleShipControler.targetModeJ2 = false;
        }
    }
    public void pointSurLigneJ1(){
        Point lastHit = battleShipControler.hitJ1.get(battleShipControler.hitJ1.size() - 1);
        Point secondLastHit = battleShipControler.hitJ1.get(battleShipControler.hitJ1.size() - 2);
        if (lastHit.x == secondLastHit.x) {
            // Vertical line
            if (isValidPosition(new Point(lastHit.x, lastHit.y - 1)) && gridP1[lastHit.y - 1][lastHit.x] == 0) {
                battleShipControler.lineTargetsJ1.add(new Point(lastHit.x, lastHit.y - 1));
            }
            if (isValidPosition(new Point(lastHit.x, lastHit.y + 1)) && gridP1[lastHit.y + 1][lastHit.x] == 0) {
                battleShipControler.lineTargetsJ1.add(new Point(lastHit.x, lastHit.y + 1));
            }
        } else if (lastHit.y == secondLastHit.y) {
            // Horizontal line
            if (isValidPosition(new Point(lastHit.x - 1, lastHit.y)) && gridP1[lastHit.y][lastHit.x - 1] == 0) {
                battleShipControler.lineTargetsJ1.add(new Point(lastHit.x - 1, lastHit.y));
            }
            if (isValidPosition(new Point(lastHit.x + 1, lastHit.y)) && gridP1[lastHit.y][lastHit.x + 1] == 0) {
                battleShipControler.lineTargetsJ1.add(new Point(lastHit.x + 1, lastHit.y));
            }
        }

        if (!battleShipControler.lineTargetsJ1.isEmpty()) {
            battleShipControler.lineModeJ1 = true;
            battleShipControler.targetModeJ1 = false;
        }
    }

    @Override
    public ActionList decide() {
        // phase de placement : l'IA place toute sa flotte d'un coup
        boolean placement = !battleShipStageModel.flottePlacee(id_Bot);
        if (!placement) {
            // petite pause pour qu'on puisse suivre les tirs de l'IA
            try { Thread.sleep(400); } catch (InterruptedException e) { }
        }
        // synchronisé avec l'arrêt de la partie (BattleShipControler.stopGame) :
        // la partie a pu être arrêtée ou relancée depuis le menu, on ne joue alors plus
        synchronized (battleShipControler) {
            if (model.getGameStage() != battleShipStageModel || model.isEndGame()) {
                return new ActionList();
            }
            return placement ? placerTousLesBateaux() : tirerSurUneCase();
        }
    }

    private ActionList tirerSurUneCase() {
        Point target;
        if (levelBot == 1) {
            target = (id_Bot == 0) ? getNextTarget() : getNextTarget2();
        } else {
            target = (id_Bot == 0) ? getNextTargetBot2J1() : getNextTargetBot2J2();
        }
        // sécurité : si la stratégie ne trouve rien, on prend la première case pas encore visée
        if (target == null || battleShipStageModel.dejaTire(id_Bot, target.y, target.x)) {
            target = premiereCaseLibre();
        }
        ActionList actions = (target == null) ? null : battleShipControler.tirer(id_Bot, target.y, target.x);
        if (actions == null) {
            // rien à tirer : on passe la main
            battleShipControler.tourJoue();
            actions = new ActionList();
            actions.setDoEndOfTurn(true);
            return actions;
        }

        // target.x = colonne, target.y = ligne
        int x = target.x;
        int y = target.y;
        Ship[] shipsAdverses = battleShipStageModel.getShipsAdverse(id_Bot);
        boolean result = battleShipStageModel.partieA(shipsAdverses, x, y) != null;
        if (id_Bot == 0) {
            if (!result) {
                markMiss(target);
            } else {
                markHit(target);
                battleShipControler.shipPartJ1.add(target);
                if (isSunk(shipsAdverses, x, y)) {
                    markSunk(target);
                } else if (battleShipControler.targetModeJ1 && battleShipControler.hitJ1.size() > 1) {
                    // Check if we can switch to line mode
                    pointSurLigneJ1();
                }
            }
        } else {
            if (!result) {
                markMiss2(target);
            } else {
                markHit2(target);
                battleShipControler.shipPartJ2.add(target);
                if (isSunk(shipsAdverses, x, y)) {
                    markSunk2(target);
                } else if (battleShipControler.targetModeJ2 && battleShipControler.hitJ2.size() > 1) {
                    // Check if we can switch to line mode
                    pointSurLigneJ2();
                }
            }
        }
        return actions;
    }

    private Point premiereCaseLibre() {
        for (int ligne = 0; ligne < GRID_SIZE; ligne++) {
            for (int colonne = 0; colonne < GRID_SIZE; colonne++) {
                if (!battleShipStageModel.dejaTire(id_Bot, ligne, colonne)) return new Point(colonne, ligne);
            }
        }
        return null;
    }

    public Point getNextTargetBot2J1() {
        if (battleShipControler.lineModeJ1) {
            return getLineModeShot();
        }else if (battleShipControler.targetModeJ1) {
            return getTargetModeShot();
        } else {
            calculerGrilleProba(gridP1);
            Point probaTarget = trouverLaMeilleurProba();
            if (probaTarget != null) {
                return probaTarget;
            } else {
                return trouverLaMeilleurProba();
            }
        }
    }
    public Point getNextTargetBot2J2() {
        if (battleShipControler.lineModeJ2) {
            return getLineModeShot2();
        }else if (battleShipControler.targetModeJ2) {
            return getTargetModeShot2();
        } else {
            calculerGrilleProba(gridP2);
            Point probaTarget = trouverLaMeilleurProba2();
            if (probaTarget != null) {
                return probaTarget;
            } else {
                return trouverLaMeilleurProba2();
            }
        }
    }
    public boolean isSunk(Ship[] ships,int x, int y){
        int coordX;
        int coordY;
        for (Ship s :ships) {
            s.verifcouler();
            if (s.getcouler()){
                for (int i = 0;i<s.getTaille();i++){
                    coordX=s.getPartCordonneX(i);
                    coordY=s.getPartCordonneY(i);
                    if (coordX==x && coordY==y)
                        return true;
                }
            }
        }
        return false;
    }


    //===================================   niveau 2 Grille proba   ==================================


    private static final int GRID_SIZE = 10;
    private int[] SHIP_SIZES; // Liste de la taille des bateaux non-couler
    public int[][] grilleProba;

    public void getSurroundingPoints(List<Point> shipParts, int[][] grid) {
        List<Point> surroundingPoints = new ArrayList<>();

        // Directions adjacentes et diagonales
        int[][] directions = { {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1} };

        // Parcourez chaque partie du bateau
        for (Point part : shipParts) {
            for (int[] dir : directions) {
                Point adjacent = new Point(part.x + dir[0], part.y + dir[1]);
                if (isValidPosition(adjacent) && !shipParts.contains(adjacent) && !surroundingPoints.contains(adjacent)) {
                    grid[adjacent.y][adjacent.x]=2;
                    surroundingPoints.add(adjacent);
                }
            }
        }
    }

    public void listeBateauxNonCoulerJ1(){
        SHIP_SIZES = new int[battleShipStageModel.ShipPlayer1.length];
        for (int i = 0; i<battleShipStageModel.ShipPlayer1.length;i++){
            battleShipStageModel.ShipPlayer1[i].verifcouler();
            if (!battleShipStageModel.ShipPlayer1[i].getcouler())
                SHIP_SIZES[i] = battleShipStageModel.ShipPlayer1[i].getTaille();
        }
    }
    public void listeBateauxNonCoulerJ2(){
        SHIP_SIZES = new int[battleShipStageModel.ShipPlayer2.length];
        for (int i = 0; i<battleShipStageModel.ShipPlayer2.length;i++){
            battleShipStageModel.ShipPlayer2[i].verifcouler();
            if (!battleShipStageModel.ShipPlayer2[i].getcouler())
                SHIP_SIZES[i] = battleShipStageModel.ShipPlayer2[i].getTaille();
        }
    }

    public void calculerGrilleProba(int[][] previousHitsGrid) {
        if (id_Bot==0)
            listeBateauxNonCoulerJ2(); // calculer la liste des tailles des bateaux non-couler du joueur 2
        else
            listeBateauxNonCoulerJ1(); // calculer la liste des tailles des bateaux non-couler du joueur 1
        System.out.println("SHIP_SIZE : ");
        for (int i = 0;i<SHIP_SIZES.length;i++)
            System.out.println(SHIP_SIZES[i]);
        // Reset the probability grid
        if (grilleProba == null) {
            grilleProba = new int[GRID_SIZE][GRID_SIZE];
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (previousHitsGrid[i][j] != 0) {
                    grilleProba[i][j] = 0;
                } else {
                    grilleProba[i][j] = 1;
                } // Reset to 0 if a missile was already launched, otherwise set to 1
            }
        }
        // Calculate probabilities for each ship size
        for (int size : SHIP_SIZES) {
            calculerProbaParBateau(size, previousHitsGrid);
        }
    }

    private void calculerProbaParBateau(int size, int[][] previousHitsGrid) {
        // horizontal
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col <= GRID_SIZE - size; col++) {
                boolean canPlace = true;
                for (int k = 0; k < size; k++) {
                    if (previousHitsGrid[row][col + k] != 0) {
                        canPlace = false;
                        break;
                    }
                }
                if (canPlace) {
                    for (int k = 0; k < size; k++) {
                        grilleProba[row][col + k]++;
                    }
                }
            }
        }

        // vertical
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row <= GRID_SIZE - size; row++) {
                boolean canPlace = true;
                for (int k = 0; k < size; k++) {
                    if (previousHitsGrid[row + k][col] != 0) {
                        canPlace = false;
                        break;
                    }
                }
                if (canPlace) {
                    for (int k = 0; k < size; k++) {
                        grilleProba[row + k][col]++;
                    }
                }
            }
        }
    }


    public Point trouverLaMeilleurProba() {
        int maxProbability = -1;
        Point bestCell = null;
        int x = 0;
        int y = 0;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grilleProba[row][col] > maxProbability && gridP1[row][col]==0) {
                    maxProbability = grilleProba[row][col];
                    x = col;
                    y = row;
                }
            }
        }
        if (maxProbability == -1) return null; // plus aucune case libre
        bestCell = new Point(x,y);
        return bestCell;
    }
    public Point trouverLaMeilleurProba2() {
        int maxProbability = -1;
        Point bestCell = null;
        int x = 0;
        int y = 0;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grilleProba[row][col] > maxProbability && gridP2[row][col]==0) {
                    maxProbability = grilleProba[row][col];
                    x = col;
                    y = row;
                }
            }
        }
        if (maxProbability == -1) return null; // plus aucune case libre
        bestCell = new Point(x,y);
        return bestCell;
    }

    //=========================== Méthode de placement =============================

    // l'IA place tous ses bateaux au hasard, en respectant les règles de placement
    private ActionList placerTousLesBateaux() {
        Ship[] ships = battleShipStageModel.getShips(id_Bot);
        // placement séquentiel : dans de rares cas les derniers bateaux n'ont plus de place,
        // on recommence alors tout le placement
        while (!placerFlotte(ships)) { }

        ActionList actions = new ActionList();
        for (Ship ship : ships) {
            battleShipStageModel.shipPlaced(id_Bot);
            actions.addAll(battleShipControler.actionsPlacerBateau(id_Bot, ship));
        }
        actions.setDoEndOfTurn(true);
        return actions;
    }

    private boolean placerFlotte(Ship[] ships) {
        for (Ship ship : ships) {
            for (shipPart part : ship.getshippart()) part.setCordoner(-10, -10);
        }
        for (Ship ship : ships) {
            if (!placeShip(ship, ships)) return false;
        }
        return true;
    }

    private boolean placeShip(Ship bateau, Ship[] ships) {
        for (int essai = 0; essai < 1000; essai++) {
            int x = random.nextInt(GRID_SIZE); // colonne
            int y = random.nextInt(GRID_SIZE); // ligne
            char sens = random.nextBoolean() ? 'V' : 'H';
            if (battleShipStageModel.Verifpeutetreposer(ships, x, y, bateau.getTaille(), sens)) {
                bateau.setCordonnerShip(y, x, sens);
                return true;
            }
        }
        return false;
    }
}
