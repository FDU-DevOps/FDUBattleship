package org.fdu;

/**
 * Data Transfer Object representing the current state of one side of the game.
 * <p>
 * The single DTO class in the project. BattleShipManager holds two instances:
 * humanDTO for the human player's tracking grid, and computerDTO for the
 * computer's ship grid. AttackProcessor reads from both and returns updated
 * copies. No other DTO classes exist.
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
 */

public record PlayerDTO(Cell[][] grid,Cell[][] homeGrid, int guessesLeft, GameStatus gameStatus) {}