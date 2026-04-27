package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class BattleshipServiceTest {

    private BattleshipService service;
    private BattleshipManager manager;

    @BeforeEach
    void setUp() {
        // Real instances, no mocking magic.
        service = new BattleshipService();
        manager = new BattleshipManager();
    }

    // -------------------------------------------------------------------------
    // Lifecycle Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should initialize game and return the starting guess count")
    void testStartGame() {
        int guesses = service.startGame(manager);

        assertTrue(guesses > 0);
        assertEquals(GameStatus.IN_PROGRESS, manager.getHumanDTO().gameStatus());
    }

    @Test
    @DisplayName("Should return a blank 10x10 lowercase grid for placement")
    void testStartPlacement() {
        String[][] grid = service.startPlacement(manager);

        assertEquals(10, grid.length);
        assertEquals(10, grid[0].length);
        assertEquals("water", grid[0][0]);
    }

    // -------------------------------------------------------------------------
    // Attack Validation Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw when manager is null (no session)")
    void testProcessAttackNullManager() {
        AttackRequestDTO request = new AttackRequestDTO(0, 0);

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> service.processAttack(request, null)
        );
        assertEquals("No active game session found", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject attacks outside the 0-9 range")
    void testProcessAttackOutOfBounds() {
        service.startGame(manager);
        AttackRequestDTO request = new AttackRequestDTO(10, 10);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.processAttack(request, manager)
        );
        assertEquals("Coordinates must be between 0 and 9", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject attacking the same cell twice")
    void testProcessAttackDuplicate() {
        service.startGame(manager);
        AttackRequestDTO request = new AttackRequestDTO(5, 5);

        // First attack should pass
        service.processAttack(request, manager);

        // Second attack on same spot should throw
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.processAttack(request, manager)
        );
        assertEquals("Cell already attacked", ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Placement Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw if placement is invalid (out of bounds)")
    void testInvalidPlacement() {
        service.startPlacement(manager);

        // Try to place a 5-cell ship starting at index 8 (will go off board)
        PlaceShipRequestDTO request = new PlaceShipRequestDTO(8, 8, 5, true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.placeShip(request, manager)
        );
        assertEquals("Cannot place ship at requested position", ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Utility Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should convert Cell enum grid to lowercase String grid")
    void testConvertGrid() {
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = Cell.WATER;
        grid[0][1] = Cell.SHIP;
        grid[1][0] = Cell.HIT;
        grid[1][1] = Cell.MISS;

        String[][] result = service.convertGrid(grid);

        assertEquals("water", result[0][0]);
        assertEquals("ship", result[0][1]);
        assertEquals("hit", result[1][0]);
        assertEquals("miss", result[1][1]);
    }
}