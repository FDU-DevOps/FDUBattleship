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
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        manager = new BattleshipManager();
        manager.initializeGame();
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
    @DisplayName("Init: human starts with 30 guesses")
    void humanStartsWithTenGuesses() {
        assertEquals(30, manager.getHumanDTO().guessesLeft());
    }

    @Test
    @DisplayName("Init: human game status is IN_PROGRESS at game start")
    void humanStatusIsInProgress() {
        assertEquals(GameStatus.IN_PROGRESS, manager.getHumanDTO().gameStatus());
    }

    // HUMAN TRACKING GRID INIT
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

    // HUMAN HOME GRID INIT
    @Test
    @DisplayName("Init: human homeGrid is not null after initialization")
    void humanHomeGridIsNotNull() {
        assertNotNull(manager.getHumanDTO().homeGrid());
    }

    @Test
    @DisplayName("Init: human home grid is 10x10")
    void humanHomeGridIsTenByTen() {
        Cell[][] grid = manager.getHumanDTO().homeGrid();
        assertEquals(10, grid.length);
        for (Cell[] row : grid)
            assertEquals(10, row.length);
    }

    @Test
    @DisplayName("Init: human home grid contains all ships - total 17 SHIP cells")
    void humanHomeGridContainsAllShips() {
        Cell[][] grid = manager.getHumanDTO().homeGrid();
        int shipCount = 0;
        for (Cell[] row : grid)
            for (Cell cell : row)
                if (cell == Cell.SHIP) shipCount++;
        assertEquals(17, shipCount);
    }

    @Test
    @DisplayName("Init: every non-SHIP cell in the human home grid is WATER")
    void humanHomeGridNonShipCellsAreWater() {
        Cell[][] grid = manager.getHumanDTO().homeGrid();
        for (Cell[] row : grid)
            for (Cell cell : row)
                assertTrue(cell == Cell.SHIP || cell == Cell.WATER);
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
    @DisplayName("Init: computer ship grid contains all ships - total 17 SHIP cells")
    void computerGridContainsAllShips() {
        Cell[][] grid = manager.getComputerDTO().grid();
        int shipCount = 0;
        for (Cell[] row : grid)
            for (Cell cell : row)
                if (cell == Cell.SHIP) shipCount++;
        assertEquals(17, shipCount);
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
        PlayerDTO replacement = new PlayerDTO(blank, null, 3, GameStatus.IN_PROGRESS, null, null);

        manager.setHumanDTO(replacement);

        assertSame(replacement, manager.getHumanDTO());
    }

    @Test
    @DisplayName("Setter: setComputerDTO replaces the stored computerDTO reference")
    void setComputerDTOReplacesPreviousDTO() {
        Cell[][] blank = new Cell[10][10];
        for (Cell[] row : blank) java.util.Arrays.fill(row, Cell.WATER);
        PlayerDTO replacement = new PlayerDTO(blank, null, 0, GameStatus.IN_PROGRESS, null, null);

        manager.setComputerDTO(replacement);

        assertSame(replacement, manager.getComputerDTO());
    }
}