package org.fdu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stateless service responsible for resolving a single Battleship turn.
 * <p>
 * Receives the player's guess coordinates along with both {@link PlayerDTO}
 * instances, resolves whether the attack is a HIT, MISS, or ship-sunk event,
 * fires the computer's random counter-attack, and returns a {@link TurnResultDTO}
 * carrying all updated state and metadata for the turn.
 * </p>
 * <p>
 * This class holds no mutable fields. Every piece of per-turn data
 * (sunk ships, computer move coordinates) is returned inside {@link TurnResultDTO}
 * rather than stored as instance state, making this class safe to use as a
 * Spring singleton.
 * </p>
 */
public class AttackProcessor {

    private final Random random;

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
     * After a HIT, checks whether the struck ship is now fully sunk.
     * Win/loss resolution: player wins when all computer ship cells are gone;
     * computer wins when all home ship cells are gone. Guesses-left loss
     * is kept as a fallback.
     * </p>
     *
     * @param row         row index of the player's attack (0-9)
     * @param col         column index of the player's attack (0-9)
     * @param humanDTO    current human player state
     * @param computerDTO current computer state
     * @return TurnResultDTO with updated DTOs, sunk-ship references,
     *         and the computer's move coordinates
     */
    public TurnResultDTO processAttack(int row, int col, PlayerDTO humanDTO, PlayerDTO computerDTO) {

        // Deep-copy all grids so incoming DTOs remain immutable
        Cell[][] newShipGrid = copyGrid(computerDTO.grid());
        Cell[][] newTrackingGrid = copyGrid(humanDTO.grid());
        Cell[][] newHomeGrid = copyGrid(humanDTO.homeGrid());

        // ----------------------------------------------------------------
        // Resolve player's attack
        // ----------------------------------------------------------------
        Cell target = newShipGrid[row][col];
        Ship sunkShip = null;

        if (target == Cell.SHIP) {
            newShipGrid[row][col] = Cell.HIT;
            newTrackingGrid[row][col] = Cell.HIT;
            sunkShip = findSunkShip(computerDTO.ships(), newShipGrid, row, col);
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
        if (allShipsSunk(newShipGrid)) {
            PlayerDTO updatedHuman = new PlayerDTO(newTrackingGrid, newHomeGrid, guessesLeft,
                    GameStatus.WIN, humanDTO.ships(), humanDTO.homeShips());
            PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, null, 0,
                    GameStatus.LOSS, computerDTO.ships(), null);
            return new TurnResultDTO(updatedHuman, updatedComputer, sunkShip, null, -1, -1);
        }

        // ----------------------------------------------------------------
        // Check if the player ran out of guesses
        // ----------------------------------------------------------------
        if (guessesLeft <= 0) {
            PlayerDTO updatedHuman = new PlayerDTO(newTrackingGrid, newHomeGrid, 0,
                    GameStatus.LOSS, humanDTO.ships(), humanDTO.homeShips());
            PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, null, 0,
                    GameStatus.WIN, computerDTO.ships(), null);
            return new TurnResultDTO(updatedHuman, updatedComputer, sunkShip, null, -1, -1);
        }

        // ----------------------------------------------------------------
        // Computer's random move on the human's home grid
        // ----------------------------------------------------------------
        int[] computerMove = pickRandomUnattackedCell(newHomeGrid);
        int computerRow = computerMove[0];
        int computerCol = computerMove[1];

        Ship homeSunkShip = null;
        Cell homeTarget = newHomeGrid[computerRow][computerCol];

        if (homeTarget == Cell.SHIP) {
            newHomeGrid[computerRow][computerCol] = Cell.HIT;
            homeSunkShip = findSunkShip(humanDTO.homeShips(), newHomeGrid, computerRow, computerCol);
        } else {
            newHomeGrid[computerRow][computerCol] = Cell.MISS;
        }

        // ----------------------------------------------------------------
        // Check if the computer just won
        // ----------------------------------------------------------------
        boolean computerWon = allShipsSunk(newHomeGrid);
        GameStatus humanStatus = computerWon ? GameStatus.LOSS : GameStatus.IN_PROGRESS;

        PlayerDTO updatedHuman = new PlayerDTO(newTrackingGrid, newHomeGrid, guessesLeft,
                humanStatus, humanDTO.ships(), humanDTO.homeShips());
        PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, null, 0,
                GameStatus.IN_PROGRESS, computerDTO.ships(), null);

        return new TurnResultDTO(updatedHuman, updatedComputer, sunkShip, homeSunkShip, computerRow, computerCol);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Scans the ship list to find the ship occupying (row, col) and checks
     * whether that specific ship is now fully sunk on the given grid.
     */
    private Ship findSunkShip(List<Ship> ships, Cell[][] grid, int row, int col) {
        if (ships == null) return null;
        for (Ship ship : ships) {
            for (int[] cellCoords : ship.cells()) {
                if (cellCoords[0] == row && cellCoords[1] == col) {
                    return ship.isSunk(grid) ? ship : null;
                }
            }
        }
        return null;
    }

    /**
     * Creates a deep copy of a 2D Cell array so mutations to the returned grid
     * do not affect the original.
     */
    private Cell[][] copyGrid(Cell[][] original) {
        Cell[][] copy = new Cell[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * Returns {@code true} when no SHIP cells remain on the given grid,
     * meaning all ships have been sunk.
     */
    private boolean allShipsSunk(Cell[][] grid) {
        for (Cell[] row : grid)
            for (Cell c : row)
                if (c == Cell.SHIP) return false;
        return true;
    }

    /**
     * Selects a random cell from all cells not yet attacked on the given grid.
     * Assumes at least one unattacked cell exists.
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