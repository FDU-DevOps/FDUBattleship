package org.fdu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class BattleshipManagerTest {

    private BattleshipManager manager;
    private ByteArrayOutputStream out;
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        manager = new BattleshipManager();
    }

    @AfterEach
    void restoreStream() {
        System.setOut(originalOut);
    }

    // Constructor: humanDTO

    @Test
    @DisplayName("Init: humanDTO is not null after construction")
    void humanDTOIsNotNull() {
        assertNotNull(manager.getHumanDTO());
    }

    @Test
    @DisplayName("Init: human tracking grid is 10x10")
    void humanTrackingGridIsTenByTen() {
        Cell[][] grid = manager.getHumanDTO().grid();
        assertEquals(10, grid.length);
        for (Cell[] row : grid)
            assertEquals(10, row.length);
    }

    @Test
    @DisplayName("Init: human tracking grid is entirely WATER at game start")
    void humanTrackingGridIsAllWater() {
        Cell[][] grid = manager.getHumanDTO().grid();
        for (Cell[] row : grid)
            for (Cell cell : row)
                assertEquals(Cell.WATER, cell);
    }

    @Test
    @DisplayName("Init: human starts with 10 guesses")
    void humanStartsWithTenGuesses() {
        assertEquals(10, manager.getHumanDTO().guessesLeft());
    }

    @Test
    @DisplayName("Init: human game status is IN_PROGRESS at game start")
    void humanStatusIsInProgress() {
        assertEquals(GameStatus.IN_PROGRESS, manager.getHumanDTO().gameStatus());
    }

    // Constructor: computerDTO

    @Test
    @DisplayName("Init: computerDTO is not null after construction")
    void computerDTOIsNotNull() {
        assertNotNull(manager.getComputerDTO());
    }

    @Test
    @DisplayName("Init: computer ship grid is 10x10")
    void computerShipGridIsTenByTen() {
        Cell[][] grid = manager.getComputerDTO().grid();
        assertEquals(10, grid.length);
        for (Cell[] row : grid)
            assertEquals(10, row.length);
    }

    @Test
    @DisplayName("Init: computer ship grid contains exactly one SHIP cell")
    void computerGridContainsExactlyOneShip() {
        Cell[][] grid = manager.getComputerDTO().grid();
        long shipCount = 0;
        for (Cell[] row : grid)
            for (Cell cell : row)
                if (cell == Cell.SHIP) shipCount++;
        assertEquals(1, shipCount);
    }

    @Test
    @DisplayName("Init: every non-SHIP cell in the computer grid is WATER")
    void computerGridNonShipCellsAreWater() {
        Cell[][] grid = manager.getComputerDTO().grid();
        for (Cell[] row : grid)
            for (Cell cell : row)
                assertTrue(cell == Cell.SHIP || cell == Cell.WATER);
    }

    @Test
    @DisplayName("Init: computer game status is IN_PROGRESS at game start")
    void computerStatusIsInProgress() {
        assertEquals(GameStatus.IN_PROGRESS, manager.getComputerDTO().gameStatus());
    }

    // Setters

    @Test
    @DisplayName("Setter: setHumanDTO replaces the stored humanDTO reference")
    void setHumanDTOReplacesPreviousDTO() {
        Cell[][] blank = new Cell[10][10];
        for (Cell[] row : blank) java.util.Arrays.fill(row, Cell.WATER);
        PlayerDTO replacement = new PlayerDTO(blank, 3, GameStatus.IN_PROGRESS);

        manager.setHumanDTO(replacement);

        assertSame(replacement, manager.getHumanDTO());
    }

    @Test
    @DisplayName("Setter: setComputerDTO replaces the stored computerDTO reference")
    void setComputerDTOReplacesPreviousDTO() {
        Cell[][] blank = new Cell[10][10];
        for (Cell[] row : blank) java.util.Arrays.fill(row, Cell.WATER);
        PlayerDTO replacement = new PlayerDTO(blank, 0, GameStatus.IN_PROGRESS);

        manager.setComputerDTO(replacement);

        assertSame(replacement, manager.getComputerDTO());
    }

    // Debug output

    @Test
    @DisplayName("Debug: constructor prints the ship coordinate to stdout")
    void constructorPrintsShipLocation() {
        // The debug line format is "Ship is at: <col-letter><row-number>"
        String output = out.toString();
        assertTrue(output.contains("Ship is at: "),
                "Expected debug ship location line was not printed");
    }
}