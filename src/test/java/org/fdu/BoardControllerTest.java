package org.fdu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class BoardControllerTest {

    @Test
    @DisplayName("Reset board returns a non-null PlayerDTO")
    void resetBoard_returnsNonNullPlayerDTO() {
        BoardController controller = new BoardController();
        PlayerDTO result = controller.resetBoard();
        assertNotNull(result);
    }

    @Test
    @DisplayName("Reset board returns 10 guesses remaining")
    void resetBoard_returnsCorrectGuessCount() {
        BoardController controller = new BoardController();
        PlayerDTO result = controller.resetBoard();
        assertEquals(10, result.guessesLeft());
    }

    @Test
    @DisplayName("Reset board returns game status as IN_PROGRESS")
    void resetBoard_returnsInProgressStatus() {
        BoardController controller = new BoardController();
        PlayerDTO result = controller.resetBoard();
        assertEquals(GameStatus.IN_PROGRESS, result.gameStatus());
    }
}