package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
    @DisplayName("Should return error if manager is null (no session)")
    void testProcessAttackNullManager() {
        AttackRequestDTO request = new AttackRequestDTO(0, 0);
        AttackResponseDTO response = service.processAttack(request, null);

        assertTrue(response.isError());
        assertEquals("Start a game first", response.message());
    }

    @Test
    @DisplayName("Should reject attacks outside the 0-9 range")
    void testProcessAttackOutOfBounds() {
        service.startGame(manager);

        AttackRequestDTO request = new AttackRequestDTO(10, 10);
        AttackResponseDTO response = service.processAttack(request, manager);

        assertTrue(response.isError());
        assertEquals("Invalid coordinates", response.message());
    }

    @Test
    @DisplayName("Should reject attacking the same cell twice")
    void testProcessAttackDuplicate() {
        service.startGame(manager);
        AttackRequestDTO request = new AttackRequestDTO(5, 5);

        // First attack
        service.processAttack(request, manager);

        // Second attack on same spot
        AttackResponseDTO response = service.processAttack(request, manager);

        assertTrue(response.isError());
        assertEquals("Cell already attacked", response.message());
    }

    // -------------------------------------------------------------------------
    // Placement Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return failure response if placement is invalid (out of bounds)")
    void testInvalidPlacement() {
        service.startPlacement(manager);

        // Try to place a 5-cell ship starting at index 8 (will go off board)
        PlaceShipRequestDTO request = new PlaceShipRequestDTO(8, 8, 5, true);
        AttackResponseDTO response = service.placeShip(request, manager);

        assertTrue(response.isError());
        assertEquals("Invalid placement", response.message());
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