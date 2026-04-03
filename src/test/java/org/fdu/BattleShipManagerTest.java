package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BattleShipManagerTest {

    @Test
    @DisplayName("Validating user guesses.")
    void testValidatePlayerGuess()
    {
        BattleShipManager manager = new BattleShipManager();

        // Out of Bounds Test Cases
        // Example: A11 (OOB)
        String outOfBounds_3Index = "A11";
        assertFalse(manager.validatePlayerGuess(outOfBounds_3Index));

        // Example: A100 (oob)
        String outOfBounds_3Index_truncation = "A100";
        assertFalse(manager.validatePlayerGuess(outOfBounds_3Index_truncation));

        // Wrong Format (Flipped col,row)
        String flippedFormat_3Index = "10J";
        assertFalse(manager.validatePlayerGuess(flippedFormat_3Index));

        String flippedFormat = "5A";
        assertFalse(manager.validatePlayerGuess(flippedFormat));

        

        // Example: A1000000 (OOB AND more than index 3)
    }


}