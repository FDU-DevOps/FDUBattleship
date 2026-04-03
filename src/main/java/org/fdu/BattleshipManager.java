package org.fdu;
import java.util.Random;
import java.util.Scanner;

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

    private static final int SIZE = 10;
    private static final int MAX_GUESSES = 10;

    private PlayerDTO humanDTO;
    private PlayerDTO computerDTO;
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

        Cell[][] shipGrid = new Cell[SIZE][SIZE];
        for (Cell[] row : shipGrid) java.util.Arrays.fill(row, Cell.WATER);
        Random rand = new Random();
        shipRow = rand.nextInt(SIZE);
        shipCol = rand.nextInt(SIZE);
        shipGrid[shipRow][shipCol] = Cell.SHIP;
        computerDTO = new PlayerDTO(shipGrid, 0, GameStatus.IN_PROGRESS);

        Cell[][] trackingGrid = new Cell[SIZE][SIZE];
        for (Cell[] row : trackingGrid) java.util.Arrays.fill(row, Cell.WATER);
        humanDTO = new PlayerDTO(trackingGrid, MAX_GUESSES, GameStatus.IN_PROGRESS);
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

    public void startGame() {
        Scanner scanner = new Scanner(System.in);

        while (humanDTO.gameStatus() == GameStatus.IN_PROGRESS) {
            battleBoard.displayBoard(humanDTO.grid());
            System.out.println("Guesses remaining: " + humanDTO.guessesLeft());
            System.out.print("Enter coordinate (e.g. A1): ");

            String input = scanner.nextLine().trim().toUpperCase();
            int col = input.charAt(0) - 'A';
            int row = Integer.parseInt(input.substring(1)) - 1;

            PlayerDTO[] result = attackProcessor.processAttack(row, col, humanDTO, computerDTO);
            humanDTO = result[0];
            computerDTO = result[1];

            if (humanDTO.grid()[row][col] == Cell.HIT) System.out.println("Hit!");
            if (humanDTO.grid()[row][col] == Cell.MISS) System.out.println("Miss!");
            if (humanDTO.gameStatus() == GameStatus.WIN) System.out.println("You sunk my battleship! You win!");
            if (humanDTO.gameStatus() == GameStatus.LOSS) System.out.println("No guesses remaining. You lose!");
        }
    }
}
