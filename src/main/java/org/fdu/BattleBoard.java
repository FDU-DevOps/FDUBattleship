package org.fdu;
import java.util.Scanner;

/**
 * Stateless renderer for the Battleship tracking grid.
 * <p>
 * Holds no grid state of its own. BattleShipManager passes the current
 * tracking grid from humanDTO each turn to display the player's guess history.
 * </p>
 */

public class BattleBoard {

    /**
     * Prints a live tracking grid passed in from humanDTO.
     * <p>
     * Renders the 10x10 grid with column labels A-J across the top and row
     * numbers 1-10 along the left side. Cell display symbols: HIT as [X],
     * MISS as [O], WATER or SHIP as [~] (ship location hidden from player).
     * Called by BattleShipManager each turn to show the player their current
     * guess history.
     * </p>
     *
     * @param trackingGrid the current tracking grid from humanDTO to render
     */

    public void displayBoard(Cell[][] trackingGrid) {
        printHeader();
        for (int row = 0; row < 10; row++) {
            System.out.printf("%2d", row + 1);
            for (int col = 0; col < 10; col++) {
                Cell cell = trackingGrid[row][col];
                if (cell == Cell.HIT)       System.out.print("[X]");
                else if (cell == Cell.MISS) System.out.print("[O]");
                else                        System.out.print("[~]");
            }
            System.out.println();
        }
    }

    /**
     * Prints the column labels A-J across the top of the board.
     * <p>
     * Outputs a leading two-space indent to align with the row number
     * prefix used in displayBoard, then prints each column letter
     * wrapped in brackets. Called once at the start of each displayBoard
     * invocation.
     * </p>
     */

    private void printHeader() {
        System.out.print("  ");
        for (char c = 'A'; c <= 'J'; c++) {
            System.out.printf("[%c]", c);
        }
        System.out.println();
    }
}