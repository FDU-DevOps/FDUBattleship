package org.fdu;

/**
 * Data Transfer Object returned to the frontend after each attack turn.
 * <p>
 * Carries the full state of both boards plus descriptive messages for both
 * the player's move and the computer's retaliatory move. The frontend uses
 * this single response to update both board displays simultaneously after
 * every turn.
 * </p>
 * <p>
 * If the player wins on their turn, the computer does not fire. In that case,
 * computerRow and computerCol are -1 and computerMessage is an empty string.
 * </p>
 * <p>
 * If isError is true, the attack was not processed and no game state changed.
 * All grid fields may be null in that case.
 * </p>
 *
 * @param grid            the player's tracking board after the turn, as lowercase strings.
 *                        Values: "water", "ship", "hit", "miss".
 *                        Reflects only hits and misses on computer ships,
 *                        never exposes computer ship positions as "ship".
 * @param homeGrid        the player's home board after the turn, as lowercase strings.
 *                        Values: "water", "ship", "hit", "miss".
 *                        Always shows the player's own ship positions alongside
 *                        computer attacks so the player can see what has been sunk.
 * @param guessesLeft     the number of guesses the human player has remaining.
 *                        Decremented by one on each MISS by the player.
 *                        No longer the primary loss condition, kept for display purposes.
 * @param gameStatus      the current game state after both moves this turn.
 *                        One of: "IN_PROGRESS", "WIN", "LOSS".
 *                        WIN means the player sank the last computer ship.
 *                        LOSS means the computer sank the last player ship.
 * @param message         human-readable result of the player's attack this turn.
 *                        Examples: "Hit!", "Miss!".
 * @param computerRow     row index (0-9) of the cell the computer attacked this turn.
 *                        -1 if the computer did not fire because the player won first.
 * @param computerCol     column index (0-9) of the cell the computer attacked this turn.
 *                        -1 if the computer did not fire because the player won first.
 * @param computerMessage human-readable result of the computer's attack this turn.
 *                        Examples: "Computer hit your ship at B3!", "Computer missed at G7".
 *                        Empty string if the computer did not fire this turn.
 * @param sunkCells       row/col pairs of every cell belonging to the computer ship the
 *                        player just sunk, so the frontend can apply a sunk visual.
 *                        Null when the player's attack did not sink a ship.
 * @param homeSunkCells   row/col pairs of every cell belonging to the player ship the
 *                        computer just sunk. Null when the computer did not sink a ship.
 */
public record AttackResponseDTO(
        String[][] grid,
        String[][] homeGrid,
        int guessesLeft,
        String gameStatus,
        String message,
        int computerRow,
        int computerCol,
        String computerMessage,
        int[][] sunkCells,
        int[][] homeSunkCells
) {
    public AttackResponseDTO {
        grid = deepCopy(grid);
        homeGrid = deepCopy(homeGrid);
        sunkCells = deepCopy(sunkCells);
        homeSunkCells = deepCopy(homeSunkCells);
    }

    /**
     * Returns a deep copy of the player's tracking board, so callers cannot
     * mutate internal state.
     *
     * @return a deep copy of {@code grid}, or {@code null} if {@code grid} is {@code null}
     */
    @Override
    public String[][] grid() {
        return deepCopy(grid);
    }

    /**
     * Returns a deep copy of the player's home board, so callers cannot
     * mutate internal state.
     *
     * @return a deep copy of {@code homeGrid}, or {@code null} if {@code homeGrid} is {@code null}
     */
    @Override
    public String[][] homeGrid() {
        return deepCopy(homeGrid);
    }

    /**
     * Returns a deep copy of the sunk computer ship cells, so callers cannot
     * mutate internal state.
     *
     * @return a deep copy of {@code sunkCells}, or {@code null} if no ship was sunk this turn
     */
    @Override
    public int[][] sunkCells() {
        return deepCopy(sunkCells);
    }

    /**
     * Returns a deep copy of the sunk player ship cells, so callers cannot
     * mutate internal state.
     *
     * @return a deep copy of {@code homeSunkCells}, or {@code null} if no ship was sunk this turn
     */
    @Override
    public int[][] homeSunkCells() {
        return deepCopy(homeSunkCells);
    }


    /**
     * Creates a deep copy of a 2D {@link String} array, cloning each row individually.
     *
     * @param source the array to copy; may be {@code null}
     * @return a deep copy of {@code source}, or {@code null} if {@code source} is {@code null}
     */
    private static String[][] deepCopy(String[][] source) {
        if (source == null) return null;
        String[][] copy = new String[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] == null ? null : source[i].clone();
        }
        return copy;
    }

    /**
     * Creates a deep copy of a 2D {@code int} array, cloning each row individually.
     *
     * @param source the array to copy; may be {@code null}
     * @return a deep copy of {@code source}, or {@code null} if {@code source} is {@code null}
     */
    private static int[][] deepCopy(int[][] source) {
        if (source == null) return null;
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] == null ? null : source[i].clone();
        }
        return copy;
    }
}