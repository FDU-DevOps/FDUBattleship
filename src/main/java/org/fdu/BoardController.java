package org.fdu;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the Battleship HTTP API.
 * <p>
 * Receives client requests for game lifecycle and turn actions, retrieves
 * the session-scoped {@link BattleshipManager}, delegates all business logic
 * to {@link BattleshipService}, and returns typed {@link ResponseEntity}
 * responses for successful operations.
 * </p>
 * <p>
 * Error payload creation is intentionally excluded from this controller.
 * Domain and request failures are propagated as exceptions and translated into
 * RFC-style {@link org.springframework.http.ProblemDetail} responses by
 * global exception handling, keeping endpoint methods thin and focused.
 * </p>
 */
@RestController
@RequestMapping("/api/battleship")
public class BoardController {

    private static final Logger LOG = LoggerFactory.getLogger(BoardController.class);

    private final BattleshipService battleshipService;

    /**
     * Creates a controller with the Battleship application service dependency.
     *
     * @param battleshipService stateless orchestration service
     */
    public BoardController(BattleshipService battleshipService) {
        this.battleshipService = battleshipService;
    }

    /**
     * Starts gameplay for the current session.
     * <p>
     * Creates a manager if absent, delegates startup logic to service,
     * stores the manager back into session, and returns guesses available.
     * </p>
     *
     * @param session HTTP session containing per-user game state
     * @return remaining guesses for active game
     */
    @PostMapping("/start-game")
    public ResponseEntity<Integer> startGame(HttpSession session) {
        LOG.debug("POST /api/battleship/start-game");
        BattleshipManager manager = getOrCreateManager(session);
        int guessesLeft = battleshipService.startGame(manager);
        session.setAttribute("game", manager);
        LOG.debug("Game stored in session. guessesLeft={}", guessesLeft);
        return ResponseEntity.ok(guessesLeft);
    }

    /**
     * Starts placement phase by creating a new manager and resetting session state.
     *
     * @param session HTTP session containing per-user game state
     * @return player's empty placement grid
     */
    @PostMapping("/placement-start")
    public ResponseEntity<String[][]> placementStart(HttpSession session) {
        LOG.debug("POST /api/battleship/placement-start");
        BattleshipManager manager = new BattleshipManager();
        session.setAttribute("game", manager);
        return ResponseEntity.ok(battleshipService.startPlacement(manager));
    }

    /**
     * Places one ship on the player's home board during placement phase.
     *
     * @param request placement request payload
     * @param session HTTP session containing per-user game state
     * @return updated placement response payload
     */
    @PostMapping("/place-ship")
    public ResponseEntity<AttackResponseDTO> placeShip(@RequestBody PlaceShipRequestDTO request, HttpSession session) {
        LOG.debug("POST /api/battleship/place-ship row={}, col={}, len={}, horizontal={}",
                request.row(), request.col(), request.shipLength(), request.horizontal());
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        AttackResponseDTO response = battleshipService.placeShip(request, manager);
        session.setAttribute("game", manager);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes one attack turn and returns updated game state for both boards.
     * Also masks the positions of ships with maskComputerGrid
     *
     * @param request attack request payload
     * @param session HTTP session containing per-user game state
     * @return resolved attack response payload
     */
    @PostMapping("/attack")
    public ResponseEntity<AttackResponseDTO> attack(@RequestBody AttackRequestDTO request, HttpSession session) {
        LOG.debug("POST /api/battleship/attack row={}, col={}", request.row(), request.column());
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        AttackResponseDTO response = battleshipService.processAttack(request, manager);
        session.setAttribute("game", manager);
        String[][] maskedGrid = maskComputerGrid(response.grid());

        AttackResponseDTO safeResponse = new AttackResponseDTO(
                maskedGrid,
                response.homeGrid(),
                response.guessesLeft(),
                response.gameStatus(),
                response.message(),
                response.computerRow(),
                response.computerCol(),
                response.computerMessage(),
                response.sunkCells(),
                response.homeSunkCells()
        );
        return ResponseEntity.ok(safeResponse);
    }

    /**
     * Retrieves session manager or creates one when absent.
     *
     * @param session HTTP session containing per-user game state
     * @return existing or newly created manager
     */
    private BattleshipManager getOrCreateManager(HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        if (manager == null) {
            LOG.debug("No manager in session. Creating new BattleshipManager.");
            manager = new BattleshipManager();
        }
        return manager;
    }

    /**
     * Converts the computer's board to a version with the ships hidden
     * <p>
     * "hit" and "miss" are unchanged so the player can see prior attacks
     * all other values ("ship", "water") become water, thus hiding them.
     * This prevents accidental leaking without changing anything else
     * </p>
     *
     * @param grid the grid to be masked
     * @return a masked grid containing only "hit", "miss", or "water"
     */
    private String[][] maskComputerGrid(String[][] grid) {
        String[][] masked = new String[grid.length][grid[0].length];

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                String cell = grid[r][c];
                masked[r][c] = switch (cell) {
                    case "hit" -> "hit";
                    case "miss" -> "miss";
                    default -> "water"; // hides ship as water
                };
            }
        }
        return masked;
    }
}
