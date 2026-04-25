package org.fdu;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Battleship game API.
 * <p>
 * This class is a thin HTTP layer. Its only responsibilities are:
 * <ul>
 *   <li>Reading and writing the {@link BattleshipManager} from the HTTP session</li>
 *   <li>Delegating all game logic to {@link BattleshipService}</li>
 *   <li>Returning the response to the frontend</li>
 * </ul>
 * No message building, grid conversion, or game logic belongs here.
 * </p>
 */
@RestController
@RequestMapping("/api/battleship")
public class BoardController {

    private final BattleshipService battleshipService;

    public BoardController(BattleshipService battleshipService) {
        this.battleshipService = battleshipService;
    }

    /**
     * Starts a fully random game (skips manual placement) and returns
     * the player's starting guess count.
     *
     * @param session the HTTP session for this player
     * @return starting guess count
     */
    @PostMapping("/start-game")
    public int startGame(HttpSession session) {
        BattleshipManager manager = getOrCreateManager(session);
        int guessesLeft = battleshipService.startGame(manager);
        session.setAttribute("game", manager);
        return guessesLeft;
    }

    /**
     * Initializes the placement phase and returns the blank player home grid.
     *
     * @param session the HTTP session for this player
     * @return blank player home grid as lowercase strings
     */
    @PostMapping("/placement-start")
    public String[][] placementStart(HttpSession session) {
        BattleshipManager manager = new BattleshipManager();
        session.setAttribute("game", manager);
        return battleshipService.startPlacement(manager);
    }

    /**
     * Receives a single ship placement from the player and returns the result.
     *
     * @param request the placement parameters from the frontend
     * @param session the HTTP session holding the {@link BattleshipManager}
     * @return {@link AttackResponseDTO} with updated home grid and placement status
     */
    @PostMapping("/place-ship")
    public AttackResponseDTO placeShip(@RequestBody PlaceShipRequestDTO request, HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return battleshipService.placeShip(request, manager);
    }

    /**
     * Handles a single attack from the player and returns the full turn result,
     * including the computer's retaliatory move.
     *
     * @param request the attack coordinates from the frontend
     * @param session the HTTP session holding the {@link BattleshipManager}
     * @return {@link AttackResponseDTO} with both updated grids, messages, and game status
     */
    @PostMapping("/attack")
    public AttackResponseDTO attack(@RequestBody AttackRequestDTO request, HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return battleshipService.processAttack(request, manager);
    }

    // -------------------------------------------------------------------------
    // Debug / test endpoints
    // -------------------------------------------------------------------------

    @PostMapping("/debug/set-manager")
    public void setManager(@RequestBody BattleshipManager newManager, HttpSession session) {
        session.setAttribute("game", newManager);
    }

    @GetMapping("/humanStatus")
    public PlayerDTO getHumanStatus(HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return manager.getHumanDTO();
    }

    @GetMapping("/computerStatus")
    public PlayerDTO getComputerStatus(HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return manager.getComputerDTO();
    }

    @GetMapping("/battleshipManager")
    public BattleshipManager getBattleshipManager(HttpSession session) {
        return (BattleshipManager) session.getAttribute("game");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the existing {@link BattleshipManager} from the session,
     * or creates a new one if none exists. Used by start-game to support
     * both fresh starts and re-starts over an existing placement session.
     */
    private BattleshipManager getOrCreateManager(HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        if (manager == null) {
            manager = new BattleshipManager();
        }
        return manager;
    }
}