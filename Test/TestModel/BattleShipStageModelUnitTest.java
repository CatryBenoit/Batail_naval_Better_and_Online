package TestModel;


import boardifier.model.Model;
import boardifier.model.TextElement;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class BattleShipStageModelUnitTest {

    private BattleShipStageModel battleShipStageModel;
    private Model model;
    private BattleBoard boardPlayer1;
    private BattleBoard boardPlayer2;
    private Ship[] shipsPlayer1;
    private Ship[] shipsPlayer2;
    private TextElement player1Name;
    private TextElement player2Name;
    private StockMissile stockMissileJ1;
    private StockMissile stockMissileJ2;
    private Missille missille;

    @BeforeEach
    public void setUp() {
        model = mock(Model.class);
        battleShipStageModel = new BattleShipStageModel("TestStage", model);
        boardPlayer1 = mock(BattleBoard.class);
        boardPlayer2 = mock(BattleBoard.class);
        missille = mock(Missille.class);
        shipsPlayer1 = new Ship[5];
        shipsPlayer2 = new Ship[5];
        for (int i = 0; i < 5; i++) {
            shipsPlayer1[i] = mock(Ship.class);
            shipsPlayer2[i] = mock(Ship.class);
        }
        battleShipStageModel.setBoardPlayer1(boardPlayer1);
        battleShipStageModel.setBoardPlayer2(boardPlayer2);
        battleShipStageModel.setShipsPlayer1(shipsPlayer1);
        battleShipStageModel.setShipsPlayer2(shipsPlayer2);
    }


    @Test
    public void testSetStockMissileJ1() {
        battleShipStageModel.setStockMissileJ1(stockMissileJ1);
        assertEquals(stockMissileJ1, battleShipStageModel.getStockMissileJ1());
    }

    @Test
    public void testSetStockMissileJ2() {
        battleShipStageModel.setStockMissileJ2(stockMissileJ2);
        assertEquals(stockMissileJ2, battleShipStageModel.getStockMissileJ2());
    }

    @Test
    public void testSetShipsPlayer1() {
        battleShipStageModel.setShipsPlayer1(shipsPlayer1);
        assertArrayEquals(shipsPlayer1, battleShipStageModel.getShipsPlayer1());
    }

    @Test
    public void testSetShipsPlayer2() {
        battleShipStageModel.setShipsPlayer2(shipsPlayer2);
        assertArrayEquals(shipsPlayer2, battleShipStageModel.getShipsPlayer2());
    }

    @Test
    public void testSetPlayer1Name() {
        battleShipStageModel.setPlayer1Name(player1Name);
        assertEquals(player1Name, battleShipStageModel.getPlayer1Name());
    }

    @Test
    public void testSetPlayer2Name() {
        battleShipStageModel.setPlayer2Name(player2Name);
        assertEquals(player2Name, battleShipStageModel.getPlayer2Name());
    }


    @Test
    public void testVerifpeutetreposer() {

        when(shipsPlayer1[0].getPartCordonneX(anyInt())).thenReturn(1);
        when(shipsPlayer1[0].getPartCordonneY(anyInt())).thenReturn(1);
        when(shipsPlayer1[0].getTaille()).thenReturn(3);

        boolean result = battleShipStageModel.Verifpeutetreposer(shipsPlayer1, 4, 4, 3, 'H');
        assertTrue(result);

        result = battleShipStageModel.Verifpeutetreposer(shipsPlayer1, 1, 1, 3, 'H');
        assertFalse(result);
    }

    // bateau réel de taille 3 posé horizontalement à partir de la colonne x, ligne y
    private Ship vraiBateau(int x, int y) {
        Ship ship = new Ship(0, 0, 3, battleShipStageModel);
        ship.setShipParts(battleShipStageModel);
        for (int i = 0; i < 3; i++) {
            ship.getshippart()[i] = new shipPart(i + 1, 1, 0, battleShipStageModel);
        }
        ship.setCordonnerShip(y, x, 'H');
        return ship;
    }

    @Test
    public void testToucherOuPas() {
        Ship[] ships = { vraiBateau(2, 3) };
        // x = colonne, y = ligne
        assertTrue(battleShipStageModel.toucheroupas(ships, 3, 3));
        assertTrue(ships[0].getshippart()[1].esttoucher());
        assertFalse(battleShipStageModel.toucheroupas(ships, 3, 4));
        assertFalse(ships[0].getcouler());
        battleShipStageModel.toucheroupas(ships, 2, 3);
        battleShipStageModel.toucheroupas(ships, 4, 3);
        assertTrue(ships[0].getcouler());
    }

    @Test
    public void testVerifpeutetreposer_ContactEtBords() {
        Ship[] ships = { vraiBateau(2, 3) }; // occupe (2,3) (3,3) (4,3)
        // collé en dessous, dans le coin, ou par-dessus : interdit
        assertFalse(battleShipStageModel.Verifpeutetreposer(ships, 2, 4, 2, 'H'));
        assertFalse(battleShipStageModel.Verifpeutetreposer(ships, 5, 4, 2, 'V'));
        assertFalse(battleShipStageModel.Verifpeutetreposer(ships, 0, 3, 3, 'H'));
        // à une case d'écart : autorisé
        assertTrue(battleShipStageModel.Verifpeutetreposer(ships, 2, 5, 3, 'H'));
        // hors de la grille : interdit
        assertFalse(battleShipStageModel.Verifpeutetreposer(ships, 8, 0, 3, 'H'));
        assertFalse(battleShipStageModel.Verifpeutetreposer(ships, 0, 8, 3, 'V'));
        assertFalse(battleShipStageModel.Verifpeutetreposer(ships, -1, 0, 3, 'H'));
    }

    @Test
    public void testToutShipCouler() {
        when(shipsPlayer1[0].getcouler()).thenReturn(true);
        when(shipsPlayer1[1].getcouler()).thenReturn(true);
        when(shipsPlayer1[2].getcouler()).thenReturn(true);
        when(shipsPlayer1[3].getcouler()).thenReturn(true);
        when(shipsPlayer1[4].getcouler()).thenReturn(true);
        boolean result = battleShipStageModel.toutShipCouler(shipsPlayer1);
        assertTrue(result);

        when(shipsPlayer1[4].getcouler()).thenReturn(false);
        result = battleShipStageModel.toutShipCouler(shipsPlayer1);
        assertFalse(result);
    }

    // putInContainer() déclenche le callback onPutInContainer défini dans setupCallbacks()
    @Test
    public void testSetupCallbacks_Player1Missille() {
        when(missille.getIdjoueur()).thenReturn(0);
        battleShipStageModel.putInContainer(missille, boardPlayer2, 0, 0);
        assertEquals(49, battleShipStageModel.getPlayer1ToPlay());
        assertEquals(50, battleShipStageModel.getPlayer2ToPlay());
    }

    @Test
    public void testSetupCallbacks_Player2Missille() {
        when(missille.getIdjoueur()).thenReturn(1);
        battleShipStageModel.putInContainer(missille, boardPlayer1, 0, 0);
        assertEquals(49, battleShipStageModel.getPlayer2ToPlay());
        assertEquals(50, battleShipStageModel.getPlayer1ToPlay());
    }

    @Test
    public void testSetupCallbacks_IgnoreHorsGrille() {
        when(missille.getIdjoueur()).thenReturn(0);
        // un missile rangé ailleurs que sur une grille ne compte pas comme un tir
        battleShipStageModel.putInContainer(missille, shipsPlayer1[0], 0, 0);
        assertEquals(50, battleShipStageModel.getPlayer1ToPlay());
    }

    @Test
    public void testSetupCallbacks_FinDePartieSansMissiles() {
        when(missille.getIdjoueur()).thenReturn(0);
        battleShipStageModel.setPlayer1ToPlay(1);
        battleShipStageModel.setPlayer2ToPlay(0);
        battleShipStageModel.putInContainer(missille, boardPlayer2, 0, 0);
        assertEquals(0, battleShipStageModel.getPlayer1ToPlay());
        // aucune touche de part et d'autre : match nul, et fin de partie (stopGame, pas stopStage)
        verify(model).setIdWinner(-1);
        verify(model).stopGame();
    }

    @Test
    public void testSetupCallbacks_PasFinDePartie() {
        when(missille.getIdjoueur()).thenReturn(0);
        battleShipStageModel.setPlayer1ToPlay(1);
        battleShipStageModel.setPlayer2ToPlay(1);
        battleShipStageModel.putInContainer(missille, boardPlayer2, 0, 0);
        assertEquals(0, battleShipStageModel.getPlayer1ToPlay());
        verify(model, never()).stopGame();
    }

    @Test
    public void testGagnant_Joueur1QuiACouleLaFlotteAdverse() {
        // toute la flotte du joueur 2 est coulée : le joueur 1 (index 0) gagne
        for (Ship ship : shipsPlayer2) {
            when(ship.getcouler()).thenReturn(true);
        }
        for (int i = 0; i < 3; i++) battleShipStageModel.ajouterTouche(0);
        battleShipStageModel.ajouterTouche(1);
        when(missille.getIdjoueur()).thenReturn(0);
        battleShipStageModel.putInContainer(missille, boardPlayer2, 0, 0);
        verify(model).setIdWinner(0);
        verify(model).stopGame();
    }

    @Test
    public void testGagnant_Joueur2() {
        // plus de missiles : le joueur 2 a touché plus souvent
        battleShipStageModel.ajouterTouche(0);
        battleShipStageModel.ajouterTouche(1);
        battleShipStageModel.ajouterTouche(1);
        battleShipStageModel.setPlayer1ToPlay(0);
        battleShipStageModel.setPlayer2ToPlay(1);
        when(missille.getIdjoueur()).thenReturn(1);
        battleShipStageModel.putInContainer(missille, boardPlayer1, 0, 0);
        verify(model).setIdWinner(1);
    }
}
