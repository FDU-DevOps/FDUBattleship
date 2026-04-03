package org.fdu;

import org.springframework.web.bind.annotation.*;

@RestController
public class BoardController {

    @GetMapping("/reset")
    public PlayerDTO resetBoard() {
        BattleBoard board = new BattleBoard();
        return board.getState();
    }
}