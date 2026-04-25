package org.fdu;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Battleship game API.
 */
@RestController
@RequestMapping("/api/battleship")
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    private final BattleshipService battleshipService;

    public BoardController(BattleshipService battleshipService) {
        this.battleshipService = battleshipService;
    }

    @PostMapping("/start-game")
    public int startGame(HttpSession session) {
        log.debug("POST /api/battleship/start-game");
        BattleshipManager manager = getOrCreateManager(session);
        int guessesLeft = battleshipService.startGame(manager);
        session.setAttribute("game", manager);
        log.debug("Game stored in session. guessesLeft={}", guessesLeft);
        return guessesLeft;
    }

    @PostMapping("/placement-start")
    public String[][] placementStart(HttpSession session) {
        log.debug("POST /api/battleship/placement-start");
        BattleshipManager manager = new BattleshipManager();
        session.setAttribute("game", manager);
        return battleshipService.startPlacement(manager);
    }

    @PostMapping("/place-ship")
    public AttackResponseDTO placeShip(@RequestBody PlaceShipRequestDTO request, HttpSession session) {
        log.debug("POST /api/battleship/place-ship row={}, col={}, len={}, horizontal={}",
                request.row(), request.col(), request.shipLength(), request.horizontal());
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        session.setAttribute("game", manager);
        return battleshipService.placeShip(request, manager);
    }

    @PostMapping("/attack")
    public AttackResponseDTO attack(@RequestBody AttackRequestDTO request, HttpSession session) {
        log.debug("POST /api/battleship/attack row={}, col={}", request.row(), request.column());
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        session.setAttribute("game", manager);
        return battleshipService.processAttack(request, manager);
    }

    // -------------------------------------------------------------------------
    // Debug / test endpoints
    // -------------------------------------------------------------------------

    @PostMapping("/debug/set-manager")
    public void setManager(@RequestBody BattleshipManager newManager, HttpSession session) {
        log.debug("POST /api/battleship/debug/set-manager");
        session.setAttribute("game", newManager);
    }

    @GetMapping("/humanStatus")
    public PlayerDTO getHumanStatus(HttpSession session) {
        log.debug("GET /api/battleship/humanStatus");
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return manager.getHumanDTO();
    }

    @GetMapping("/computerStatus")
    public PlayerDTO getComputerStatus(HttpSession session) {
        log.debug("GET /api/battleship/computerStatus");
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return manager.getComputerDTO();
    }

    @GetMapping("/battleshipManager")
    public BattleshipManager getBattleshipManager(HttpSession session) {
        log.debug("GET /api/battleship/battleshipManager");
        return (BattleshipManager) session.getAttribute("game");
    }

    private BattleshipManager getOrCreateManager(HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        if (manager == null) {
            log.debug("No manager in session. Creating new BattleshipManager.");
            manager = new BattleshipManager();
        }
        return manager;
    }
}