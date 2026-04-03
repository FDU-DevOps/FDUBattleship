package org.fdu;

import java.util.Scanner;

/**
 * Application entry point for the Battleship game.
 * <p>
 * Instantiates BattleshipManager to initialize game state, then runs the
 * primary game loop directly, reading user input and delegating to the
 * manager's exposed components.
 * </p>
 */
public class App {
    public static void main(String[] args) {
        BattleshipManager manager = new BattleshipManager();
        Scanner scanner = new Scanner(System.in);

        // The main game loop driven directly by the App class
        while (manager.getHumanDTO().gameStatus() == GameStatus.IN_PROGRESS) {

            // Render the board and prompt the user
            manager.getBattleBoard().displayBoard(manager.getHumanDTO().grid());
            System.out.println("Guesses remaining: " + manager.getHumanDTO().guessesLeft());
            System.out.print("Enter coordinate (e.g. A1): ");

            // Read and parse input
            String input = scanner.nextLine().trim().toUpperCase();
            int col = input.charAt(0) - 'A';
            int row = Integer.parseInt(input.substring(1)) - 1;

            // Delegate to the processor using the manager's current state
            PlayerDTO[] result = manager.getAttackProcessor().processAttack(
                    row, col, manager.getHumanDTO(), manager.getComputerDTO()
            );

            // Unpack and update the manager's state
            manager.setHumanDTO(result[0]);
            manager.setComputerDTO(result[1]);

            // Retrieve the updated DTO to evaluate the outcome
            PlayerDTO updatedHuman = manager.getHumanDTO();

            if (updatedHuman.grid()[row][col] == Cell.HIT) {
                System.out.println("Hit!");
            }
            if (updatedHuman.grid()[row][col] == Cell.MISS) {
                System.out.println("Miss!");
            }
            if (updatedHuman.gameStatus() == GameStatus.WIN) {
                System.out.println("You sunk my battleship! You win!");
            }
            if (updatedHuman.gameStatus() == GameStatus.LOSS) {
                System.out.println("No guesses remaining. You lose!");
            }
        }
    }
}