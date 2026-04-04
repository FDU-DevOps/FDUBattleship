package org.fdu;

import org.springframework.web.bind.annotation.*;

@RestController
public class BoardController {

    @GetMapping("/reset")
    public PlayerDTO resetBoard() {
        BattleshipManager board = new BattleshipManager();
        return board.getHumanDTO();
    }
}