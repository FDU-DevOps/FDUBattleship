package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttackProcessorTest {

    private AttackProcessor processor;
    private Cell[][] shipGrid;
    private Cell[][] trackingGrid;

    @BeforeEach
    void setUp() {
        processor = new AttackProcessor();

        // 10x10 ship grid: one SHIP cell at (2, 3), rest WATER
        shipGrid = new Cell[10][10];
        trackingGrid = new Cell[10][10];
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                shipGrid[r][c]    = Cell.WATER;
                trackingGrid[r][c] = Cell.WATER;
            }
        }
        shipGrid[2][3] = Cell.SHIP;
    }

    // HIT tests

    @Test
    @DisplayName("HIT: returns array of length 2")
    void hitReturnsArrayOfTwo() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(2, result.length);
    }

    @Test
    @DisplayName("HIT: tracking grid cell is updated to HIT")
    void hitUpdatesTrackingGrid() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(Cell.HIT, result[0].grid()[2][3]);
    }

    @Test
    @DisplayName("HIT: ship grid cell is updated to HIT")
    void hitUpdatesShipGrid() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(Cell.HIT, result[1].grid()[2][3]);
    }

    @Test
    @DisplayName("HIT: human game status is set to WIN")
    void hitSetsHumanStatusToWin() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 10, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(GameStatus.WIN, result[0].gameStatus());
    }

    @Test
    @DisplayName("HIT: guesses remaining are unchanged after hitting a ship")
    void hitDoesNotDecrementGuesses() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 7, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(2, 3, human, computer);

        assertEquals(7, result[0].guessesLeft());
    }

    //MISS tests

    @Test
    @DisplayName("MISS: tracking grid cell is updated to MISS")
    void missUpdatesTrackingGrid() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 5, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(Cell.MISS, result[0].grid()[0][0]);
    }

    @Test
    @DisplayName("MISS: ship grid cell is updated to MISS")
    void missUpdatesShipGrid() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 5, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(Cell.MISS, result[1].grid()[0][0]);
    }

    @Test
    @DisplayName("MISS: guesses remaining are decremented by one")
    void missDecrementsGuesses() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 5, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(4, result[0].guessesLeft());
    }

    @Test
    @DisplayName("MISS: status stays IN_PROGRESS when guesses remain")
    void missKeepsInProgressWhenGuessesRemain() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 3, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(GameStatus.IN_PROGRESS, result[0].gameStatus());
    }

    @Test
    @DisplayName("MISS: status is set to LOSS when last guess is consumed")
    void missOnLastGuessSetsLoss() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 1, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(GameStatus.LOSS, result[0].gameStatus());
    }

    @Test
    @DisplayName("MISS: guesses reach zero after consuming the last guess")
    void missOnLastGuessResultsInZeroGuesses() {
        PlayerDTO human = new PlayerDTO(trackingGrid, 1, GameStatus.IN_PROGRESS);
        PlayerDTO computer = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        PlayerDTO[] result = processor.processAttack(0, 0, human, computer);

        assertEquals(0, result[0].guessesLeft());
    }
}