package org.fdu;

/**
 * Represents the possible states of a single cell on the Battleship board.
 * <p>
 * Each cell on the 10x10 grid holds one of these values at any given time.
 * For this story, only WATER is defined, representing a blank/empty cell.
 * </p>
 */

public enum Cell
{
    WATER,
    SHIP,
    HIT,
    MISS
}
