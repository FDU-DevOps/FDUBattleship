package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttackProcessorTest {

    private AttackProcessor processor;
    private Cell[][] shipGrid;
    private Cell[][] trackingGrid;
    private Cell[][] homeGrid;

    @BeforeEach
    void setUp() {
        processor = new AttackProcessor();

        shipGrid = new Cell[10][10];
        trackingGrid = new Cell[10][10];
        homeGrid = new Cell[10][10];

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                shipGrid[r][c] = Cell.WATER;
                trackingGrid[r][c] = Cell.WATER;
                homeGrid[r][c]  = Cell.WATER;
            }
        }

        shipGrid[2][3] = Cell.SHIP;
        homeGrid[5][5] = Cell.SHIP;
        homeGrid[6][6] = Cell.SHIP;
    }

    private PlayerDTO human(int guesses) {
        return new PlayerDTO(trackingGrid, homeGrid, guesses, GameStatus.IN_PROGRESS, null, null);
    }

    private PlayerDTO computer() {
        return new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS, null, null);
    }

    @Test
    @DisplayName("HIT: tracking grid cell is updated to HIT")
    void hitUpdatesTrackingGrid() {
        TurnResultDTO result = processor.processAttack(2, 3, human(10), computer());
        assertEquals(Cell.HIT, result.updatedHuman().grid()[2][3]);
    }

    @Test
    @DisplayName("HIT: ship grid cell is updated to HIT")
    void hitUpdatesShipGrid() {
        TurnResultDTO result = processor.processAttack(2, 3, human(10), computer());
        assertEquals(Cell.HIT, result.updatedComputer().grid()[2][3]);
    }

    @Test
    @DisplayName("HIT: human game status is WIN when last computer ship is sunk")
    void hitSetsHumanStatusToWin() {
        TurnResultDTO result = processor.processAttack(2, 3, human(10), computer());
        assertEquals(GameStatus.WIN, result.updatedHuman().gameStatus());
    }

    @Test
    @DisplayName("HIT: guesses remaining are unchanged after hitting a ship")
    void hitDoesNotDecrementGuesses() {
        TurnResultDTO result = processor.processAttack(2, 3, human(7), computer());
        assertEquals(7, result.updatedHuman().guessesLeft());
    }

    @Test
    @DisplayName("HIT: status stays IN_PROGRESS when computer ships still remain")
    void hitKeepsInProgressWhenShipsRemain() {
        shipGrid[7][7] = Cell.SHIP;
        TurnResultDTO result = processor.processAttack(2, 3, human(10), computer());
        assertEquals(GameStatus.IN_PROGRESS, result.updatedHuman().gameStatus());
    }

    @Test
    @DisplayName("MISS: tracking grid cell is updated to MISS")
    void missUpdatesTrackingGrid() {
        TurnResultDTO result = processor.processAttack(0, 0, human(5), computer());
        assertEquals(Cell.MISS, result.updatedHuman().grid()[0][0]);
    }

    @Test
    @DisplayName("MISS: ship grid cell is updated to MISS")
    void missUpdatesShipGrid() {
        TurnResultDTO result = processor.processAttack(0, 0, human(5), computer());
        assertEquals(Cell.MISS, result.updatedComputer().grid()[0][0]);
    }

    @Test
    @DisplayName("MISS: guesses remaining are decremented by one")
    void missDecrementsGuesses() {
        TurnResultDTO result = processor.processAttack(0, 0, human(5), computer());
        assertEquals(4, result.updatedHuman().guessesLeft());
    }

    @Test
    @DisplayName("MISS: status stays IN_PROGRESS when human ships still remain")
    void missKeepsInProgressWhenShipsRemain() {
        TurnResultDTO result = processor.processAttack(0, 0, human(3), computer());
        assertEquals(GameStatus.IN_PROGRESS, result.updatedHuman().gameStatus());
    }

    @Test
    @DisplayName("MISS: computer fires and coordinates are recorded in TurnResult")
    void missRecordsComputerMoveCoordinates() {
        TurnResultDTO result = processor.processAttack(0, 0, human(5), computer());
        assertTrue(result.computerRow() >= 0);
        assertTrue(result.computerCol() >= 0);
    }

    @Test
    @DisplayName("LOSS: status is LOSS when computer sinks the last human ship")
    void computerSinksLastShipSetsLoss() {
        for (int r = 0; r < 10; r++)
            for (int c = 0; c < 10; c++)
                if (r != 5 || c != 5) homeGrid[r][c] = Cell.MISS;

        TurnResultDTO result = processor.processAttack(0, 0, human(5), computer());
        assertEquals(GameStatus.LOSS, result.updatedHuman().gameStatus());
    }

    @Test
    @DisplayName("LOSS: computer does not fire if player wins on their turn")
    void computerDoesNotFireAfterPlayerWins() {
        TurnResultDTO result = processor.processAttack(2, 3, human(10), computer());
        assertEquals(-1, result.computerRow());
        assertEquals(-1, result.computerCol());
    }

    @Test
    @DisplayName("LOSS: status is LOSS when player runs out of guesses")
    void runningOutOfGuessesSetsLoss() {
        TurnResultDTO result = processor.processAttack(0, 0, human(1), computer());
        assertEquals(GameStatus.LOSS, result.updatedHuman().gameStatus());
        assertEquals(0, result.updatedHuman().guessesLeft());
        assertEquals(GameStatus.WIN, result.updatedComputer().gameStatus());
    }

    // -------------------------------------------------------------------------
    // Ship tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Ship: isSunk returns false when no cells are hit")
    void shipIsNotSunkWhenIntact() {
        Ship ship = new Ship(List.of(new int[]{2, 3}));
        assertFalse(ship.isSunk(shipGrid));
    }

    @Test
    @DisplayName("Ship: isSunk returns true when all cells are hit")
    void shipIsSunkWhenAllCellsHit() {
        shipGrid[2][3] = Cell.HIT;
        Ship ship = new Ship(List.of(new int[]{2, 3}));
        assertTrue(ship.isSunk(shipGrid));
    }

    @Test
    @DisplayName("Ship: isSunk returns false when only some cells are hit")
    void shipIsNotSunkWhenPartiallySunk() {
        shipGrid[0][0] = Cell.SHIP;
        shipGrid[0][1] = Cell.SHIP;
        shipGrid[0][0] = Cell.HIT;
        Ship ship = new Ship(List.of(new int[]{0, 0}, new int[]{0, 1}));
        assertFalse(ship.isSunk(shipGrid));
    }

    @Test
    @DisplayName("Ship: size returns correct number of cells")
    void shipSizeMatchesCellCount() {
        Ship ship = new Ship(List.of(new int[]{0, 0}, new int[]{0, 1}, new int[]{0, 2}));
        assertEquals(3, ship.size());
    }
}