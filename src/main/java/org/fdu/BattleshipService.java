package org.fdu;

import org.springframework.stereotype.Service;

/**
 * Stateless Spring service that orchestrates all Battleship game logic.
 * <p>
 * This is the single point of coordination between {@link BoardController}
 * and the domain layer ({@link BattleshipManager}, {@link AttackProcessor}).
 * The controller's only jobs are reading the session and returning HTTP
 * responses. Everything else lives here.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validating attack coordinates and duplicate-attack checks</li>
 *   <li>Delegating attack resolution to {@link AttackProcessor}</li>
 *   <li>Building human-readable messages for both the player's and
 *       the computer's moves</li>
 *   <li>Converting internal {@link Cell} grids to the lowercase strings
 *       the frontend expects</li>
 *   <li>Converting {@link Ship} cell lists to {@code int[][]} for
 *       sunk-ship highlighting</li>
 *   <li>Delegating placement calls to {@link BattleshipManager}</li>
 * </ul>
 * </p>
 * <p>
 * This class is a Spring singleton and holds no mutable state of its own.
 * All game state lives in the session-scoped {@link BattleshipManager}.
 * </p>
 */
@Service
public class BattleshipService {

    private final AttackProcessor attackProcessor;

    public BattleshipService() {
        this.attackProcessor = new AttackProcessor();
    }

    // -------------------------------------------------------------------------
    // Game lifecycle
    // -------------------------------------------------------------------------

    /**
     * Initializes a full random game (no manual placement) and returns
     * the number of guesses the player starts with.
     *
     * @param manager the session-scoped game manager to initialize
     * @return starting guess count
     */
    public int startGame(BattleshipManager manager) {
        manager.initializeGame();
        return manager.getHumanDTO().guessesLeft();
    }

    /**
     * Initializes the placement phase and returns the blank home grid
     * as lowercase strings for the frontend to render.
     *
     * @param manager the session-scoped game manager to initialize
     * @return blank player home grid as lowercase strings
     */
    public String[][] startPlacement(BattleshipManager manager) {
        manager.initializePlacementPhase();
        return convertGrid(manager.getHumanDTO().homeGrid());
    }

    // -------------------------------------------------------------------------
    // Placement
    // -------------------------------------------------------------------------

    /**
     * Attempts to place a single player ship and returns the updated response.
     * <p>
     * Returns an error response if the manager is null (no active session).
     * Returns a failed-placement response if the coordinates or length are invalid.
     * </p>
     *
     * @param request the placement parameters from the frontend
     * @param manager the session-scoped game manager, or {@code null} if no session exists
     * @return {@link AttackResponseDTO} with the updated home grid and placement status
     */
    public AttackResponseDTO placeShip(PlaceShipRequestDTO request, BattleshipManager manager) {
        if (manager == null) {
            return errorResponse("Start a game first");
        }

        boolean success = manager.placePlayerShip(
                request.row(), request.col(),
                request.shipLength(), request.horizontal()
        );

        String[][] updatedGrid = convertGrid(manager.getHumanDTO().homeGrid());
        boolean allPlaced = manager.isPlacementComplete();
        String status = allPlaced ? GameStatus.IN_PROGRESS.name() : GameStatus.PLACEMENT.name();
        String message = success ? "Ship placed" : "Invalid placement";

        return new AttackResponseDTO(
                null, updatedGrid, 0, status, message,
                -1, -1, "", !success, null, null
        );
    }

    // -------------------------------------------------------------------------
    // Attack
    // -------------------------------------------------------------------------

    /**
     * Validates the player's attack, processes the full turn, updates the
     * manager, and returns a complete response for the frontend.
     * <p>
     * Validation order:
     * <ol>
     *   <li>No active session</li>
     *   <li>Coordinates out of bounds</li>
     *   <li>Cell already attacked</li>
     * </ol>
     * On success, delegates to {@link AttackProcessor#processAttack}, updates
     * both DTOs on the manager, builds both player and computer messages,
     * and assembles the response.
     * </p>
     *
     * @param request the attack coordinates from the frontend
     * @param manager the session-scoped game manager, or {@code null} if no session exists
     * @return {@link AttackResponseDTO} with both updated grids, messages, and game status
     */
    public AttackResponseDTO processAttack(AttackRequestDTO request, BattleshipManager manager) {
        if (manager == null) {
            return errorResponse("Start a game first");
        }

        int row = request.row();
        int col = request.column();

        PlayerDTO human    = manager.getHumanDTO();
        PlayerDTO computer = manager.getComputerDTO();

        // Reject out-of-bounds
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return new AttackResponseDTO(
                    null, null,
                    human.guessesLeft(), human.gameStatus().name(),
                    "Invalid coordinates",
                    -1, -1, "", true, null, null
            );
        }

        // Reject already-attacked cell
        Cell[][] trackingGrid = human.grid();
        if (trackingGrid[row][col] == Cell.HIT || trackingGrid[row][col] == Cell.MISS) {
            return new AttackResponseDTO(
                    convertGrid(trackingGrid),
                    convertGrid(human.homeGrid()),
                    human.guessesLeft(), human.gameStatus().name(),
                    "Cell already attacked",
                    -1, -1, "", true, null, null
            );
        }

        // Process the full turn
        TurnResultDTO turn = attackProcessor.processAttack(row, col, human, computer);

        manager.setHumanDTO(turn.updatedHuman());
        manager.setComputerDTO(turn.updatedComputer());

        String playerMessage  = buildPlayerMessage(turn, row, col);
        String computerMessage = buildComputerMessage(turn);

        return new AttackResponseDTO(
                convertGrid(turn.updatedHuman().grid()),
                convertGrid(turn.updatedHuman().homeGrid()),
                turn.updatedHuman().guessesLeft(),
                turn.updatedHuman().gameStatus().name(),
                playerMessage,
                turn.computerRow(),
                turn.computerCol(),
                computerMessage,
                false,
                shipToCoords(turn.sunkShip()),
                shipToCoords(turn.homeSunkShip())
        );
    }

    // -------------------------------------------------------------------------
    // Message builders
    // -------------------------------------------------------------------------

    /**
     * Builds the human-readable message describing the player's attack result.
     */
    private String buildPlayerMessage(TurnResultDTO turn, int row, int col) {
        if (turn.updatedHuman().gameStatus() == GameStatus.WIN) {
            return "You win!";
        }
        if (turn.sunkShip() != null) {
            return turn.sunkShip().size() + "-cell ship sunk!";
        }
        if (turn.updatedHuman().grid()[row][col] == Cell.HIT) {
            return "Hit!";
        }
        return "Miss!";
    }

    /**
     * Builds the human-readable message describing the computer's attack result.
     * Returns an empty string if the computer did not fire (player won first).
     */
    private String buildComputerMessage(TurnResultDTO turn) {
        int compRow = turn.computerRow();
        int compCol = turn.computerCol();

        if (compRow < 0) {
            return "";
        }

        String coord = (char) ('A' + compCol) + String.valueOf(compRow + 1);
        boolean compHit = turn.updatedHuman().homeGrid()[compRow][compCol] == Cell.HIT;

        if (turn.updatedHuman().gameStatus() == GameStatus.LOSS) {
            return "Computer sunk your last ship. You lose!";
        }
        if (turn.homeSunkShip() != null) {
            return "Computer sunk your " + turn.homeSunkShip().size() + "-cell ship at " + coord + "!";
        }
        if (compHit) {
            return "Computer hit your ship at " + coord + "!";
        }
        return "Computer missed at " + coord + ".";
    }

    // -------------------------------------------------------------------------
    // Grid / ship conversion utilities
    // -------------------------------------------------------------------------

    /**
     * Converts a 2D {@link Cell} array to lowercase strings for the frontend.
     * Values: "water", "ship", "hit", "miss".
     */
    public String[][] convertGrid(Cell[][] grid) {
        String[][] result = new String[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++)
                result[i][j] = grid[i][j].name().toLowerCase();
        return result;
    }

    /**
     * Converts a {@link Ship}'s cell list to a plain {@code int[][]},
     * or {@code null} if the ship is {@code null}.
     */
    private int[][] shipToCoords(Ship ship) {
        if (ship == null) return null;
        return ship.cells().stream()
                .map(cell -> new int[]{ cell[0], cell[1] })
                .toArray(int[][]::new);
    }

    /**
     * Builds a generic error response with no grid state.
     */
    private AttackResponseDTO errorResponse(String message) {
        return new AttackResponseDTO(
                null, null, 0, "NO_GAME", message,
                -1, -1, "", true, null, null
        );
    }
}