package org.fdu;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        BattleBoard board = new BattleBoard();
        BattleShipManager manager = new BattleShipManager();
        PlayerDTO player = board.getState();
        player = manager.startGame(player,0, 0, 9,9);
        board.displayBoard();

        Scanner scanner = new Scanner(System.in);
        System.out.println(player.grid()[0][0]);
        System.out.println(player.grid()[9][9]);
        while (true) {
            System.out.print("Enter your guess (e.g. A5): ");
            String userGuess = scanner.nextLine();

            player = manager.validatePlayerGuess(userGuess, player);

            if (!player.isValidGuess()) {
                System.out.println(player.statusMessage());
            }
            // valid guess is processed, so continue game
        }
    }
}