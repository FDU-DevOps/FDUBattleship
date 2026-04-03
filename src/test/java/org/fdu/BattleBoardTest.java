package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;


class BattleBoardTest {

    private BattleBoard board;

    @BeforeEach
    void setUp() {
        board = new BattleBoard();
    }

    @Test
    void getState_returnsNonNullDTO() {
        assertNotNull(board.getState());
    }

    @Test
    void getState_gridIsProperSize() {
        Cell[][] grid = board.getState().grid();
        assertEquals(10, grid.length);
        for (Cell[] row : grid) {
            assertEquals(10, row.length);
        }
    }

    @Test
    void displayBoard_outputContainsAllColumnHeaders() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        board.displayBoard();
        System.setOut(System.out);
        String output = out.toString();

        for (char c : new char[]{'A','B','C','D','E','F','G','H','I','J'}) {
            assertTrue(output.contains(String.valueOf(c)),
                    "Missing column header: " + c);
        }
    }

    @Test
    void displayBoard_outputContainsAllRowNumbers() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        board.displayBoard();
        System.setOut(System.out);
        String output = out.toString();

        for (int i = 1; i <= 10; i++) {
            assertTrue(output.contains(String.valueOf(i)),
                    "Missing row number: " + i);
        }
    }
}