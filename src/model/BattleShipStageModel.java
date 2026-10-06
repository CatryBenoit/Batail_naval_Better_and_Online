package model;

import boardifier.model.*;

/*
 * Convention des coordonnées (partout dans le jeu) :
 *   - X = numéro de colonne, Y = numéro de ligne (de 0 à 9)
 *   - dans les conteneurs boardifier, un élément est placé en (ligne, colonne) = (Y, X)
 *
 * Joueur 1 (idJoueur 0) place ses bateaux sur boardplayer1 et tire sur boardplayer2.
 * Joueur 2 (idJoueur 1) place ses bateaux sur boardplayer2 et tire sur boardplayer1.
 */
public class BattleShipStageModel extends GameStageModel {

    public static final int TAILLE_GRILLE = 10;

    private int player1toplay;
    private int Player2toplay;

    private BattleBoard Boardplayer1;
    private BattleBoard Boardplayer2;
    public Ship[] ShipPlayer1;
    public Ship[] ShipPlayer2;
    private TextElement player1Name;
    private TextElement player2Name;
    private Missille[] MissileJoueur1;
    private Missille[] MissileJoueur2;
    private TextElement InfoPartie;
    private StockMissile stockMissileJ1;
    private StockMissile stockMissileJ2;

    // nombre de bateaux déjà placés par chaque joueur
    private int[] nbShipPlaced = new int[2];
    // cases sur lesquelles chaque joueur a déjà tiré : tirs[idJoueur][ligne][colonne]
    private boolean[][][] tirs = new boolean[2][TAILLE_GRILLE][TAILLE_GRILLE];
    // nombre de tirs réussis de chaque joueur (en ligne, on ne connaît pas la flotte adverse :
    // on ne peut donc pas recompter les parties touchées, d'où ce compteur)
    private int[] touches = new int[2];


    public BattleShipStageModel(String name, Model model) {
        super(name, model);
        player1toplay = 50;
        Player2toplay = 50;
        setupCallbacks();

    }

    public void setStockMissileJ1(StockMissile stockMissileJ1) {this.stockMissileJ1 = stockMissileJ1;
        addContainer(stockMissileJ1);

    }

    public void setStockMissileJ2(StockMissile stockMissileJ2) {this.stockMissileJ2 = stockMissileJ2;
        addContainer(stockMissileJ2);

    }

    public StockMissile getStockMissileJ1() {return stockMissileJ1;}
    public StockMissile getStockMissileJ2() {return stockMissileJ2;}

    // nombre d'images différentes de bateau pour chaque taille (voir outils/generer_navires.sh)
    private static final int[] VARIANTES_IMAGES = {0, 2, 3, 2, 1, 1};

    // nom de l'image du bateau n°j : les bateaux de même taille utilisent tour à tour les variantes a, b, c...
    public static String apparence(Ship[] ships, int j) {
        int taille = ships[j].getTaille();
        if (taille >= VARIANTES_IMAGES.length || VARIANTES_IMAGES[taille] == 0) return null;
        int occurrence = 0;
        for (int k = 0; k < j; k++) {
            if (ships[k].getTaille() == taille) occurrence++;
        }
        return "navire" + taille + (char) ('a' + occurrence % VARIANTES_IMAGES[taille]);
    }

    public void setshippartplayer1(shipPart[] shipParts){
        int num =0;
        for(int j = 0; j < ShipPlayer1.length; j++) {
            for (int i = 0; i < ShipPlayer1[j].shipParts.length; i++) {
                ShipPlayer1[j].shipParts[i] = shipParts[num];
                shipParts[num].setApparence(apparence(ShipPlayer1, j), i);
                addElement(ShipPlayer1[j].shipParts[i]);
                ShipPlayer1[j].addElement(ShipPlayer1[j].shipParts[i], i, 0);
                num++;
            }

        }
    }
    public void setshippartplayer2(shipPart[] shipParts){
        int num =0;
        for(int j = 0; j < ShipPlayer2.length; j++) {
            for (int i = 0; i < ShipPlayer2[j].shipParts.length; i++) {
                ShipPlayer2[j].shipParts[i] = shipParts[num];
                shipParts[num].setApparence(apparence(ShipPlayer2, j), i);
                addElement(ShipPlayer2[j].shipParts[i]);
                ShipPlayer2[j].addElement(ShipPlayer2[j].shipParts[i], i, 0);
                num++;
            }

        }
    }


    public void setInfoPartie(TextElement infopartie){this.InfoPartie = infopartie; addElement(infopartie);}
    public TextElement getInfoPartie(){return this.InfoPartie;}


    // set et get pour playerXtoPlay utilisé pour nb de coup réstant
    public void setPlayer1ToPlay(int player1ToPlay) {this.player1toplay = player1ToPlay;}
    public int getPlayer1ToPlay() {return player1toplay;}
    public void setPlayer2ToPlay(int player2ToPlay) {this.Player2toplay = player2ToPlay;}
    public int getPlayer2ToPlay() {return Player2toplay;}

    //set et get de board pour chaque joueur
    public void setBoardPlayer1(BattleBoard boardPlayer1) {this.Boardplayer1 = boardPlayer1; addContainer(boardPlayer1);}
    public BattleBoard getBoardPlayer1() {return Boardplayer1;}
    public void setBoardPlayer2(BattleBoard boardPlayer2) {this.Boardplayer2 = boardPlayer2; addContainer(boardPlayer2);}
    public BattleBoard getBoardPlayer2() {return Boardplayer2;}


    //set et get du tableau des bateau de chaque joueur
    public void setShipsPlayer1(Ship[] shipsPlayer1) {this.ShipPlayer1 = shipsPlayer1;
        for(int i = 0; i < shipsPlayer1.length ; i++){addContainer(shipsPlayer1[i]);}}
    public Ship[] getShipsPlayer1() {return ShipPlayer1;}
    public void setShipsPlayer2(Ship[] shipsPlayer2) {this.ShipPlayer2 = shipsPlayer2;
        for(int i = 0; i < shipsPlayer2.length ; i++){addContainer(shipsPlayer2[i]);}}
    public Ship[] getShipsPlayer2() {return ShipPlayer2;}


    //set et get du nom de chaque joueur
    public void setPlayer1Name(TextElement player1Name) {this.player1Name = player1Name; addElement(player1Name);}
    public TextElement getPlayer1Name() {return player1Name;}
    public void setPlayer2Name(TextElement player2Name) {this.player2Name = player2Name; addElement(player2Name);}
    public TextElement getPlayer2Name() {return player2Name;}

    //Set et get des cellule pour les board
    public Missille[] getMissileJoueur1() {return MissileJoueur1;}
    public void setMissileJoueur1(Missille[] m){
        this.MissileJoueur1 = m;
        for(int i = 0; i < MissileJoueur1.length ; i++){addElement(MissileJoueur1[i]);}}
    public Missille[] getMissileJoueur2() {return MissileJoueur2;}
    public void setMissileJoueur2(Missille[] m){
        this.MissileJoueur2 = m;
        for(int i = 0; i < MissileJoueur2.length ; i++){addElement(MissileJoueur2[i]);}}


    // accès par numéro de joueur (0 = joueur 1, 1 = joueur 2)
    public Ship[] getShips(int idJoueur) {return idJoueur == 0 ? ShipPlayer1 : ShipPlayer2;}
    public Ship[] getShipsAdverse(int idJoueur) {return getShips(1 - idJoueur);}
    public BattleBoard getBoard(int idJoueur) {return idJoueur == 0 ? Boardplayer1 : Boardplayer2;}
    public BattleBoard getBoardAdverse(int idJoueur) {return getBoard(1 - idJoueur);}
    public StockMissile getStockMissile(int idJoueur) {return idJoueur == 0 ? stockMissileJ1 : stockMissileJ2;}
    public int getMissilesRestants(int idJoueur) {return idJoueur == 0 ? player1toplay : Player2toplay;}


    // placement des bateaux
    public int getNbShipPlaced(int idJoueur) {return nbShipPlaced[idJoueur];}
    public void shipPlaced(int idJoueur) {nbShipPlaced[idJoueur]++;}
    public boolean flottePlacee(int idJoueur) {return nbShipPlaced[idJoueur] >= getShips(idJoueur).length;}
    public boolean phaseDeTir() {return flottePlacee(0) && flottePlacee(1);}
    // en ligne : l'adversaire a placé sa flotte (sans nous dire où)
    public void marquerFlottePlacee(int idJoueur) {nbShipPlaced[idJoueur] = getShips(idJoueur).length;}


    // tirs déjà effectués (ligne/colonne sur la grille adverse)
    public boolean dejaTire(int idJoueur, int ligne, int colonne) {return tirs[idJoueur][ligne][colonne];}
    public void marquerTir(int idJoueur, int ligne, int colonne) {tirs[idJoueur][ligne][colonne] = true;}

    public void ajouterTouche(int idJoueur) {touches[idJoueur]++;}
    public int getTouches(int idJoueur) {return touches[idJoueur];}


    //verif que le bateau tient dans la grille et qu'il ne touche aucun autre bateau : ni sur les côtés ni dans les coins
    // xNewship = colonne de départ, yNewShip = ligne de départ
    public boolean Verifpeutetreposer(Ship[] ships, int xNewship, int yNewShip, int tailleNewShip, char sens){
        if (sens != 'H' && sens != 'V') return false;
        int xFin = (sens == 'H') ? xNewship + tailleNewShip - 1 : xNewship;
        int yFin = (sens == 'V') ? yNewShip + tailleNewShip - 1 : yNewShip;
        if (xNewship < 0 || yNewShip < 0 || xFin >= TAILLE_GRILLE || yFin >= TAILLE_GRILLE) return false;

        // zone interdite = le bateau + une case tout autour
        for (Ship ship : ships) {
            for (int l = 0; l < ship.getTaille(); l++) {
                int x = ship.getPartCordonneX(l);
                int y = ship.getPartCordonneY(l);
                if (x >= xNewship - 1 && x <= xFin + 1 && y >= yNewShip - 1 && y <= yFin + 1) {
                    return false;
                }
            }
        }
        return true;
    }


    // renvoie la partie de bateau située en (x = colonne, y = ligne), ou null
    public shipPart partieA(Ship[] ships, int x, int y){
        Ship ship = bateauA(ships, x, y);
        if (ship == null) return null;
        for (shipPart part : ship.shipParts) {
            if (part.getcordonneX() == x && part.getcordonneY() == y) return part;
        }
        return null;
    }

    // renvoie le bateau qui a une partie en (x = colonne, y = ligne), ou null
    public Ship bateauA(Ship[] ships, int x, int y){
        for (Ship ship : ships) {
            for (shipPart part : ship.shipParts) {
                if (part.getcordonneX() == x && part.getcordonneY() == y) return ship;
            }
        }
        return null;
    }

    // premier bateau pas encore coulé de la taille donnée, ou null
    public Ship bateauNonCouleDeTaille(Ship[] ships, int taille){
        for (Ship ship : ships) {
            if (!ship.getcouler() && ship.getTaille() == taille) return ship;
        }
        return null;
    }

    // x = colonne, y = ligne
    public boolean toucheroupas(Ship[] ships, int x , int y){
        for(int i = 0; i < ships.length; i++){
            for(int j= 0; j < ships[i].shipParts.length; j++ ){
                    if(ships[i].shipParts[j].getcordonneX() == x && ships[i].shipParts[j].getcordonneY() == y){
                        ships[i].shipParts[j].setToucher(true);
                        System.out.println("TOUCHER !!!");
                        ships[i].verifcouler();
                        return true;
                    }

            }
        }
        return false;
    }





    public void setupCallbacks(){
        onPutInContainer( (element, gridDest, rowDest, colDest) -> {
                // seuls les missiles qui arrivent sur une grille comptent comme un tir
                if ((gridDest != Boardplayer1 && gridDest != Boardplayer2) || !(element instanceof Missille)) return;

                Missille m = (Missille) element;
                m.setTire(true);
                if (m.getIdjoueur() == 0) {
                    player1toplay--;
                }
                else {
                    Player2toplay--;
                }
                System.out.println("missiles restants : J1 = " + player1toplay + ", J2 = " + Player2toplay);
                if ((player1toplay <= 0 && Player2toplay <= 0 ) || toutShipCouler(ShipPlayer1) || toutShipCouler(ShipPlayer2)) {
                    computePartyResult();
                }
            });


    }


    public boolean toutShipCouler(Ship[] ships){

        for (Ship ship : ships) {
            if (ship.getcouler() ==false) {
                return false;
            }
        }
        return true;
    }


    //méthode pour regarder qui gagne dans la partie :
    // le joueur 1 marque un point à chaque tir qui touche un bateau du joueur 2, et inversement.
    // (si une flotte est entièrement coulée, son adversaire a forcément le score maximum)
    private void computePartyResult(){
        int scoreJ1 = touches[0];
        int scoreJ2 = touches[1];

        if (scoreJ1 > scoreJ2) {
            System.out.println("le joueur 1 a gagné avec : " + scoreJ1 + " touches.");
            model.setIdWinner(0); // index du joueur dans la liste des joueurs du modèle
        } else if (scoreJ2 > scoreJ1) {
            System.out.println("le joueur 2 a gagné avec : " + scoreJ2 + " touches.");
            model.setIdWinner(1);
        } else {
            System.out.println("Match nul");
            model.setIdWinner(-1);
        }
        // stopGame (et pas stopStage) : c'est ce qui déclenche l'affichage du gagnant par le contrôleur
        model.stopGame();
    }


    public int nbdepart(Ship[] ships){
        int nbtotal = 0;
        for (int i = 0 ; i <ships.length; i++){
            nbtotal+= ships[i].getTaille();
        }
        return nbtotal;
    }


    //truc obligatoir c'est pas a quoi il sert mais il est la
    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return new BattleShipStageFactory(this);
    }


    public void setinvisiblebateau(Ship[] ships){
        for (int j = 0; j < ships.length; j++) {
            for (int i = 0; i < ships[j].getTaille(); i++) {
                // un bateau déjà coulé reste visible
                if (!ships[j].getcouler()) {
                    ships[j].shipParts[i].setVisible(false);
                }
            }
        }
    }
    public void setvisiblebateau(Ship[] ships){
        for (int j = 0; j < ships.length; j++) {
            for (int i = 0; i < ships[j].getTaille(); i++) {
                ships[j].shipParts[i].setVisible(true);
            }
        }
    }

    public Ship getship(int numero, Ship[]ships){
        if (numero < 0 || numero >= ships.length) {return null;}
        return ships[numero];
    }
}
