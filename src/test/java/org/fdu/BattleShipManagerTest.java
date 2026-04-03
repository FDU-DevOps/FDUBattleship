package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BattleShipManagerTest {

    private PlayerDTO player;
    @Test
    @DisplayName("Validating user guesses.")

    void testValidatePlayerGuess()
    {
        BattleShipManager manager = new BattleShipManager();
        BattleBoard board = new BattleBoard();
        player = board.getState();

        // Out of Bounds Test Cases
        String outOfBounds_3Index = "A11";
        player = manager.validatePlayerGuess(outOfBounds_3Index, player);
        assertFalse(player.isValidGuess(), "Guess Out of Bounds should be False");

        // Example: A100000 (out of bounds and beyond 3 index
        String outOfBounds_Over3Index = "A100000";
        player = manager.validatePlayerGuess(outOfBounds_Over3Index, player);
        assertFalse(player.isValidGuess(), "Guess Out of Bounds should be False");

        // Example: A100 (oob)
        String outOfBounds_3Index_truncation = "A100";
        player = manager.validatePlayerGuess(outOfBounds_3Index_truncation, player);
        assertFalse(player.isValidGuess(), "Guess Out of Bounds should be False");

        // Wrong Format (Flipped col,row) Test Cases
        // Example: 10J, flipped format and 10 coordinate
        String flippedFormat_3Index = "10J";
        player = manager.validatePlayerGuess(flippedFormat_3Index, player);
        assertFalse(player.isValidGuess(), "Guess should be in the correct col,row format");

        // Example 5A, flipped format and 1-9 coordinate
        String flippedFormat = "5A";
        player = manager.validatePlayerGuess(flippedFormat, player);
        assertFalse(player.isValidGuess(), "Guess should be in the correct col,row format");


    }


}