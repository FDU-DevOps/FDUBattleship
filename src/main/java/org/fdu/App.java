package org.fdu;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        BattleBoard board = new BattleBoard();
        BattleShipManager manager = new BattleShipManager();
        PlayerDTO player = board.getState();
        board.displayBoard();

        // Take in user guess
        // validate guess
        // if invalid - error message
        // if not invalid, continue game loop


        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter your guess (e.g. A5): ");
            String userGuess = scanner.nextLine();

            PlayerDTO result = manager.validatePlayerGuess(userGuess, player);

            if (!result.isValidGuess()) {
                System.out.println(result.statusMessage());
                continue;
            }
            // valid guess - process it
            break;
        }

    }
}