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
    private static final int MAX_GUESSES = 100;

    // Reassigned each turn with the updated DTO returned by AttackProcessor
    private PlayerDTO humanDTO;
    private PlayerDTO computerDTO;
    // Stateless, shared across all turns
    private BattleBoard battleBoard;
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
     * Constructs and initializes all game components for a new session.
     * <p>
     * Creates a stateless BattleBoard renderer and AttackProcessor. Builds the
     * computer's ship grid and the human's home grid with ships placed randomly.
     * Both sides use the same fleet: ship lengths {5, 4, 3, 3, 2}.
     * Ship objects are collected during placement so AttackProcessor can
     * detect sunk ships after each hit.
     * </p>
     */
    public void initializeGame() {
        battleBoard   = new BattleBoard();
        attackProcessor = new AttackProcessor();

        int[] shipLengths = {5, 4, 3, 3, 2};

        // --- Computer's ship grid ---
        Cell[][] shipGrid = blankGrid();
        List<Ship> computerShips = new ArrayList<>();
        for (int len : shipLengths) computerShips.add(placeShip(shipGrid, len));
        computerDTO = new PlayerDTO(shipGrid, null, 0, GameStatus.IN_PROGRESS, computerShips, null);

        // --- Human's home grid (ships shown to the player, targeted by the computer) ---
        Cell[][] homeGrid = blankGrid();
        List<Ship> homeShips = new ArrayList<>();
        for (int len : shipLengths) homeShips.add(placeShip(homeGrid, len));

        // --- Human's tracking grid (blank, updated as player attacks) ---
        Cell[][] trackingGrid = blankGrid();

        humanDTO = new PlayerDTO(trackingGrid, homeGrid, MAX_GUESSES,
                GameStatus.IN_PROGRESS, new ArrayList<>(), homeShips);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Cell[][] blankGrid() {
        Cell[][] grid = new Cell[SIZE][SIZE];
        for (Cell[] row : grid) Arrays.fill(row, Cell.WATER);
        return grid;
    }

    /**
     * Places a ship of the given length at a random valid position on the grid
     * and returns a Ship record describing the cells it occupies.
     *
     * @param grid      the grid to place on
     * @param shipLength number of cells the ship occupies
     * @return Ship record with coordinates of every placed cell
     *
     * ToDo: throw an exception if no valid position can be found after N attempts
     */
    private Ship placeShip(Cell[][] grid, int shipLength) {
        while (true) {
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

    public BattleBoard     getBattleBoard()     { return battleBoard; }
    public AttackProcessor getAttackProcessor() { return attackProcessor; }

    public static int getBoardSize()  { return SIZE; }
    public static int getMaxGuesses() { return MAX_GUESSES; }
}