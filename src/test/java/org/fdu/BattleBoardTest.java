package org.fdu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class BattleBoardTest {

    private BattleBoard board;
    private Cell[][] grid;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        board = new BattleBoard();

        // Redirect System.out so we can assert on printed output
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        // Default 10x10 grid filled with WATER
        grid = new Cell[10][10];
        for (int r = 0; r < 10; r++)
            for (int c = 0; c < 10; c++)
                grid[r][c] = Cell.WATER;
    }

    @AfterEach
    void restoreStream() {
        System.setOut(System.out);
    }

    // Header tests

    @Test
    @DisplayName("Header: first line contains all column labels A through J")
    void headerContainsAllColumnLabels() {
        board.displayBoard(grid);
        String firstLine = out.toString().lines().findFirst().orElse("");

        for (char c = 'A'; c <= 'J'; c++) {
            assertTrue(firstLine.contains("[" + c + "]"),
                    "Missing column label: [" + c + "]");
        }
    }

    @Test
    @DisplayName("Header: exactly 10 column labels are printed")
    void headerHasExactlyTenColumns() {
        board.displayBoard(grid);
        String firstLine = out.toString().lines().findFirst().orElse("");

        long count = firstLine.chars().filter(ch -> ch == '[').count();
        assertEquals(10, count);
    }

    // Row number tests

    @Test
    @DisplayName("Rows: output contains 11 lines total (1 header + 10 rows)")
    void outputHasElevenLines() {
        board.displayBoard(grid);
        long lineCount = out.toString().lines().count();
        assertEquals(11, lineCount);
    }

    @Test
    @DisplayName("Rows: row numbers 1 through 10 are each present in the output")
    void rowNumbersOneToTenArePresent() {
        board.displayBoard(grid);
        String output = out.toString();

        for (int i = 1; i <= 10; i++) {
            assertTrue(output.contains(String.valueOf(i)),
                    "Missing row number: " + i);
        }
    }

    // Cell symbol tests

    @Test
    @DisplayName("Symbols: WATER cell is rendered as [~]")
    void waterCellRenderedAsTilde() {
        board.displayBoard(grid);
        assertTrue(out.toString().contains("[~]"));
    }

    @Test
    @DisplayName("Symbols: HIT cell is rendered as [X]")
    void hitCellRenderedAsX() {
        grid[0][0] = Cell.HIT;
        board.displayBoard(grid);
        assertTrue(out.toString().contains("[X]"));
    }

    @Test
    @DisplayName("Symbols: MISS cell is rendered as [O]")
    void missCellRenderedAsO() {
        grid[0][0] = Cell.MISS;
        board.displayBoard(grid);
        assertTrue(out.toString().contains("[O]"));
    }

    @Test
    @DisplayName("Symbols: SHIP cell is rendered as [~] to keep ship positions hidden")
    void shipCellRenderedAsTilde() {
        grid[0][0] = Cell.SHIP;
        board.displayBoard(grid);

        // [X] must not appear, and [~] must be present, proving SHIP is hidden
        assertFalse(out.toString().contains("[X]"));
        assertTrue(out.toString().contains("[~]"));
    }

    // Alignment tests

    @Test
    @DisplayName("Alignment: each row line contains exactly 10 cell symbols")
    void eachRowContainsTenCells() {
        board.displayBoard(grid);

        // Skip the header line, check the 10 data rows
        out.toString().lines().skip(1).forEach(line -> {
            long count = line.chars().filter(ch -> ch == '[').count();
            assertEquals(10, count, "Row does not contain exactly 10 cells: " + line);
        });
    }
}