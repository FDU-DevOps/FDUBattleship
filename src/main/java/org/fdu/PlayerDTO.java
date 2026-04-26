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
) implements Serializable {}
