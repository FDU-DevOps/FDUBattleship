package org.fdu;
/**
 * Represents the possible states of a single cell on the Battleship board.
 * <p>
 * Each cell on the 10x10 grid holds one of these values at any given time.
 * WATER represents a blank/empty cell. SHIP represents a cell occupied by
 * a ship. HIT represents a cell that was attacked and contained a ship.
 * MISS represents a cell that was attacked but contained no ship.
 * </p>
 */

public enum Cell {
    WATER,
    SHIP,
    HIT,
    MISS
}