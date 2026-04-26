package org.fdu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Stateless Spring service that orchestrates Battleship application use-cases.
 * <p>
 * Acts as the API-facing domain layer for game actions such as starting game
 * flow, handling ship placement, and resolving attack turns. This service
 * validates request intent and game-phase rules, delegates turn resolution to
 * {@link AttackProcessor}, transforms domain state into DTOs, and raises
 * exceptions for invalid requests so HTTP status mapping can be handled
 * centrally by global exception handling.
 * </p>
 * <p>
 * This class intentionally contains no mutable instance state. Session-bound
 * game state is owned by {@link BattleshipManager} and passed into each method,
 * making this service safe for singleton Spring usage and easy to test.
 * </p>
 */
@Service
public class BattleshipService {

    private static final Logger log = LoggerFactory.getLogger(BattleshipService.class);

    private static final int NO_COORD = -1;
    private static final int MIN_INDEX = 0;
    private static final char COLUMN_LABEL_START = 'A';
    private static final int ROW_LABEL_OFFSET = 1;

    /**
     * Starts or resumes game play for the current session manager.
     * <p>
     * If no valid placed fleet exists, initializes a full random game state.
     * If manual placement is already complete, transitions the human status
     * to {@link GameStatus#IN_PROGRESS} and resets guesses for active play.
     * </p>
     *
     * @param manager session-scoped game manager
     * @return current number of guesses available to the player
     */
    public int startGame(BattleshipManager manager) {
        log.debug("startGame called");
        if (manager.getHumanDTO() == null || !manager.isPlacementComplete()) {
            log.debug("Initializing random game");
            manager.initializeGame();
        } else {
            log.debug("Using manually placed ships, switching status to IN_PROGRESS");
            PlayerDTO human = manager.getHumanDTO();
            manager.setHumanDTO(new PlayerDTO(
                    human.grid(), human.homeGrid(), BattleshipManager.getMaxGuesses(),
                    GameStatus.IN_PROGRESS, human.ships(), human.homeShips()
            ));
        }
        log.info("Game started with {} guesses", manager.getHumanDTO().guessesLeft());
        return manager.getHumanDTO().guessesLeft();
    }

    /**
     * Starts the ship-placement phase for a new session game.
     * <p>
     * Re-initializes placement state and returns the player's home grid so
     * the frontend can render an empty placement board.
     * </p>
     *
     * @param manager session-scoped game manager
     * @return lowercase string representation of the player's home grid
     */
    public String[][] startPlacement(BattleshipManager manager) {
        log.debug("startPlacement called");
        manager.initializePlacementPhase();
        return convertGrid(manager.getHumanDTO().homeGrid());
    }

    /**
     * Places one player ship during placement phase.
     * <p>
     * Enforces phase validity and fleet-order constraints, then delegates
     * coordinate placement to {@link BattleshipManager#placePlayerShip(int, int, int, boolean)}.
     * Invalid session/resource, malformed intent, or illegal game actions are
     * surfaced as exceptions for centralized HTTP mapping.
     * </p>
     *
     * @param request ship placement request payload
     * @param manager session-scoped game manager
     * @return successful placement response with updated home grid and phase
     * @throws NoSuchElementException if no active game exists in session
     * @throws IllegalStateException  if placement is illegal for current game state
     */
    public AttackResponseDTO placeShip(PlaceShipRequestDTO request, BattleshipManager manager) {
        if (manager == null) {
            log.warn("placeShip called without active session");
            throw new NoSuchElementException("No active game session found");
        }

        log.debug("placeShip request: row={}, col={}, length={}, horizontal={}",
                request.row(), request.col(), request.shipLength(), request.horizontal());

        if (manager.getHumanDTO().gameStatus() != GameStatus.PLACEMENT) {
            throw new IllegalStateException("Ships can only be placed during placement phase");
        }

        assert manager.getHumanDTO().homeShips() != null;
        int placed = manager.getHumanDTO().homeShips().size();
        int required = BattleshipManager.FLEET_LENGTHS[placed];

        if (request.shipLength() != required) {
            throw new IllegalStateException("Ship length does not match required fleet order");
        }

        boolean success = manager.placePlayerShip(
                request.row(), request.col(),
                request.shipLength(), request.horizontal()
        );

        if (!success) {
            throw new IllegalStateException("Cannot place ship at requested position");
        }

        String[][] updatedGrid = convertGrid(manager.getHumanDTO().homeGrid());
        boolean allPlaced = manager.isPlacementComplete();
        String status = allPlaced ? GameStatus.IN_PROGRESS.name() : GameStatus.PLACEMENT.name();

        if (allPlaced) {
            log.info("All player ships placed. Game ready.");
        }

        return new AttackResponseDTO(
                null, updatedGrid, 0, status, "Ship placed",
                NO_COORD, NO_COORD, "", null, null
        );
    }

    /**
     * Processes one player attack turn.
     * <p>
     * Validates request coordinates and game-phase/state constraints, rejects
     * illegal repeat attacks, delegates turn resolution to {@link AttackProcessor},
     * and maps resulting domain state into a single response payload for the UI.
     * </p>
     *
     * @param request attack request payload containing row/column target
     * @param manager session-scoped game manager
     * @return full turn response containing updated boards, status, and messages
     * @throws NoSuchElementException   if no active game exists in session
     * @throws IllegalArgumentException if coordinates are out of bounds
     * @throws IllegalStateException    if action is invalid for current game state
     */
    public AttackResponseDTO processAttack(AttackRequestDTO request, BattleshipManager manager) {
        if (manager == null) {
            log.warn("processAttack called without active session");
            throw new NoSuchElementException("No active game session found");
        }

        int row = request.row();
        int col = request.column();
        PlayerDTO human = manager.getHumanDTO();

        log.debug("processAttack request: row={}, col={}", row, col);

        if (row < MIN_INDEX || row >= BattleshipManager.getBoardSize()
                || col < MIN_INDEX || col >= BattleshipManager.getBoardSize()) {
            throw new IllegalArgumentException("Coordinates must be between 0 and 9");
        }

        if (human.gameStatus() == GameStatus.PLACEMENT) {
            throw new IllegalStateException("Game has not started yet");
        }

        if (human.gameStatus() == GameStatus.WIN || human.gameStatus() == GameStatus.LOSS) {
            throw new IllegalStateException("Game already finished");
        }

        Cell[][] trackingGrid = human.grid();
        if (trackingGrid[row][col] == Cell.HIT || trackingGrid[row][col] == Cell.MISS) {
            throw new IllegalStateException("Cell already attacked");
        }

        AttackProcessor processor = new AttackProcessor();
        TurnResultDTO turn = manager.performTurn(row, col, processor);

        String playerMessage = buildPlayerMessage(turn, row, col);
        String computerMessage = buildComputerMessage(turn);

        log.debug("Turn completed. guessesLeft={}, status={}",
                turn.updatedHuman().guessesLeft(),
                turn.updatedHuman().gameStatus());

        if (turn.updatedHuman().gameStatus() == GameStatus.WIN) {
            log.info("Game ended: player WIN");
        } else if (turn.updatedHuman().gameStatus() == GameStatus.LOSS) {
            log.info("Game ended: player LOSS");
        }

        return new AttackResponseDTO(
                convertGrid(turn.updatedHuman().grid()),
                convertGrid(turn.updatedHuman().homeGrid()),
                turn.updatedHuman().guessesLeft(),
                turn.updatedHuman().gameStatus().name(),
                playerMessage,
                turn.computerRow(),
                turn.computerCol(),
                computerMessage,
                shipToCoords(turn.sunkShip()),
                shipToCoords(turn.homeSunkShip())
        );
    }

    /**
     * Builds a human-readable player attack message for the resolved turn.
     *
     * @param turn resolved turn result
     * @param row  attacked row
     * @param col  attacked column
     * @return player-facing outcome message
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
     * Builds a human-readable computer counter-attack message for the turn.
     *
     * @param turn resolved turn result
     * @return computer action message, or empty string when computer did not fire
     */
    private String buildComputerMessage(TurnResultDTO turn) {
        int compRow = turn.computerRow();
        int compCol = turn.computerCol();

        if (compRow == NO_COORD) {
            return "";
        }

        String coord = (char) (COLUMN_LABEL_START + compCol) + String.valueOf(compRow + ROW_LABEL_OFFSET);
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

    /**
     * Converts an internal {@link Cell} grid to a lowercase string grid
     * for API serialization.
     *
     * @param grid internal game grid
     * @return string grid with lowercase cell values
     */
    public String[][] convertGrid(Cell[][] grid) {
        String[][] result = new String[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++)
                result[i][j] = grid[i][j].name().toLowerCase();
        return result;
    }

    /**
     * Converts a {@link Ship} object into a 2D coordinate array expected by API clients.
     *
     * @param ship sunk ship domain object
     * @return coordinates of ship cells, or {@code null} when no ship is provided
     */
    private int[][] shipToCoords(Ship ship) {
        if (ship == null) return null;
        return Objects.requireNonNull(ship.cells()).stream()
                .map(cell -> new int[]{cell[0], cell[1]})
                .toArray(int[][]::new);
    }
}