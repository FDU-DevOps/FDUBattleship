package org.fdu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stateless Spring service that orchestrates all Battleship game logic.
 */
@Service
public class BattleshipService {

    private static final Logger log = LoggerFactory.getLogger(BattleshipService.class);

    private static final int NO_COORD = -1;
    private static final int MIN_INDEX = 0;
    private static final char COLUMN_LABEL_START = 'A';
    private static final int ROW_LABEL_OFFSET = 1;
    private static final String STATUS_NO_GAME = "NO_GAME";

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

    public String[][] startPlacement(BattleshipManager manager) {
        log.debug("startPlacement called");
        manager.initializePlacementPhase();
        return convertGrid(manager.getHumanDTO().homeGrid());
    }

    public AttackResponseDTO placeShip(PlaceShipRequestDTO request, BattleshipManager manager) {
        if (manager == null) {
            log.warn("placeShip called without active session");
            return errorResponse("Start a game first");
        }

        log.debug("placeShip request: row={}, col={}, length={}, horizontal={}",
                request.row(), request.col(), request.shipLength(), request.horizontal());

        int placed = manager.getHumanDTO().homeShips().size();
        int required = BattleshipManager.FLEET_LENGTHS[placed];

        if (request.shipLength() != required) {
            throw new IllegalArgumentException("Ship length does not match fleet order");
        }

        boolean success = manager.placePlayerShip(
                request.row(), request.col(),
                request.shipLength(), request.horizontal()
        );

        String[][] updatedGrid = convertGrid(manager.getHumanDTO().homeGrid());
        boolean allPlaced = manager.isPlacementComplete();
        String status = allPlaced ? GameStatus.IN_PROGRESS.name() : GameStatus.PLACEMENT.name();
        String message = success ? "Ship placed" : "Invalid placement";

        if (success) {
            log.debug("Ship placed successfully");
        } else {
            log.debug("Invalid ship placement");
        }
        if (allPlaced) {
            log.info("All player ships placed. Game ready.");
        }

        return new AttackResponseDTO(
                null, updatedGrid, 0, status, message,
                NO_COORD, NO_COORD, "", !success, null, null
        );
    }

    public AttackResponseDTO processAttack(AttackRequestDTO request, BattleshipManager manager) {
        if (manager == null) {
            log.warn("processAttack called without active session");
            return errorResponse("Start a game first");
        }

        int row = request.row();
        int col = request.column();
        PlayerDTO human = manager.getHumanDTO();

        log.debug("processAttack request: row={}, col={}", row, col);

        if (row < MIN_INDEX || row >= BattleshipManager.getBoardSize() || col < MIN_INDEX || col >= BattleshipManager.getBoardSize()) {
            log.debug("Rejected attack: invalid coordinates row={}, col={}", row, col);
            return new AttackResponseDTO(
                    null, null,
                    human.guessesLeft(), human.gameStatus().name(),
                    "Invalid coordinates",
                    NO_COORD, NO_COORD, "", true, null, null
            );
        }

        Cell[][] trackingGrid = human.grid();
        if (trackingGrid[row][col] == Cell.HIT || trackingGrid[row][col] == Cell.MISS) {
            log.debug("Rejected attack: cell already attacked row={}, col={}", row, col);
            return new AttackResponseDTO(
                    convertGrid(trackingGrid),
                    convertGrid(human.homeGrid()),
                    human.guessesLeft(),
                    human.gameStatus().name(),
                    "Cell already attacked",
                    NO_COORD, NO_COORD, "", true, null, null
            );
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
                false,
                shipToCoords(turn.sunkShip()),
                shipToCoords(turn.homeSunkShip())
        );
    }

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

    public String[][] convertGrid(Cell[][] grid) {
        String[][] result = new String[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++)
                result[i][j] = grid[i][j].name().toLowerCase();
        return result;
    }

    private int[][] shipToCoords(Ship ship) {
        if (ship == null) return null;
        return ship.cells().stream()
                .map(cell -> new int[]{cell[0], cell[1]})
                .toArray(int[][]::new);
    }

    private AttackResponseDTO errorResponse(String message) {
        return new AttackResponseDTO(
                null, null, 0, STATUS_NO_GAME, message,
                NO_COORD, NO_COORD, "", true, null, null
        );
    }
}