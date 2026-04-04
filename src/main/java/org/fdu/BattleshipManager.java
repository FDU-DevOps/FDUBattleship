package org.fdu;
import java.util.Random;

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
    private static final int MAX_GUESSES = 10;

    // Reassigned each turn with the updated DTO returned by AttackProcessor, not mutated in place
    private PlayerDTO humanDTO;
    private PlayerDTO computerDTO;
    // Stateless, shared across all turns, no need to recreate each iteration
    private final BattleBoard battleBoard;
    private final AttackProcessor attackProcessor;


    /**
     * Constructs a new BattleShipManager and initializes all game components.
     * <p>
     * Creates a stateless BattleBoard renderer and AttackProcessor. Builds the
     * computer's ship grid with a single 1x1 ship placed at a random location
     * using java.util.Random. Builds the human player's blank tracking grid
     * with full guess count and IN_PROGRESS status.
     * </p>
     */

    public BattleshipManager() {
        final int shipRow;
        final int shipCol;
        battleBoard     = new BattleBoard();
        attackProcessor = new AttackProcessor();

        // Fill every cell with WATER first, then overwrite one cell with the ship
        Cell[][] shipGrid = new Cell[SIZE][SIZE];
        for (Cell[] row : shipGrid) java.util.Arrays.fill(row, Cell.WATER);

        // Pick a random cell inside the 10x10 bounds for the ship placement
        Random rand = new Random();
        shipRow = rand.nextInt(SIZE);
        shipCol = rand.nextInt(SIZE);
        shipGrid[shipRow][shipCol] = Cell.SHIP;
        computerDTO = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        // Human starts with a fully blank tracking grid and the maximum allowed guesses
        Cell[][] trackingGrid = new Cell[SIZE][SIZE];
        for (Cell[] row : trackingGrid) java.util.Arrays.fill(row, Cell.WATER);
        humanDTO = new PlayerDTO(trackingGrid, MAX_GUESSES, GameStatus.IN_PROGRESS);

        // Debug line, remove before shipping to players
        System.out.println("Ship is at: " + (char)('A' + shipCol) + (shipRow + 1));
    }


    /**
     * Starts and runs the main game loop until the player wins or loses.
     * <p>
     * On each iteration: renders the current tracking board and guess count,
     * reads a coordinate from the player via console input, converts the input
     * to row and col indices, delegates to AttackProcessor, unpacks the
     * returned PlayerDTO array to update both DTOs, prints the result message,
     * then checks game status. Exits the loop and prints the final message on
     * WIN or LOSS.
     * </p>
     */

    //Getters and Setters

    public PlayerDTO getHumanDTO() { return humanDTO; }
    public void setHumanDTO(PlayerDTO humanDTO) { this.humanDTO = humanDTO; }

    public PlayerDTO getComputerDTO() { return computerDTO; }
    public void setComputerDTO(PlayerDTO computerDTO) { this.computerDTO = computerDTO; }

    public BattleBoard getBattleBoard() { return battleBoard; }
    public AttackProcessor getAttackProcessor() { return attackProcessor; }
}