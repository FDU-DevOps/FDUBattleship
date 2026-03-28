package org.fdu;

import org.junit.jupiter.api.Test;

 //import org.junit.jupiter.api.DisplayName;
 import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void testMain() {
        App.main(new String[]{});
    }

    @Test
    void testGetStateNotNull() {
        BattleBoard board = new BattleBoard();
        assertNotNull(board.getState());
    }
}
