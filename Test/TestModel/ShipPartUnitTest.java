package TestModel;

import boardifier.model.GameStageModel;
import boardifier.model.Model;
import boardifier.model.StageElementsFactory;
import model.shipPart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShipPartUnitTest {
    private model.shipPart shipPart;
    private GameStageModel gameStageModel;

    @BeforeEach
    public void setUp() {
        Model m = new Model();
        gameStageModel = new GameStageModel("test", m) {
            @Override
            public StageElementsFactory getDefaultElementFactory() {
                return null;
            }
        }; // Assuming you have a default constructor
        shipPart = new shipPart(1, 2, 0, gameStageModel);
    }

    @Test
    public void testSetAndGetNumber() {
        shipPart.setNumber(5);
        Assertions.assertEquals(5, shipPart.getNumber());
    }

    @Test
    public void testSetAndGetColor() {
        shipPart.setColors(3);
        Assertions.assertEquals(3, shipPart.getColor());
    }

    @Test
    public void testSetAndGetCoordinates() {
        shipPart.setCordoner(10, 15);
        Assertions.assertEquals(10, shipPart.getcordonneX());
        Assertions.assertEquals(15, shipPart.getcordonneY());
    }

    @Test
    public void testSetetGetX() {
        shipPart.setcordonneX(20);
        Assertions.assertEquals(20, shipPart.getcordonneX());
    }

    @Test
    public void testSetetGetY() {
        shipPart.setcordonneY(25);
        Assertions.assertEquals(25, shipPart.getcordonneY());
    }

    @Test
    public void testSetetestTouchertrue() {
        shipPart.setToucher(true);
        Assertions.assertTrue(shipPart.esttoucher());
    }

    @Test
    public void testSetetestToucherfalse() {
        shipPart.setToucher(false);
        Assertions.assertFalse(shipPart.esttoucher());
    }



}
