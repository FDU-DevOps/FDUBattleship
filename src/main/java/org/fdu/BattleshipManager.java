package org.fdu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service class responsible for starting the full Battleship game loop.
 * <p>
 * Owns humanDTO, computerDTO, BattleBoard, and AttackProcessor. Initializes
 * all game state at startup, runs the guess loop, delegates attack resolution
 * to AttackProcessor, unpacks the returned PlayerDTO array to update both
 * DTOs, and prints result messages to the console after each turn.
 * App calls startGame() to begin play.
 * </p>
 */
public class BattleshipManager {

    // Fixed board dimension, both axes are 10x10 throughout the entire game
    private static final int SIZE = 10;
    // Total number of attacks the player is allowed before the game is lost
    private static final int MAX_GUESSES = 30;

    // Reassigned each turn with the updated DTO returned by AttackProcessor
    private PlayerDTO humanDTO;
    private PlayerDTO computerDTO;

    private AttackProcessor attackProcessor;


    /**
     * Constructs a new BattleShipManager and initializes all game components.
     * <p>
     * Creates a stateless BattleBoard renderer and AttackProcessor. Builds the
     * computer's ship grid with a single 1x1 ship placed at a random location
     * using java.util.Random. Builds the human player's blank tracking grid
     * with full guess count and IN_PROGRESS status.
     * </p>
     */

    public BattleshipManager() { }

    /**
     * Constructs and initializes all game components for a new session. This skips manual placement
     * <p>
     * Creates a stateless BattleBoard renderer and AttackProcessor. Builds the
     * computer's ship grid (in initializePlacementPhase()) and the human's home grid with random placements.
     * Both sides use the same fleet: ship lengths {5, 4, 3, 3, 2}.
     * Ship objects are collected during placement so AttackProcessor can
     * detect sunk ships after each hit.
     * </p>
     */
    public void initializeGame() {

        int[] shipLengths = {5, 4, 3, 3, 2};

        //Creates the Computer Ship Grid and Tracking Grid
        initializePlacementPhase();

        // --- Human's home grid (ships shown to the player, targeted by the computer) ---
        Cell[][] homeGrid = humanDTO.homeGrid(); //empty grid from the placement phase that will now have ships
        List<Ship> homeShips = placeAllShips(homeGrid, shipLengths);

        //humanDTO.grid() is still empty, homeGrid is now filled with the random ships
        humanDTO = new PlayerDTO(humanDTO.grid(), homeGrid, MAX_GUESSES,
                GameStatus.IN_PROGRESS, new ArrayList<>(), homeShips);
    }
    /**
     * Phase 1 of game initialization — sets up computer ships and blank player grids so manual placement can begin.
     * <p>
     * Called at session start before the player places their ships.
     * The game is not yet attackable after this call.
     * </p>
     */
    public void initializePlacementPhase() {
        attackProcessor = new AttackProcessor();
        int[] shipLengths = {5, 4, 3, 3, 2}; //ToDo move this into a constant

        // --- Computer's ship grid ---
        Cell[][] compGrid = blankGrid(null);
        List<Ship> compShips = placeAllShips(compGrid, shipLengths);
        computerDTO = new PlayerDTO(compGrid, null, 0, GameStatus.IN_PROGRESS, compShips, null);

        // --- Human's home grid (blank, awaiting player placement) ---
        Cell[][] homeGrid = blankGrid(null);

        // --- Human's tracking grid (blank) ---
        Cell[][] trackingGrid = blankGrid(null);

        humanDTO = new PlayerDTO(trackingGrid, homeGrid, MAX_GUESSES,
                GameStatus.PLACEMENT, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Attempts to place a single player ship on the human home grid.
     * Called once per ship during the placement phase.
     *
     * @param row        Start row of the ship (0-9)
     * @param col        Start column of the ship (0-9)
     * @param shipLength Length of the ship being placed
     * @param horizontal true = horizontal, false = vertical
     * @return true if placed successfully, false if out of bounds or overlapping
     */
    public boolean placePlayerShip(int row, int col, int shipLength, boolean horizontal) {
        Cell[][] homeGrid = humanDTO.homeGrid();

        // --- Bounds check ---
        boolean fits = horizontal
                ? (col + shipLength <= SIZE)
                : (row + shipLength <= SIZE);
        if (!fits) return false;

        // --- Overlap check ---
        for (int i = 0; i < shipLength; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (homeGrid[r][c] != Cell.WATER) return false;
        }

        // --- Place ship ---
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < shipLength; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            homeGrid[r][c] = Cell.SHIP;
            cells.add(new int[]{ r, c });
        }

        // Add the new ship to the human's home ship list
        List<Ship> homeShips = new ArrayList<>(humanDTO.homeShips());
        homeShips.add(new Ship(cells));
        humanDTO = new PlayerDTO(humanDTO.grid(), homeGrid, humanDTO.guessesLeft(),
                GameStatus.PLACEMENT, humanDTO.ships(), homeShips);

        return true;
    }

    /**
     * Checks whether the player has finished placing all ships.
     * Counts SHIP cells on the human home grid.
     *
     * @return true if total SHIP cells == 17 (5+4+3+3+2), false otherwise
     */
    public boolean isPlacementComplete() {
        int shipCells = 0;
        for (Cell[] row : humanDTO.homeGrid())
            for (Cell c : row)
                if (c == Cell.SHIP) shipCells++;
        return shipCells == 17;
    }
    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Sets all cells in a grid to water.
     * If the grid is null, it initializes a new one.
     * @param grid the grid that is set to water
     * @return blank grid (all cells water)
     */
    private Cell[][] blankGrid(Cell[][] grid) {
        if (grid == null) {
            grid = new Cell[SIZE][SIZE];
        }
        for (Cell[] row : grid) Arrays.fill(row, Cell.WATER);
        return grid;
    }

    /**
     * Places all ships but will retry on failure by wiping the board
     *
     * <p>
     * Attempts to place all ships in shipLengths from longest to shortest.
     * If the placement algorithm fails, an exception is thrown.
     * placeShip can get into impossible situations, and this method handles that
     * This method catches the exception, wipes the grid with blankGrid(Cell[][]) and then returns.
     * </p>
     *
     * @param grid grid on which ships will be placed
     * @param shipLengths contains the number of cells that each ship will occupy
     * @return List of properly placed Ship objects
     */
    private List<Ship> placeAllShips(Cell[][] grid, int[] shipLengths) {
        while (true) {
            try {
                blankGrid(grid);
                List<Ship> allShips = new ArrayList<>();
                for (int len : shipLengths) allShips.add(placeShip(grid, len));
                return allShips; //(Returns once all ships placed successfully)
            } catch (RuntimeException e) {
                System.out.println("Impossible to place all ships, trying again...");
            }
        }
    }

    /**
     * Places a ship of the given length at a random valid position on the grid
     * and returns a Ship record describing the cells it occupies.
     *
     * @param grid      the grid to place on
     * @param shipLength number of cells the ship occupies
     * @return Ship record with coordinates of every placed cell
     *
     *
     */
    private Ship placeShip(Cell[][] grid, int shipLength) {
        int attemptCounter = 0;
        final int ATTEMPTS = 1000;
        while (attemptCounter < ATTEMPTS) {
            attemptCounter++;
            boolean horizontal = ThreadLocalRandom.current().nextBoolean();
            int row = ThreadLocalRandom.current().nextInt(SIZE);
            int col = ThreadLocalRandom.current().nextInt(SIZE);

            boolean fitsBounds = horizontal
                    ? (col + shipLength <= SIZE)
                    : (row + shipLength <= SIZE);

            if (!fitsBounds) continue;

            boolean canPlace = true;
            for (int i = 0; i < shipLength; i++) {
                int r = horizontal ? row : row + i;
                int c = horizontal ? col + i : col;
                if (grid[r][c] != Cell.WATER) { canPlace = false; break; }
            }

            if (!canPlace) continue;

            List<int[]> cells = new ArrayList<>();
            for (int i = 0; i < shipLength; i++) {
                int r = horizontal ? row : row + i;
                int c = horizontal ? col + i : col;
                grid[r][c] = Cell.SHIP;
                cells.add(new int[]{ r, c });
                System.out.println("Placing ship cell at: " + (char)('A' + c) + (r + 1));
            }
            System.out.println("--- Ship of length " + shipLength + " placed ---");
            return new Ship(cells);
        }
        // tried ATTEMPTS times and failed
        throw new RuntimeException("Could not place ship of length " + shipLength);
    }

    // -------------------------------------------------------------------------
    // Public test-support methods
    // -------------------------------------------------------------------------

    /** Fills every cell of the DTO's primary grid with WATER. */
    void clearGrid(PlayerDTO dto) {
        for (Cell[] row : dto.grid()) Arrays.fill(row, Cell.WATER);
        System.out.println("--- board cleared of all ships ---");
    }

    /**
     * Places a ship of the given length at a fixed position.
     * Duplicates some logic from the random placement, kept for test support.
     */
    void placeShip(PlayerDTO dto, int shipLength, boolean isHorizontal, int startCol, int startRow) {
        for (int i = 0; i < shipLength; i++) {
            int r = isHorizontal ? startRow : startRow + i;
            int c = isHorizontal ? startCol + i : startCol;
            dto.grid()[r][c] = Cell.SHIP;
            System.out.println("Placing ship cell at: " + (char)('A' + c) + (r + 1));
        }
        System.out.println("--- Ship of length " + shipLength + " placed ---");
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public PlayerDTO getHumanDTO()    { return humanDTO; }
    public void setHumanDTO(PlayerDTO humanDTO) { this.humanDTO = humanDTO; }

    public PlayerDTO getComputerDTO() { return computerDTO; }
    public void setComputerDTO(PlayerDTO computerDTO) { this.computerDTO = computerDTO; }

    public AttackProcessor getAttackProcessor() { return attackProcessor; }

    public static int getBoardSize()  { return SIZE; }
    public static int getMaxGuesses() { return MAX_GUESSES; }
}