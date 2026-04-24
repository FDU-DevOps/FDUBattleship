package org.fdu;

/**
 * Represents the overall status of the Battleship game.
 * <p>
 * Used by PlayerDTO to signal whether the game is still in progress,
 * has been won, or has been lost. BattleShipManager checks this value
 * after each turn to determine whether to continue the game loop.
 * </p>
 */

public enum GameStatus {
   PLACEMENT,
    IN_PROGRESS,
    WIN,
    LOSS
}
