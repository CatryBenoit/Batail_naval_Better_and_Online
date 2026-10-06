package TestModel;

import model.shipPart;
import boardifier.model.GameStageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import model.Ship;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


public class ShipUnitTest {

    private GameStageModel gameStageModel;
    private Ship ship;

    @BeforeEach
    public void setUp() {
        gameStageModel = mock(GameStageModel.class);
        ship = new Ship(0, 0, 3, gameStageModel);
    }

    // crée le tableau des parties ET les parties (dans le jeu, c'est BattleShipStageModel qui les fournit)
    private void creerParties() {
        ship.setShipParts(gameStageModel);
        for (int i = 0; i < ship.getTaille(); i++) {
            ship.getshippart()[i] = new shipPart(i + 1, 1, 0, gameStageModel);
        }
    }

    @Test
    public void testSetAndGetSenstrue() {
        ship.setSens(true);
        assertTrue(ship.getSens());


    }
    @Test
    public void testgetsensfalse(){
        ship.setSens(false);
        assertFalse(ship.getSens());
    }

    @Test
    public void testSetShipParts() {
        ship.setShipParts(gameStageModel);
        assertNotNull(ship.getshippart());
        assertEquals(3, ship.getshippart().length);
    }

    @Test
    public void testSetAndGetCoordinates() {
        creerParties();
        ship.setCordonnerShip(1, 1, 'H');

        assertEquals(1, ship.getPartCordonneX(0));
        assertEquals(1, ship.getPartCordonneY(0));
        assertEquals(2, ship.getPartCordonneX(1));
        assertEquals(1, ship.getPartCordonneY(1));
        assertEquals(3, ship.getPartCordonneX(2));
        assertEquals(1, ship.getPartCordonneY(2));
    }


    @Test
    public void testSetAndGetCoordinatesVertical() {
        creerParties();
        ship.setCordonnerShip(4, 1, 'V');

        assertEquals(1, ship.getPartCordonneX(0));
        assertEquals(4, ship.getPartCordonneY(0));
        assertEquals(1, ship.getPartCordonneX(1));
        assertEquals(5, ship.getPartCordonneY(1));
        assertEquals(1, ship.getPartCordonneX(2));
        assertEquals(6, ship.getPartCordonneY(2));
    }

    @Test
    public void testVerifCouler() {
        creerParties();
        ship.verifcouler();
        assertFalse(ship.getcouler());

        for (shipPart part : ship.getshippart()) {
            part.setToucher(true);
        }
        ship.verifcouler();
        assertTrue(ship.getcouler());
    }

    @Test
    public void testNbDepartCouler() {
        creerParties();

        ship.getshippart()[0].setToucher(true);
        assertEquals(1, ship.nbdepartcouler());

        ship.getshippart()[1].setToucher(true);
        assertEquals(2, ship.nbdepartcouler());

        ship.getshippart()[2].setToucher(true);
        assertEquals(3, ship.nbdepartcouler());
    }

    @Test
    public void testestcouler() {
        creerParties();
        assertFalse( ship.getcouler());
        ship.verifcouler();
        assertFalse( ship.getcouler());
        ship.getshippart()[0].setToucher(true);
        ship.verifcouler();
        assertFalse( ship.getcouler());
        ship.getshippart()[1].setToucher(true);
        ship.verifcouler();
        assertFalse( ship.getcouler());
        ship.getshippart()[2].setToucher(true);
        ship.verifcouler();
        assertTrue( ship.getcouler());
    }


    @Test
    public void testSetAndGetPlayerID() {
        ship.setidplayer(10);
        assertEquals(10, ship.getPlayerID());
    }

    @Test
    public void testGetShipID() {
        // le compteur est statique : on vérifie seulement que chaque nouveau bateau a le numéro suivant
        int id = ship.getShipID();
        Ship anotherShip = new Ship(0, 0, 2, gameStageModel);
        assertEquals(id + 1, anotherShip.getShipID());
    }

    @Test
    public void testSetCordonnerShipPlaceinvalidePossitif() {
        creerParties();
        assertFalse(ship.setCordonnerShip(8, 8, 'H'));
    }
    @Test
    public void testSetCordonnerShipPlaceinvalideNégatif() {
        creerParties();
        assertFalse(ship.setCordonnerShip(-8, -8, 'H'));
    }

    @Test
    public void testSetCordonnerShipPlaceinvalidenauvaisselettre() {
        creerParties();
        assertFalse(ship.setCordonnerShip(5, 5, 'c'));
    }
}
