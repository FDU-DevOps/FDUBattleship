package org.fdu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BattleShipManagerTest {

    private PlayerDTO player;
    private PlayerDTO prefilledPlayer;
    @Test
    @DisplayName("Validating user guesses.")

    void testValidatePlayerGuess()
    {
        BattleShipManager manager = new BattleShipManager();
        BattleBoard board = new BattleBoard();
        player = board.getState();

        // Status Messages
        String formatErrorStatusMessage = "Format Error: Column needs to be A-J and Row needs to 1-10";
        String invalidGuessHITCell = "Invalid Guess: HIT Cell already guessed";
        String invalidGuessMISSCell = "Invalid Guess: MISS Cell already guessed";
        String invalidMissingUserInput = "Input Error: Missing User Input";
        String validGuess = "Valid Guess";

        // Happy Path Testing
        String happyPathGuess = "B2";
        player = manager.validatePlayerGuess(happyPathGuess, player);
        assertTrue(player.isValidGuess(), "B2 should be a valid guess");
        assertEquals(validGuess, player.statusMessage());

        // Checking playerGuess == null
        player = manager.validatePlayerGuess(null, player);
        assertFalse(player.isValidGuess(), "No input is an invalid guess");
        assertEquals(invalidMissingUserInput, player.statusMessage());

        // Out of Bounds Test Cases
        String outOfBounds_3Index = "A11";
        player = manager.validatePlayerGuess(outOfBounds_3Index, player);
        assertFalse(player.isValidGuess(), "Guess Out of Bounds should be False");
        assertEquals(formatErrorStatusMessage, player.statusMessage());

        // Example: A100000 (out of bounds and beyond 3 index
        String outOfBounds_Over3Index = "A100000";
        player = manager.validatePlayerGuess(outOfBounds_Over3Index, player);
        assertFalse(player.isValidGuess(), "Guess Out of Bounds should be False");
        assertEquals(formatErrorStatusMessage, player.statusMessage());

        // Example: A100 (oob)
        String outOfBounds_3Index_truncation = "A100";
        player = manager.validatePlayerGuess(outOfBounds_3Index_truncation, player);
        assertFalse(player.isValidGuess(), "Guess Out of Bounds should be False");
        assertEquals(formatErrorStatusMessage, player.statusMessage());

        // Wrong Format (Flipped col,row) Test Cases
        // Example: 10J, flipped format and 10 coordinate
        String flippedFormat_3Index = "10J";
        player = manager.validatePlayerGuess(flippedFormat_3Index, player);
        assertFalse(player.isValidGuess(), "Guess should be in the correct col,row format");
        assertEquals(formatErrorStatusMessage, player.statusMessage());

        // Example 5A, flipped format and 1-9 coordinate
        String flippedFormat = "5A";
        player = manager.validatePlayerGuess(flippedFormat, player);
        assertFalse(player.isValidGuess(), "Guess should be in the correct col,row format");
        assertEquals(formatErrorStatusMessage, player.statusMessage());

        // Cell Status Test Cases -- using preFilled player method
        int xHitCoord = 0, yHitCoord = 0; // A1
        int xMissCoord = 9, yMissCoord = 9; // J10

        prefilledPlayer = manager.startGame(prefilledPlayer, xHitCoord, yHitCoord, xMissCoord, yMissCoord);
        // Example: HIT Cell
        prefilledPlayer = manager.validatePlayerGuess("A1", prefilledPlayer);
        assertFalse(prefilledPlayer.isValidGuess(), "Cannot guess a cell that has HIT status");
        assertEquals(invalidGuessHITCell, prefilledPlayer.statusMessage());

        // Example: MISS Cell
        prefilledPlayer = manager.validatePlayerGuess("J10", prefilledPlayer);
        assertFalse(prefilledPlayer.isValidGuess(), "Cannot guess a cell that has MISS status");
        assertEquals(invalidGuessMISSCell, prefilledPlayer.statusMessage());
    }

    @Test
    @DisplayName("Testing overloaded startGame Function")
    void testOverloadedStartGame()
    {
        BattleShipManager manager = new BattleShipManager();
        // HIT Coordinate is A1 or 0,0
        // MISS Coordinate is J10 or 9,9 in this case
        int xHitCoord = 0, yHitCoord = 0; // A1
        int xMissCoord = 9, yMissCoord = 9; // J10
        player = manager.startGame(player, xHitCoord, yHitCoord, xMissCoord, yMissCoord);
        assertEquals(Cell.HIT, player.grid()[xHitCoord][yHitCoord]);
        assertEquals(Cell.MISS,player.grid()[xMissCoord][yMissCoord]);
    }
}