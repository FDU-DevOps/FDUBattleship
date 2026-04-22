package org.fdu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service class responsible for processing a player's attack against the
 * computer's ship grid.
 * <p>
 * Receives the player's guess coordinates along with both PlayerDTO instances,
 * resolves whether the attack is a HIT, MISS, or ship-sunk event, and returns
 * updated versions of both as a PlayerDTO array. BattleShipManager delegates
 * all guess processing here rather than performing grid checks directly.
 * </p>
 * <p>
 * After each call, getLastSunkShip() and getLastHomeSunkShip() expose the ship
 * that was sunk this turn (if any), so BoardController can build the right
 * message without coupling the controller to grid logic.
 * </p>
 */
public class AttackProcessor {

    private int lastComputerRow = -1;
    private int lastComputerCol = -1;

    // Ship sunk this turn by the player (null if none)
    private Ship lastSunkShip = null;
    // Ship sunk this turn by the computer (null if none)
    private Ship lastHomeSunkShip = null;

    private final Random random;

    public int getLastComputerRow()   { return lastComputerRow; }
    public int getLastComputerCol()   { return lastComputerCol; }
    public Ship getLastSunkShip()      { return lastSunkShip; }
    public Ship getLastHomeSunkShip()  { return lastHomeSunkShip; }

    public AttackProcessor() {
        this.random = new Random();
    }

    public AttackProcessor(Random random) {
        this.random = random;
    }

    /**
     * Processes a single attack from the player against the computer's board,
     * then fires the computer's random counter-attack against the human's home grid.
     * <p>
     * After a HIT, checks whether the struck ship is now fully sunk. If so,
     * lastSunkShip is set to that ship, otherwise it is null. The same check
     * runs for the computer's counter-attack against homeShips, stored in
     * lastHomeSunkShip.
     * </p>
     * <p>
     * Win/loss resolution:
     * Player wins when allShipsSunk returns true for the computer's grid.
     * Computer wins when allShipsSunk returns true for the human's home grid.
     * Guesses-left loss is still present as a fallback.
     * </p>
     *
     * @param row         the row index of the attack (0-9, maps to 1-10)
     * @param col         the column index of the attack (0-9, maps to A-J)
     * @param humanDTO    the current human player state, including tracking grid,
     *                    guesses remaining, and game status
     * @param computerDTO the current computer state, including the ship grid
     * @return PlayerDTO[] of length 2: [0] updated humanDTO, [1] updated computerDTO
     */
    public PlayerDTO[] processAttack(int row, int col, PlayerDTO humanDTO, PlayerDTO computerDTO) {
        // Reset per-turn state
        lastComputerRow = -1;
        lastComputerCol = -1;
        lastSunkShip = null;
        lastHomeSunkShip = null;

        // Deep-copy all grids so incoming DTOs remain immutable
        Cell[][] newShipGrid = copyGrid(computerDTO.grid());
        Cell[][] newTrackingGrid = copyGrid(humanDTO.grid());
        Cell[][] newHomeGrid = copyGrid(humanDTO.homeGrid());

        // ----------------------------------------------------------------
        // Resolve player's attack
        // ----------------------------------------------------------------
        Cell target = newShipGrid[row][col];
        if (target == Cell.SHIP) {
            newShipGrid[row][col] = Cell.HIT;
            newTrackingGrid[row][col] = Cell.HIT;

            // Pass row and col to check ONLY the ship that was hit
            lastSunkShip = findSunkShip(computerDTO.ships(), newShipGrid, row, col);
        } else {
            newShipGrid[row][col] = Cell.MISS;
            newTrackingGrid[row][col] = Cell.MISS;
        }

        int guessesLeft = (target == Cell.SHIP)
                ? humanDTO.guessesLeft()
                : humanDTO.guessesLeft() - 1;

        // ----------------------------------------------------------------
        // Check if the player just won
        // ----------------------------------------------------------------
        boolean playerWon = allShipsSunk(newShipGrid);
        if (playerWon) {
            PlayerDTO updatedHuman = new PlayerDTO(newTrackingGrid, newHomeGrid, guessesLeft,
                    GameStatus.WIN, humanDTO.ships(), humanDTO.homeShips());
            PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, null, 0,
                    GameStatus.LOSS, computerDTO.ships(), null);
            System.out.println("Player wins! All computer ships sunk.");
            return new PlayerDTO[]{ updatedHuman, updatedComputer };
        }

        // ----------------------------------------------------------------
        // Check if guesses reached 0
        // ----------------------------------------------------------------
        if (guessesLeft <= 0) {
            System.out.println("Player ran out of guesses. Computer wins!");
            PlayerDTO updatedHuman = new PlayerDTO(newTrackingGrid, newHomeGrid, 0,
                    GameStatus.LOSS, humanDTO.ships(), humanDTO.homeShips());
            PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, null, 0,
                    GameStatus.WIN, computerDTO.ships(), null);
            return new PlayerDTO[]{ updatedHuman, updatedComputer };
        }

        // ----------------------------------------------------------------
        // Computer's random move on the human's home grid
        // ----------------------------------------------------------------
        int[] computerMove = pickRandomUnattackedCell(newHomeGrid);
        lastComputerRow = computerMove[0];
        lastComputerCol = computerMove[1];

        Cell homeTarget = newHomeGrid[lastComputerRow][lastComputerCol];
        if (homeTarget == Cell.SHIP) {
            newHomeGrid[lastComputerRow][lastComputerCol] = Cell.HIT;

            // Pass lastComputerRow and lastComputerCol
            lastHomeSunkShip = findSunkShip(humanDTO.homeShips(), newHomeGrid, lastComputerRow, lastComputerCol);
        }else {
            newHomeGrid[lastComputerRow][lastComputerCol] = Cell.MISS;
        }

        // ----------------------------------------------------------------
        // Check if the computer just won
        // ----------------------------------------------------------------
        boolean computerWon = allShipsSunk(newHomeGrid);
        GameStatus humanStatus = computerWon ? GameStatus.LOSS : GameStatus.IN_PROGRESS;
        if (computerWon) System.out.println("Computer wins! All player ships sunk.");

        PlayerDTO updatedHuman = new PlayerDTO(newTrackingGrid, newHomeGrid, guessesLeft,
                humanStatus, humanDTO.ships(), humanDTO.homeShips());

        PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, null, 0,
                GameStatus.IN_PROGRESS, computerDTO.ships(), null);
        return new PlayerDTO[]{ updatedHuman, updatedComputer };
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Scans the ship list to find the ship occupying (row, col) and checks
     * if that specific ship is now fully sunk.
     */
    private Ship findSunkShip(List<Ship> ships, Cell[][] grid, int row, int col) {
        if (ships == null) return null;

        for (Ship ship : ships) {
            // Check if this ship occupies the cell that was just hit
            for (int[] cellCoords : ship.cells()) {
                if (cellCoords[0] == row && cellCoords[1] == col) {
                    // If this is the ship we hit, check if it's now sunk
                    return ship.isSunk(grid) ? ship : null;
                }
            }
        }
        return null;
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
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * Checks whether all ships on the given grid have been sunk.
     * <p>
     * Scans every cell in the grid and looks for any remaining SHIP cell.
     * If at least one SHIP cell is found, the method returns false because
     * ships are still afloat. If no SHIP cells remain after scanning the
     * entire grid, the method returns true.
     * </p>
     *
     * @param grid the grid to inspect, either the computer's ship grid or  the human player's home grid
     * @return true if no SHIP cells remain, false if at least one ship is still present
     */
    private boolean allShipsSunk(Cell[][] grid) {
        for (Cell[] row : grid)
            for (Cell c : row)
                if (c == Cell.SHIP) return false;
        return true;
    }

    /**
     * Selects a random cell from all cells not yet attacked on the given grid.
     * <p>
     * Builds a list of all cells whose value is not HIT and not MISS, then
     * picks one at random. Used exclusively by the computer's move logic in
     * processAttack(). Assumes at least one unattacked cell exists, which is
     * guaranteed as long as the game has not already ended before this is called.
     * </p>
     *
     * @param grid the human's home grid
     * @return int[]{row, col} of the chosen cell
     */
    private int[] pickRandomUnattackedCell(Cell[][] grid) {
        List<int[]> available = new ArrayList<>();
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[r].length; c++)
                if (grid[r][c] != Cell.HIT && grid[r][c] != Cell.MISS)
                    available.add(new int[]{ r, c });

        return available.get(random.nextInt(available.size()));
    }
}