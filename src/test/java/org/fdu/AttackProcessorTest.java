package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttackProcessorTest {

    private AttackProcessor processor;
    private Cell[][] shipGrid;
    private Cell[][] trackingGrid;
    private Cell[][] homeGrid;

    @BeforeEach
    void setUp() {
        processor = new AttackProcessor();

        // 10x10 grids, all WATER by default
        shipGrid = new Cell[10][10];
        trackingGrid = new Cell[10][10];
        homeGrid = new Cell[10][10];

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                shipGrid[r][c] = Cell.WATER;
                trackingGrid[r][c] = Cell.WATER;
                homeGrid[r][c] = Cell.WATER;
            }
        }

        // One computer ship at (2,3)
        shipGrid[2][3] = Cell.SHIP;

        // One human ship at (5,5) needed so computer win check
        // does not trigger LOSS on the very first move
        homeGrid[5][5] = Cell.SHIP;
    }

    @Test
    @DisplayName("HIT: returns array of length 2")
    void hitReturnsArrayOfTwo() {
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(2, result.length);
    }

    @Test
    @DisplayName("HIT: tracking grid cell is updated to HIT")
    void hitUpdatesTrackingGrid() {
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(Cell.HIT, result[0].grid()[2][3]);
    }

    @Test
    @DisplayName("HIT: ship grid cell is updated to HIT")
    void hitUpdatesShipGrid() {
        PlayerDTO human    = new PlayerDTO(trackingGrid, homeGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(Cell.HIT, result[1].grid()[2][3]);
    }

    @Test
    @DisplayName("HIT: human game status is WIN when last computer ship is sunk")
    void hitSetsHumanStatusToWin() {
        // shipGrid already has exactly one SHIP at (2,3), so hitting it sinks the last ship
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(GameStatus.WIN, result[0].gameStatus());
    }

    @Test
    @DisplayName("HIT: guesses remaining are unchanged after hitting a ship")
    void hitDoesNotDecrementGuesses() {
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 7, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(7, result[0].guessesLeft());
    }

    @Test
    @DisplayName("HIT: status stays IN_PROGRESS when computer ships still remain")
    void hitKeepsInProgressWhenShipsRemain() {
        // add a second computer ship so the first hit does not end the game
        shipGrid[7][7] = Cell.SHIP;

        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(GameStatus.IN_PROGRESS, result[0].gameStatus());
    }

    @Test
    @DisplayName("MISS: tracking grid cell is updated to MISS")
    void missUpdatesTrackingGrid() {
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 5, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(Cell.MISS, result[0].grid()[0][0]);
    }

    @Test
    @DisplayName("MISS: ship grid cell is updated to MISS")
    void missUpdatesShipGrid() {
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 5, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(Cell.MISS, result[1].grid()[0][0]);
    }

    @Test
    @DisplayName("MISS: guesses remaining are decremented by one")
    void missDecrementsGuesses() {
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 5, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(4, result[0].guessesLeft());
    }

    @Test
    @DisplayName("MISS: status stays IN_PROGRESS when human ships still remain")
    void missKeepsInProgressWhenShipsRemain() {
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 3, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(GameStatus.IN_PROGRESS, result[0].gameStatus());
    }

    @Test
    @DisplayName("LOSS: status is LOSS when computer sinks the last human ship")
    void computerSinksLastShipSetsLoss() {
        // homeGrid has exactly one SHIP at (5,5), set up in @BeforeEach
        // force the computer to hit it by pre-marking all other cells as MISS
        // so (5,5) is the only unattacked cell left
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (r != 5 || c != 5) {
                    homeGrid[r][c] = Cell.MISS;
                }
            }
        }

        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 5, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        // player misses so computer gets to fire
        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(GameStatus.LOSS, result[0].gameStatus());
    }

    @Test
    @DisplayName("LOSS: computer does not fire if player wins on their turn")
    void computerDoesNotFireAfterPlayerWins() {
        // shipGrid has exactly one SHIP at (2,3), hitting it wins immediately
        PlayerDTO human = new PlayerDTO(trackingGrid, homeGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS);

        processor.processAttack(2, 3, human, computer);

        // lastComputerRow stays -1 because the computer never fired
        assertEquals(-1, processor.getLastComputerRow());
        assertEquals(-1, processor.getLastComputerCol());
    }
}