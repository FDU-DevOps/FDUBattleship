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

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

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
        log.debug("POST /api/battleship/start-game");
        BattleshipManager manager = getOrCreateManager(session);
        int guessesLeft = battleshipService.startGame(manager);
        session.setAttribute("game", manager);
        log.debug("Game stored in session. guessesLeft={}", guessesLeft);
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
        log.debug("POST /api/battleship/placement-start");
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
        log.debug("POST /api/battleship/place-ship row={}, col={}, len={}, horizontal={}",
                request.row(), request.col(), request.shipLength(), request.horizontal());
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        AttackResponseDTO response = battleshipService.placeShip(request, manager);
        session.setAttribute("game", manager);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes one attack turn and returns updated game state for both boards.
     *
     * @param request attack request payload
     * @param session HTTP session containing per-user game state
     * @return resolved attack response payload
     */
    @PostMapping("/attack")
    public ResponseEntity<AttackResponseDTO> attack(@RequestBody AttackRequestDTO request, HttpSession session) {
        log.debug("POST /api/battleship/attack row={}, col={}", request.row(), request.column());
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        AttackResponseDTO response = battleshipService.processAttack(request, manager);
        session.setAttribute("game", manager);
        return ResponseEntity.ok(response);
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
            log.debug("No manager in session. Creating new BattleshipManager.");
            manager = new BattleshipManager();
        }
        return manager;
    }
}