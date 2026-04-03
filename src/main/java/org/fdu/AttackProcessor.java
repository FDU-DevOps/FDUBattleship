package org.fdu;

/**
 * Service class responsible for processing a player's attack against the
 * computer's ship grid.
 * <p>
 * Receives the player's guess coordinates along with both PlayerDTO instances,
 * resolves whether the attack is a HIT or MISS, and returns updated versions
 * of both as a PlayerDTO array. BattleShipManager delegates all guess
 * processing here rather than performing grid checks directly.
 * </p>
 */

public class AttackProcessor {

    /**
     * Processes a single attack from the player against the computer's board.
     * <p>
     * Checks the target cell on computerDTO's grid. If the cell contains SHIP,
     * the attack is a HIT: both grids are updated to HIT and humanDTO game
     * status is set to WIN. If the cell contains WATER, the attack is a MISS:
     * both grids are updated to MISS, guessesLeft is decremented by one, and
     * if guessesLeft reaches zero game status is set to LOSS.
     * BattleShipManager derives hit/miss for display by reading
     * humanDTO.grid()[row][col] after the call, no AttackResult enum needed.
     * </p>
     *
     * @param row       the row index of the attack (0-9, maps to 1-10)
     * @param col       the column index of the attack (0-9, maps to A-J)
     * @param humanDTO    the current human player state, including tracking grid,
     *                    guesses remaining, and game status
     * @param computerDTO the current computer state, including the ship grid
     * @return PlayerDTO[] of length 2: [0] updated humanDTO, [1] updated computerDTO
     */

    public PlayerDTO[] processAttack(int row, int col, PlayerDTO humanDTO, PlayerDTO computerDTO) {
        // Work on copies so the original immutable DTOs are never mutated
        Cell[][] newShipGrid     = copyGrid(computerDTO.grid());
        Cell[][] newTrackingGrid = copyGrid(humanDTO.grid());

        // Read what occupies the targeted cell on the computer's ship grid
        Cell target = newShipGrid[row][col];

        if (target == Cell.SHIP) {
            // Mark the cell as HIT on both grids so both DTOs stay in sync
            newShipGrid[row][col]     = Cell.HIT;
            newTrackingGrid[row][col] = Cell.HIT;

            // guessesLeft is preserved unchanged, a win ends the game before it matters
            PlayerDTO updatedHuman    = new PlayerDTO(newTrackingGrid, humanDTO.guessesLeft(), GameStatus.WIN);
            // Computer carries no meaningful status, IN_PROGRESS is used as a neutral placeholder
            PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, 0, GameStatus.IN_PROGRESS);
            return new PlayerDTO[]{ updatedHuman, updatedComputer };

        } else {
            // No ship was hit, mark the cell as MISS on both grids
            newShipGrid[row][col]     = Cell.MISS;
            newTrackingGrid[row][col] = Cell.MISS;

            // Consume one guess and decide whether the player has just run out
            int guessesLeft = humanDTO.guessesLeft() - 1;

            GameStatus status;
            if (guessesLeft == 0) {
                status = GameStatus.LOSS;
            } else {
                status = GameStatus.IN_PROGRESS;
            };

            PlayerDTO updatedHuman    = new PlayerDTO(newTrackingGrid, guessesLeft, status);
            PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, 0, GameStatus.IN_PROGRESS);
            return new PlayerDTO[]{ updatedHuman, updatedComputer };
        }
    }

    /**
     * Creates a deep copy of a 2D Cell array.
     * <p>
     * Clones each row individually so that mutations to the returned grid
     * do not affect the original. Used before modifying ship or tracking
     * grids inside processAttack to preserve the immutability of the
     * incoming PlayerDTO instances.
     * </p>
     *
     * @param original the 2D Cell array to copy, indexed as original[row][col]
     * @return a new Cell[][] with the same dimensions and values as the original
     */

    private Cell[][] copyGrid(Cell[][] original) {
        Cell[][] copy = new Cell[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            // clone() is safe here because Cell is an enum, so its values are shared constants
            copy[i] = original[i].clone();
        }
        return copy;
    }
}