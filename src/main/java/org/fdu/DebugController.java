package org.fdu;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

// -------------------------------------------------------------------------
// Debug / test endpoints
// -------------------------------------------------------------------------


@Profile("test")
@RestController
@RequestMapping("/api/battleship")
public class DebugController {

    private static final Logger LOG = LoggerFactory.getLogger(DebugController.class);
    // -------------------------------------------------------------------------
    // Debug / test endpoints
    // -------------------------------------------------------------------------

    /**
     * Replaces the current session manager (debug/test only).
     *
     * @param newManager manager payload to set in session
     * @param session    HTTP session containing per-user game state
     */
    @PostMapping("/debug/set-manager")
    public void setManager(@RequestBody BattleshipManager newManager, HttpSession session) {
        LOG.debug("POST /api/battleship/debug/set-manager");
        session.setAttribute("game", newManager);
    }

    /**
     * Returns current human-side DTO from session manager.
     *
     * @param session HTTP session containing per-user game state
     * @return current human DTO
     */
    @GetMapping("/humanStatus")
    public PlayerDTO getHumanStatus(HttpSession session) {
        LOG.debug("GET /api/battleship/humanStatus");
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return manager.getHumanDTO();
    }

    /**
     * Returns current computer-side DTO from session manager.
     *
     * @param session HTTP session containing per-user game state
     * @return current computer DTO
     */
    @GetMapping("/computerStatus")
    public PlayerDTO getComputerStatus(HttpSession session) {
        LOG.debug("GET /api/battleship/computerStatus");
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return manager.getComputerDTO();
    }

    /**
     * Returns the full session manager object (debug/test endpoint).
     *
     * @param session HTTP session containing per-user game state
     * @return current session manager
     */
    @GetMapping("/battleshipManager")
    public BattleshipManager getBattleshipManager(HttpSession session) {
        LOG.debug("GET /api/battleship/battleshipManager");
        return (BattleshipManager) session.getAttribute("game");
    }
}
