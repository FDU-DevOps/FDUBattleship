package org.fdu;

/**
 * Application entry point for the Battleship game.
 * <p>
 * Instantiates BattleshipManager, which initializes all game state,
 * then delegates the full game loop to startGame().
 * </p>
 */

public class App {
    public static void main(String[] args) {
        BattleshipManager manager = new BattleshipManager();
        manager.startGame();
    }
}