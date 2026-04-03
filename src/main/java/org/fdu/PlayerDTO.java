package org.fdu;

/**
 * Data Transfer Object representing the current state of the Battleship board.
 * <p>
 * Holds a snapshot of the 10x10 grid. Other classes such as BattleBoard
 * or future managers will read from this DTO to make decisions without
 * directly accessing or modifying board internals.
 * </p>
 *
 * @param grid 2D array of Cell values representing the board state.
 *             Indexed as grid[col][row] where:
 *             col 0-9 maps to A-J
 *             row 0-9 maps to 1-10
 * @param isValidGuess stores the validity (true/false) of the guess that comes in
 * @param statusMessage stores the message of the player's guess based on its validity
 */
public record PlayerDTO(Cell[][] grid, boolean isValidGuess, String statusMessage) {}