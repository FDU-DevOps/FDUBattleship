package org.fdu;
import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object representing the current state of one side of the game.
 * <p>
 * BattleShipManager holds two instances:
 * humanDTO for the human player's tracking grid, and computerDTO for the
 * computer's ship grid. AttackProcessor reads from both and returns updated
 * copies.
 * </p>
 *
 * @param grid        2D array of Cell values indexed as grid[row][col].
 *                    For humanDTO: the player's guess/tracking board, updated
 *                    after each attack to reflect HIT or MISS.
 *                    For computerDTO: the ship grid containing SHIP, WATER,
 *                    or HIT cells.
 * @param homeGrid    The human player's home board, where computer attacks land.
 *                    Contains SHIP, WATER, HIT, MISS. Null on computerDTO.
 * @param guessesLeft The number of guesses the human player has remaining.
 *                    Decremented by one after each MISS. Set to 0 on
 *                    computerDTO as it is unused for the computer side.
 * @param gameStatus  The current status of the game: IN_PROGRESS, WIN, or LOSS.
 * @param ships       list of Ship objects on this DTO's primary grid.
 *                    For computerDTO: the computer's fleet on its ship grid.
 *                    For humanDTO: the human's fleet on the tracking grid
 *                    (used only for sunk detection on the computer side).
 * @param homeShips   list of Ship objects on the human's home grid.
 *                    Only populated on humanDTO so the computer's attacks
 *                    can be checked for sunk ships. Null on computerDTO.
 */

public record PlayerDTO(Cell[][] grid,
                        Cell[][] homeGrid,
                        int guessesLeft,
                        GameStatus gameStatus,
                        List<Ship> ships,
                        List<Ship> homeShips
) implements Serializable {
    public PlayerDTO {
        grid = deepCopy(grid);
        homeGrid = deepCopy(homeGrid);
        ships = immutableCopyOfShips(ships);
        homeShips = immutableCopyOfShips(homeShips);
    }

    /**
     * Returns a deep copy of the primary grid, so callers cannot mutate
     * internal state.
     *
     * @return a deep copy of {@code grid}, or {@code null} if {@code grid} is {@code null}
     */
    @Override
    public Cell[][] grid() {
        return deepCopy(grid);
    }

    /**
     * Returns a deep copy of the home grid, so callers cannot mutate
     * internal state.
     *
     * @return a deep copy of {@code homeGrid}, or {@code null} if {@code homeGrid} is {@code null}
     */
    @Override
    public Cell[][] homeGrid() {
        return deepCopy(homeGrid);
    }

    /**
     * Returns the list of ships on this DTO's primary grid.
     *
     * @return the ships list, or {@code null} if none were provided
     */
    @Override
    public List<Ship> ships() {
        return ships;
    }

    /**
     * Returns the list of ships on the human's home grid.
     *
     * @return the home ships list, or {@code null} if none were provided
     */
    @Override
    public List<Ship> homeShips() {
        return homeShips;
    }



    /**
     * Creates a deep copy of a 2D {@link Cell} array, cloning each row individually.
     *
     * @param source the array to copy; may be {@code null}
     * @return a deep copy of {@code source}, or {@code null} if {@code source} is {@code null}
     */
    private static Cell[][] deepCopy(Cell[][] source) {
        if (source == null) return null;
        Cell[][] copy = new Cell[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] == null ? null : source[i].clone();
        }
        return copy;
    }

    /**
     * Wraps the given ship list in an unmodifiable view backed by a shallow copy,
     * preventing external modification of the internal list.
     * Ships themselves are immutable records, so a shallow copy is sufficient.
     *
     * @param source the list to copy; may be {@code null}
     * @return an unmodifiable copy of {@code source}, or {@code null} if {@code source} is {@code null}
     */
    private static List<Ship> immutableCopyOfShips(List<Ship> source) {
        if (source == null) return null;
        return List.copyOf(source);
    }
}