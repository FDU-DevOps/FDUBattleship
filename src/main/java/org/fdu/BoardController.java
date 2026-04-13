package org.fdu;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battleship")
public class BoardController {

    // each start game generates a new object (v. generating a single object
    @PostMapping("/start-game")
    public int startGame(HttpSession session) {
        BattleshipManager manager = new BattleshipManager();
        manager.initializeGame();
        session.setAttribute("game", manager);
        return manager.getHumanDTO().guessesLeft();
    }

    @PostMapping("/attack")
    public AttackResponseDTO attack(@RequestBody AttackRequestDTO request,
                                    HttpSession session) {

        BattleshipManager manager =
                (BattleshipManager) session.getAttribute("game");


        // if attack received, but no game stored, don't process
        // Note: DTO is not saved, so additional attacks will land here as well
        if (manager == null) {
            return new AttackResponseDTO(
                    null, 0, "NO_GAME",
                    "Start a game first",
                    true
            );
        }

        int row = request.row();
        int col = request.column();

        PlayerDTO human = manager.getHumanDTO();
        PlayerDTO computer = manager.getComputerDTO();

        Cell[][] grid = human.grid();

        // invalid attack, return current values other than message and isError boolean
        //   why grid = null?
        //    ToDo: define and use a wither
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return new AttackResponseDTO(
                    null,
                    human.guessesLeft(),
                    human.gameStatus().name(),
                    "Invalid coordinates",
                    true
            );
        }

        // convertGrid converts the enums to Strings for consumption by client
        if (grid[row][col] == Cell.HIT || grid[row][col] == Cell.MISS) {
            return new AttackResponseDTO(
                    convertGrid(grid),
                    human.guessesLeft(),
                    human.gameStatus().name(),
                    "Cell already attacked",
                    true
            );
        }

        // valid attack ...
        PlayerDTO[] result =
                manager.getAttackProcessor()
                        .processAttack(row, col, human, computer);

        PlayerDTO updatedHuman = result[0];
        PlayerDTO updatedComputer = result[1];

        // never updates session - session contains a BattleshipManager object
        //   updates the DTOs inside the object
        manager.setHumanDTO(updatedHuman);
        manager.setComputerDTO(updatedComputer);


        String message =
                updatedHuman.grid()[row][col] == Cell.HIT ? "Hit!" : "Miss!";

        return new AttackResponseDTO(
                convertGrid(updatedHuman.grid()),
                updatedHuman.guessesLeft(),
                updatedHuman.gameStatus().name(),
                message,
                false
        );
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