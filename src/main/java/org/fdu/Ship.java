package org.fdu;
import java.io.Serializable;
import java.util.List;
/**
 * Immutable value representing one ship on the board.
 * <p>
 * Stores the coordinates of every cell it occupies so AttackProcessor
 * can check after each HIT whether the whole ship is sunk, without
 * scanning the full grid.
 * </p>
 *
 * @param cells each element is an int[]{row, col} for one ship cell
 */
public record Ship(List<int[]> cells) implements Serializable {

    /**
     * Returns true when every cell of this ship reads HIT on the given grid.
     *
     * @param grid the grid to inspect (computer ship grid or human home grid)
     */
    public boolean isSunk(Cell[][] grid) {
        for (int[] cell : cells) {
            if (grid[cell[0]][cell[1]] != Cell.HIT) return false;
        }
        return true;
    }

    /** Number of cells this ship occupies. */
    public int size() { return cells.size(); }
}
