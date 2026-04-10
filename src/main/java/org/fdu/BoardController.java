package org.fdu;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battleship")
public class BoardController {

    @PostMapping("/start-game")
    public int startGame(HttpSession session) {
        BattleshipManager manager = new BattleshipManager();
        session.setAttribute("game", manager);
        return manager.getHumanDTO().guessesLeft();
    }

    @PostMapping("/attack")
    public AttackResponseDTO attack(@RequestBody AttackRequestDTO request,
                                    HttpSession session) {

        BattleshipManager manager =
                (BattleshipManager) session.getAttribute("game");


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

        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return new AttackResponseDTO(
                    null,
                    human.guessesLeft(),
                    human.gameStatus().name(),
                    "Invalid coordinates",
                    true
            );
        }


        if (grid[row][col] == Cell.HIT || grid[row][col] == Cell.MISS) {
            return new AttackResponseDTO(
                    convertGrid(grid),
                    human.guessesLeft(),
                    human.gameStatus().name(),
                    "Cell already attacked",
                    true
            );
        }


        PlayerDTO[] result =
                manager.getAttackProcessor()
                        .processAttack(row, col, human, computer);

        PlayerDTO updatedHuman = result[0];
        PlayerDTO updatedComputer = result[1];

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
}