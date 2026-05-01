package org.fdu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stateless service responsible for resolving a single Battleship turn.
 * <p>
 * Receives the player's guess coordinates along with both {@link PlayerDTO}
 * instances, resolves whether the attack is a HIT, MISS, or ship-sunk event,
 * fires the computer's random counter-attack,
 * and returns a {@link TurnResultDTO}
 * carrying all updated state and metadata for the turn.
 * </p>
 * <p>
 * This class holds no mutable fields.
 * Every piece of per-turn data
 * (sunk ships, computer move coordinates) is returned inside {@link TurnResultDTO}
 * rather than stored as instance state, making this class safe to use as a
 * Spring singleton.
 * </p>
 */
public class AttackProcessor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AttackProcessor.class);

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
        LOG.debug("Processing turn. Player attacks row={}, col={}", row, col);

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
            LOG.debug("Player HIT at ({}, {})", row, col);
            if (sunkShip != null) {
                LOG.info("Player sunk computer ship of size {}", sunkShip.size());
            }
        } else {
            newShipGrid[row][col] = Cell.MISS;
            newTrackingGrid[row][col] = Cell.MISS;
            LOG.debug("Player MISS at ({}, {})", row, col);
        }

        int guessesLeft = (target == Cell.SHIP)
                ? humanDTO.guessesLeft()
                : humanDTO.guessesLeft() - 1;

        LOG.debug("Guesses left after player's move: {}", guessesLeft);

        // ----------------------------------------------------------------
        // Check if the player just won or ran out of guesses
        // ----------------------------------------------------------------
        if (allShipsSunk(newShipGrid)) {
            LOG.info("Player wins! All computer ships sunk.");
            PlayerDTO updatedHuman = new PlayerDTO(newTrackingGrid, newHomeGrid, guessesLeft,
                    GameStatus.WIN, humanDTO.ships(), humanDTO.homeShips());
            PlayerDTO updatedComputer = new PlayerDTO(newShipGrid, null, 0,
                    GameStatus.LOSS, computerDTO.ships(), null);
            return new TurnResultDTO(updatedHuman, updatedComputer, sunkShip, null, -1, -1);
        }



        // ----------------------------------------------------------------
        // Computer's random move
        // ----------------------------------------------------------------
        List<Ship> remainingShips = (humanDTO.homeShips() != null)
                ? humanDTO.homeShips().stream()
                .filter(s -> !s.isSunk(newHomeGrid))
                .collect(java.util.stream.Collectors.toList())
                : new ArrayList<>();
        System.out.println("Remaining unsunk ships: " + remainingShips.size());
        int[] computerMove = pickComputerMove(newHomeGrid, remainingShips);
        int computerRow = computerMove[0];
        int computerCol = computerMove[1];

        Ship homeSunkShip = null;
        Cell homeTarget = newHomeGrid[computerRow][computerCol];

        if (homeTarget == Cell.SHIP) {
            newHomeGrid[computerRow][computerCol] = Cell.HIT;
            homeSunkShip = findSunkShip(humanDTO.homeShips(), newHomeGrid, computerRow, computerCol);
            LOG.debug("Computer HIT at ({}, {})", computerRow, computerCol);
            if (homeSunkShip != null) {
                LOG.info("Computer sunk player ship of size {}", homeSunkShip.size());
            }
        } else {
            newHomeGrid[computerRow][computerCol] = Cell.MISS;
            LOG.debug("Computer MISS at ({}, {})", computerRow, computerCol);
        }

        // ----------------------------------------------------------------
        // Check if the computer just won
        // ----------------------------------------------------------------
        boolean computerWon = allShipsSunk(newHomeGrid);
        GameStatus humanStatus = computerWon ? GameStatus.LOSS : GameStatus.IN_PROGRESS;

        if (computerWon) {
            LOG.info("Computer wins! All player ships sunk.");
        }

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
        if (ships == null) {
            return null;
        }
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
        for (Cell[] row : grid) {
            for (Cell c : row) {
                if (c == Cell.SHIP) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if there are any HIT cells on the grid that belong to
     * a ship not yet fully sunk — meaning the computer has partially hit
     * a ship and should focus on finishing it before hunting elsewhere.
     *
     * @param grid      the human's home grid
     * @param remaining list of unsunk player ships
     * @return true if target mode should be active, false for hunt mode
     */
    private boolean isTargetMode(Cell[][] grid, List<Ship> remaining) {
        // For each remaining ship, check if any of its cells are HIT
        // If yes, the computer has a partial hit and should be in target mode
        for (Ship ship : remaining) {
            for (int[] cell : ship.cells()) {
                if (grid[cell[0]][cell[1]] == Cell.HIT) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Selects the computer's next attack cell when in Target mode.
     * Collects all unattacked neighbors of existing HIT cells.
     * If two adjacent HITs are found (axis known), only returns
     * neighbors along that axis to avoid wasting moves.
     *
     * @param grid the human's home grid
     * @return int[]{row, col} of the chosen target cell
     */
    private int[] pickTargetModeCell(Cell[][] grid) {
        List<int[]> hitCells = new ArrayList<>();

        // Collect all HIT cells on the grid
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == Cell.HIT) {
                    hitCells.add(new int[]{r, c});
                }
            }
        }

        // Check if two adjacent hits exist — if so, lock in the axis
        boolean axisLocked = false;
        boolean horizontal = false;
        for (int[] hit : hitCells) {
            int r = hit[0], c = hit[1];
            // Check right neighbor
            if (c + 1 < grid[r].length && grid[r][c + 1] == Cell.HIT) {
                axisLocked = true;
                horizontal = true;
                break;
            }
            // Check down neighbor
            if (r + 1 < grid.length && grid[r + 1][c] == Cell.HIT) {
                axisLocked = true;
                horizontal = false;
                break;
            }
        }

        // Collect valid candidates based on axis
        List<int[]> candidates = new ArrayList<>();
        for (int[] hit : hitCells) {
            int r = hit[0], c = hit[1];
            int[][] neighbors = axisLocked
                    ? (horizontal
                    ? new int[][]{{r, c - 1}, {r, c + 1}}   // horizontal axis only
                    : new int[][]{{r - 1, c}, {r + 1, c}})  // vertical axis only
                    : new int[][]{{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}}; // all 4

            for (int[] n : neighbors) {
                if (n[0] >= 0 && n[0] < grid.length &&
                        n[1] >= 0 && n[1] < grid[n[0]].length &&
                        grid[n[0]][n[1]] != Cell.HIT &&
                        grid[n[0]][n[1]] != Cell.MISS) {
                    candidates.add(n);
                }
            }
        }

        // Fallback to heat map if no candidates found
        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    /**
     * Orchestrates the computer's move selection by switching between
     * Target mode (partially hit ship exists) and Hunt mode (no active hits).
     * Target mode focuses attacks around existing hits until the ship is sunk.
     * Hunt mode uses the heat map to find new ships.
     *
     * @param grid           the human's home grid
     * @param remainingShips list of unsunk player ships
     * @return int[]{row, col} of the chosen cell
     */
    private int[] pickComputerMove(Cell[][] grid, List<Ship> remainingShips) {
        if (isTargetMode(grid, remainingShips)) {
            LOG.debug("Computer in TARGET mode");
            int[] targetCell = pickTargetModeCell(grid);
            if (targetCell != null) {
                return targetCell;
            }
            // Fallback to heat map if target mode yields no candidates
            LOG.debug("Target mode found no candidates, falling back to heat map");
        } else {
            LOG.debug("Computer in HUNT mode");
        }
        return pickHeatMapCell(grid, remainingShips);
    }

    /**
     * Selects the computer's next attack cell using a heat map algorithm.
     * Scores every unattacked cell based on how many remaining ships could
     * fit there horizontally or vertically without overlapping a MISS or
     * going out of bounds. Returns the cell with the highest score.
     * O(n^2) per remaining ship: loops all ships x all cells.
     *
     * @param grid      the human's home grid (current state)
     * @param homeShips the computer's list of remaining (unsunk) player ships
     * @return int[]{row, col} of the highest heat value cell
     */
    private int[] pickHeatMapCell(Cell[][] grid, List<Ship> homeShips) {
        int[][] heatMap = new int[grid.length][grid[0].length];

        // Build heat map — score each cell based on how many ships could fit there
        for (Ship ship : homeShips) {
            int shipLength = ship.size();
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[r].length; c++) {
                    if (grid[r][c] == Cell.HIT || grid[r][c] == Cell.MISS) continue;


                    // Check horizontal fit
                    if (c + shipLength <= grid[r].length) {
                        boolean canFit = true;
                        for (int i = 0; i < shipLength; i++) {
                            if (grid[r][c + i] == Cell.MISS || grid[r][c + i] == Cell.HIT) { canFit = false; break; }
                        }
                        if (canFit) {
                            for (int i = 0; i < shipLength; i++) heatMap[r][c + i]++;
                        }
                    }

                    // Check vertical fit
                    if (r + shipLength <= grid.length) {
                        boolean canFit = true;
                        for (int i = 0; i < shipLength; i++) {
                            if (grid[r + i][c] == Cell.MISS || grid[r + i][c] == Cell.HIT) { canFit = false; break; }
                        }
                        if (canFit) {
                            for (int i = 0; i < shipLength; i++) heatMap[r + i][c]++;
                        }
                    }
                }
            }
        }

        // Select highest heat cell with random tie-breaking
        int bestHeat = -1;
        List<int[]> bestCells = new ArrayList<>();

        for (int r = 0; r < heatMap.length; r++) {
            for (int c = 0; c < heatMap[r].length; c++) {
                if (heatMap[r][c] > bestHeat) {
                    bestHeat = heatMap[r][c];
                    bestCells.clear();
                    bestCells.add(new int[]{r, c});
                } else if (heatMap[r][c] == bestHeat) {
                    bestCells.add(new int[]{r, c});
                }
            }
        }

// Fallback — if heat map is all zeros, pick any unattacked cell randomly
        if (bestCells.isEmpty()) {
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[r].length; c++) {
                    if (grid[r][c] != Cell.HIT && grid[r][c] != Cell.MISS) {
                        bestCells.add(new int[]{r, c});
                    }
                }
            }
        }

        LOG.debug("Heat map best heat: {}, candidates: {}", bestHeat, bestCells.size());
        return bestCells.get(ThreadLocalRandom.current().nextInt(bestCells.size()));
    }

}
