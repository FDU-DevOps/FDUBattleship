package org.fdu;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battleship")
public class BoardController {

    // each start game generates a new object (v. generating a single object
    @PostMapping("/start-game")
    public int startGame(HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");

        // If no placement session exists, fall back to a full random game
        if (manager == null) {
            manager = new BattleshipManager();
            manager.initializeGame();
            session.setAttribute("game", manager);
        }

        return manager.getHumanDTO().guessesLeft();
    }

    /**
     * Initializes the placement phase for a new game session.
     * Sets up computer ships and a blank player home grid.
     * Stores the manager in the session.
     *
     * @param session the HTTP session for this player
     * @return the blank player home grid as lowercase strings for the frontend to render
     */
    @PostMapping("/placement-start")
    public String[][] placementStart(HttpSession session) {
        BattleshipManager manager = new BattleshipManager();
        manager.initializePlacementPhase();
        session.setAttribute("game", manager);
        return convertGrid(manager.getHumanDTO().homeGrid());
    }
    /**
     * Receives a single ship placement from the player and validates it.
     * Delegates to placePlayerShip() in BattleshipManager.
     *
     * @param request PlaceShipRequestDTO with row, col, shipLength, horizontal
     * @param session the HTTP session holding the BattleshipManager
     * @return AttackResponseDTO with updated home grid, success flag, and placement status
     */
    @PostMapping("/place-ship")
    public AttackResponseDTO placeShip(@RequestBody PlaceShipRequestDTO request,
                                       HttpSession session) {
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");

        if (manager == null) {
            return new AttackResponseDTO(
                    null, null, 0, "NO_GAME",
                    "Start a game first",
                    -1, -1, "",
                    true, null, null
            );
        }

        boolean success = manager.placePlayerShip(
                request.row(), request.col(),
                request.shipLength(), request.horizontal()
        );

        String[][] updatedGrid = convertGrid(manager.getHumanDTO().homeGrid());
        boolean allPlaced = manager.isPlacementComplete();

        return new AttackResponseDTO(
                null,
                updatedGrid,
                0,
                allPlaced ? GameStatus.IN_PROGRESS.name() : GameStatus.PLACEMENT.name(),
                success ? "Ship placed" : "Invalid placement",
                -1, -1, "",
                !success,
                null, null
        );
    }

    /**
     * Handles a single attack request from the player and returns the full result
     * of the turn, including the computer's retaliatory move.
     * <p>
     * Validates that a game session exists, that the coordinates are in range,
     * and that the targeted cell has not already been attacked. Delegates the
     * full turn to AttackProcessor, reads the computer's move coordinates back
     * from the processor, builds human-readable messages for both moves, and
     * returns an AttackResponseDTO carrying both updated board states.
     * </p>
     * <p>
     * The session object (BattleshipManager) is updated in place after each turn
     * by calling setHumanDTO and setComputerDTO. The BattleshipManager reference
     * itself is not replaced.
     * </p>
     *
     * @param request AttackRequestDTO containing row and column of the player's attack
     * @param session the HTTP session holding the BattleshipManager for this game
     * @return AttackResponseDTO with both grids, both messages, game status, and
     *         the computer's move coordinates
     */

    @PostMapping("/attack")
    public AttackResponseDTO attack(@RequestBody AttackRequestDTO request,
                                    HttpSession session) {

        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");

        if (manager == null) {
            return new AttackResponseDTO(
                    null, null, 0, "NO_GAME",
                    "Start a game first",
                    -1, -1, "",
                    true, null, null
            );
        }

        int row = request.row();
        int col = request.column();

        PlayerDTO human    = manager.getHumanDTO();
        PlayerDTO computer = manager.getComputerDTO();

        // Reject out-of-bounds
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return new AttackResponseDTO(
                    null, null,
                    human.guessesLeft(),
                    human.gameStatus().name(),
                    "Invalid coordinates",
                    -1, -1, "",
                    true, null, null
            );
        }

        // Reject already-attacked cell
        Cell[][] trackingGrid = human.grid();
        if (trackingGrid[row][col] == Cell.HIT || trackingGrid[row][col] == Cell.MISS) {
            return new AttackResponseDTO(
                    convertGrid(trackingGrid),
                    convertGrid(human.homeGrid()),
                    human.guessesLeft(),
                    human.gameStatus().name(),
                    "Cell already attacked",
                    -1, -1, "",
                    true, null, null
            );
        }

        // Process the full turn (player attack + computer retaliation)
        AttackProcessor processor = manager.getAttackProcessor();
        PlayerDTO[] result = processor.processAttack(row, col, human, computer);

        PlayerDTO updatedHuman = result[0];
        PlayerDTO updatedComputer = result[1];

        manager.setHumanDTO(updatedHuman);
        manager.setComputerDTO(updatedComputer);

        // ----------------------------------------------------------------
        // Build player's attack message
        // ----------------------------------------------------------------
        String playerMessage;
        Ship playerSunkShip = processor.getLastSunkShip();

        if (updatedHuman.gameStatus() == GameStatus.WIN) {
            playerMessage = "You win!";
        } else if (playerSunkShip != null) {
            playerMessage = playerSunkShip.size() + "-cell ship sunk!";
        } else if (updatedHuman.grid()[row][col] == Cell.HIT) {
            playerMessage = "Hit!";
        } else {
            playerMessage = "Miss!";
        }

        // ----------------------------------------------------------------
        // Build computer's attack message
        // ----------------------------------------------------------------
        int compRow = processor.getLastComputerRow();
        int compCol = processor.getLastComputerCol();
        String computerMessage = "";
        Ship homeSunkShip = processor.getLastHomeSunkShip();

        if (compRow >= 0) {
            String coord = (char)('A' + compCol) + String.valueOf(compRow + 1);
            boolean compHit = updatedHuman.homeGrid()[compRow][compCol] == Cell.HIT;

            if (updatedHuman.gameStatus() == GameStatus.LOSS) {
                computerMessage = "Computer sunk your last ship. You lose!";
            } else if (homeSunkShip != null) {
                computerMessage = "Computer sunk your " + homeSunkShip.size() + "-cell ship at " + coord + "!";
            } else if (compHit) {
                computerMessage = "Computer hit your ship at " + coord + "!";
            } else {
                computerMessage = "Computer missed at " + coord + ".";
            }
        }

        // ----------------------------------------------------------------
        // Convert sunk ship cells to int[][] for the frontend
        // ----------------------------------------------------------------
        int[][] sunkCells = shipToCoords(playerSunkShip);
        int[][] homeSunkCells = shipToCoords(homeSunkShip);

        return new AttackResponseDTO(
                convertGrid(updatedHuman.grid()),
                convertGrid(updatedHuman.homeGrid()),
                updatedHuman.guessesLeft(),
                updatedHuman.gameStatus().name(),
                playerMessage,
                compRow,
                compCol,
                computerMessage,
                false,
                sunkCells,
                homeSunkCells
        );
    }

    /** Converts a Ship's cell list to a plain int[][], or null if ship is null. */
    private int[][] shipToCoords(Ship ship) {
        if (ship == null) return null;
        return ship.cells().stream()
                .map(cell -> new int[]{ cell[0], cell[1] })
                .toArray(int[][]::new);
    }

    private String[][] convertGrid(Cell[][] grid) {
        String[][] result = new String[grid.length][grid[0].length];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                result[i][j] = grid[i][j].name().toLowerCase();
            }
        }
        return result;
    }

    // for testing
    @PostMapping("/debug/set-manager")
    public void setManager(@RequestBody BattleshipManager newManager, HttpSession session) {
        // This physically replaces the "Truth" on the server with your "Snapshot"
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
        BattleshipManager manager = (BattleshipManager) session.getAttribute("game");
        return manager;
    }
}