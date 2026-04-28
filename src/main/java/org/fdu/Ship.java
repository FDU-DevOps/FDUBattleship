package org.fdu;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import java.io.Serial;
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
@SuppressFBWarnings(value = {"EI_EXPOSE_REP"},
        justification = "DTO for testing requires direct mutable array access")

public record Ship(List<int[]> cells) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Returns true when every cell of this ship reads HIT on the given grid.
     *
     * @param grid the grid to inspect (computer ship grid or human home grid)
     */
    public boolean isSunk(Cell[][] grid) {
        for (int[] cell : cells) {
            if (grid[cell[0]][cell[1]] != Cell.HIT) {
                return false;
            }
        }
        return true;
    }

    /** Number of cells this ship occupies. */
    public int size() {
        return cells.size();
    }
}
