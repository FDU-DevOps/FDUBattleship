package org.fdu;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

    public Ship {
        cells = deepCopyCellsAsUnmodifiableList(cells);
    }

    /**
     * Returns the number of cells this ship occupies.
     *
     * @return the size of the ship
     */
    public int size() { return cells.size(); }

    /**
     * Returns a deep copy of the cells list as an unmodifiable view,
     * so callers cannot mutate the ship's internal state.
     * Each {@code int[]} element is cloned individually.
     *
     * @return an unmodifiable deep copy of the cell coordinates,
     *         or {@code null} if the underlying list is {@code null}
     */
    @Override
    public List<int[]> cells() {
        return deepCopyCellsAsUnmodifiableList(cells);
    }

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

    /**
     * Creates a deep-copied, unmodifiable version of the given cell list.
     * Each {@code int[]} element is cloned individually to prevent external
     * mutation of internal state.
     *
     * @param source the original list of cell coordinates; may be {@code null}
     * @return an unmodifiable deep copy of {@code source},
     *         or {@code null} if {@code source} is {@code null}
     */
    private static List<int[]> deepCopyCellsAsUnmodifiableList(List<int[]> source) {
        if (source == null) return null;
        List<int[]> copy = new ArrayList<>(source.size());
        for (int[] cell : source) {
            copy.add(cell == null ? null : cell.clone());
        }
        return Collections.unmodifiableList(copy);
    }
}
