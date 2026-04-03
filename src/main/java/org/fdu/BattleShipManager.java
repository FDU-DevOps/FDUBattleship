package org.fdu;
import org.fdu.PlayerDTO;

public class BattleShipManager
{

    /** Validates User Coordinate Guess - returns isValid object in DTO <br>
     * Scope: Guess is normalized before it comes in
     * @param playerGuess - guess coming in from the system
     * @return isValid DTO Field is returned as true or false
     */
    public PlayerDTO validatePlayerGuess(String playerGuess,  PlayerDTO player)
    {
        // normalize playerGuess - toUpperCase, truncate if guess is longer than index 3

        //Return false for all these checks
        // Check if guess is proper format (A5, not 5A)

        // Check if guess is out of bounds

        // Check if cell has already been marked

        //otherwise return true

        // Check if guess is out of bounds

        boolean validationResult = true;
        String guessStatus = "";

        if (playerGuess == null) {
            validationResult = false;
             guessStatus = "Sorry the guess is invalid";
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        // Normalize and check format
        String guess = playerGuess.toUpperCase().trim();
        if (!guess.matches("^[A-J](10|[1-9])$")) {
             guessStatus = "Sorry the guess is invalid";
            validationResult = false;
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        // Extract coordinates
        int row = guess.charAt(0) - 'A';
        int col = Integer.parseInt(guess.substring(1)) - 1;

        if (player.grid()[row][col] != Cell.WATER)
        {
            guessStatus = "Sorry the guess is invalid";
            validationResult = false;
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        return new PlayerDTO(player.grid(), validationResult, guessStatus);
    }
}
