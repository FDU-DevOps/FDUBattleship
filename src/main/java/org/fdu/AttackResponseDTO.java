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
 * @param isError         true if the request was invalid and no game state was changed.
 *                        Triggered by out-of-bounds coordinates, a cell already attacked,
 *                        or no active game session found.
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
        boolean isError
) {}