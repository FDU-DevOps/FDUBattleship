package org.fdu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class BoardControllerTest {

    @Test
    @DisplayName("Start game initializes session")
    void startGame_createsGameInSession() {
        BoardController controller = new BoardController();
        MockHttpSession session = new MockHttpSession();

        int response = controller.startGame(session);

        assertEquals(30, response);
        assertNotNull(session.getAttribute("game"));
    }

    @Test
    @DisplayName("Attack without starting game returns error")
    void attack_withoutGame_returnsError() {
        BoardController controller = new BoardController();
        MockHttpSession session = new MockHttpSession();

        AttackRequestDTO request = new AttackRequestDTO(0, 0);

        AttackResponseDTO response = controller.attack(request, session);

        assertTrue(response.isError());
        assertEquals("NO_GAME", response.gameStatus());
        assertEquals("Start a game first", response.message());
    }

    @Test
    @DisplayName("Attack with invalid coordinates returns error")
    void attack_invalidCoordinates_returnsError() {
        BoardController controller = new BoardController();
        MockHttpSession session = new MockHttpSession();

        controller.startGame(session);

        AttackRequestDTO request = new AttackRequestDTO(-1, 100);

        AttackResponseDTO response = controller.attack(request, session);

        assertTrue(response.isError());
        assertEquals("Invalid coordinates", response.message());
    }

    @Test
    @DisplayName("Valid attack returns updated grid")
    void attack_validMove_returnsGrid() {
        BoardController controller = new BoardController();
        MockHttpSession session = new MockHttpSession();

        controller.startGame(session);

        AttackRequestDTO request = new AttackRequestDTO(0, 0);

        AttackResponseDTO response = controller.attack(request, session);

        assertFalse(response.isError());
        assertNotNull(response.grid());
        assertEquals(10, response.grid().length);
        assertEquals(10, response.grid()[0].length);
    }

    @Test
    @DisplayName("Attacking same cell twice returns error")
    void attack_sameCellTwice_returnsError() {
        BoardController controller = new BoardController();
        MockHttpSession session = new MockHttpSession();

        controller.startGame(session);

        AttackRequestDTO request = new AttackRequestDTO(0, 0);

        // First attack
        controller.attack(request, session);

        // Second attack (same cell)
        AttackResponseDTO response = controller.attack(request, session);

        assertTrue(response.isError());
        assertEquals("Cell already attacked", response.message());
    }
}