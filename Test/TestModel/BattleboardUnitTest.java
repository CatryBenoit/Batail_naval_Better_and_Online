package TestModel;
import model.BattleBoard;
import model.Missille;
import boardifier.model.GameStageModel;
import boardifier.model.GameElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BattleboardUnitTest {

    private GameStageModel gameStageModel;
    private BattleBoard battleBoard;

    @BeforeEach
    public void setUp() {
        gameStageModel = Mockito.mock(GameStageModel.class);
        battleBoard = new BattleBoard(0, 0, gameStageModel, "board");
    }

    @Test
    public void testSetValidCells_EmptyBoard() {
        battleBoard.setValidCells(1);
        // Assuming reachableCells is publicly accessible or has a getter method
        boolean[][] reachableCells = battleBoard.getReachableCells();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertTrue(reachableCells[i][j]);
            }
        }
    }


    @Test
    public void testComputeValidCells_EmptyBoard() {
        List<Point> validCells = battleBoard.computeValidCells(1);
        assertNotNull(validCells);
        assertEquals(100, validCells.size());
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertTrue(validCells.contains(new Point(j, i)));
            }
        }
    }

    @Test
    public void testComputeValidCells_Missile() {
        // Set up the board with a missile
        Missille missile = new Missille(1, 1, 0, gameStageModel);
        missile.setColor(1);
        battleBoard.addElement(missile,1,1);

        List<Point> validCells = battleBoard.computeValidCells(1);
        assertNotNull(validCells);
        assertTrue(validCells.contains(new Point(0, 0)));
        assertTrue(validCells.contains(new Point(0, 1)));
        assertTrue(validCells.contains(new Point(0, 2)));
        assertTrue(validCells.contains(new Point(1, 0)));
        assertTrue(validCells.contains(new Point(1, 2)));
        assertTrue(validCells.contains(new Point(2, 0)));
        assertTrue(validCells.contains(new Point(2, 1)));
        assertTrue(validCells.contains(new Point(2, 2)));
    }

    @Test
    public void testGetGet() {
        assertEquals(battleBoard, battleBoard.getGet());
    }
}






