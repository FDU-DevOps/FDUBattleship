package org.fdu;

/**
 * Service class responsible for managing the Battleship board.
 * <p>
 * Owns the board state via a BattleBoardDTO and handles
 * initialization of the grid and rendering it to the console.
 * Acts as the central point of interaction for the board, meaning
 * other classes such as BattleShipManager will talk to this
 * class rather than manipulating the grid directly.
 * </p>
 */
public class BattleBoard
{

    private final int SIZE = 10;
    private static final char[] COLUMNS = {'A','B','C','D','E','F','G','H','I','J'};
    /**
     * The current state of the board, stored as a DTO.
     * Initialized in the constructor and updated as the game progresses.
     */
    private final PlayerDTO boardState;

    /**
     * Constructs a new BattleBoard and initializes a blank 10x10 grid.
     * Every cell is set to Cell: WATER on creation.
     */
    public BattleBoard() {
        boardState = initBoard();
    }
    /**
     * Creates a fresh 10x10 grid with every cell set to Cell: WATER.
     * <p>
     * This method is called once during construction. The resulting grid is
     * wrapped in a BattleBoardDTO and stored as the initial board state.
     * </p>
     *
     * @return a BattleBoardDTO containing the blank initialized grid
     */
    private PlayerDTO initBoard() {
        Cell[][] grid = new Cell[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = Cell.WATER;
            }
        }

        return new PlayerDTO(grid);
    }
    /**
     * Prints the current board state to the console.
    * <p>
    * Renders the 10x10 grid with column labels A-J across the top
    * and row numbers 1-10 along the left side. Each Cell: WATER
     * cell is displayed as ~.
    * </p>
    * <p>
    * Expected output format:
    *     A B C D E F G H I J
    *  1  ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
    *  2  ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
     *  ...
    * 10  ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
    * </p>
    */

    public void displayBoard() {

        // Print column headers
        System.out.print("   ");
        for (char c : COLUMNS) {
            System.out.print(" " + c + "  ");
        }
        System.out.println();


        // Print rows
        for (int row = 0; row < SIZE; row++) {

            System.out.printf("%2d ", row + 1);

            for (int col = 0; col < SIZE; col++) {
                System.out.print("[~] ");
            }

            System.out.println();
        }
    }
    /**
     * Returns the current state of the board as a BattleBoardDTO.
     * <p>
     * Used by other classes such as BattleShipManager to read
     * board state without directly accessing BattleBoard internals.
     * </p>
     *
     * @return the current BattleBoardDTO representing the board state
     */

    public PlayerDTO getState() {
        return boardState;
    }
}