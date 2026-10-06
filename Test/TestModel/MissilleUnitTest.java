package TestModel;
import boardifier.model.GameStageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import model.Missille;

import static org.junit.jupiter.api.Assertions.*;
public class MissilleUnitTest {
    private GameStageModel gameStageModel;
    private Missille missile;

    @BeforeEach
    public void setUp() {
        gameStageModel = Mockito.mock(GameStageModel.class);
        missile = new Missille(1, 2, 0, gameStageModel);
    }

    @Test
    public void testMissileInitialization() {
        assertNotNull(missile);
        assertEquals(1, missile.getNumber());
        assertEquals(2, missile.getColor());
    }

    @Test
    public void testSetetGetColor() {
        missile.setColor(5);
        assertEquals(5, missile.getColor());
    }

    @Test
    public void testSetetGetNumber() {
        missile.setNumber(5);
        assertEquals(5, missile.getNumber());
    }
    @Test
    public void testSetetGetidplayer() {
        missile.setIdjoueur(5);
        assertEquals(5, missile.getIdjoueur());
    }


}
