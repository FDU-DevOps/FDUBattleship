package org.fdu;

public class BattleShipManager
{
    /**
     * Overloaded Constructor - Initializes a game with a pre-filled board<br>
     * Scope: Game starts with pre-filled board for testing
     * @param xHitCoord x coordinate for the hit cell
     * @param yHitCoord y coordinate for the hit cell
     * @param xMissCoord x coordinate for the miss cell
     * @param yMissCoord y coordinate for the miss cell
     * @return an updated PlayerDTO with pre-filled cells (hit and miss)
     */
    public PlayerDTO startGame(PlayerDTO player, int xHitCoord, int yHitCoord, int xMissCoord, int yMissCoord)
    {
        player = BattleBoard.initBoard();
        player.grid()[xHitCoord][yHitCoord] = Cell.HIT;
        player.grid()[xMissCoord][yMissCoord] = Cell.MISS;
        return new PlayerDTO(player.grid(),false,"Testing Hit/miss");
    }

    /** Validates User Coordinate Guess - returns isValid object in DTO <br>
     * Scope: Guess is normalized before it comes in
     * @param playerGuess - guess coming in from the system
     * @return isValid DTO Field is returned as true or false
     */
    public PlayerDTO validatePlayerGuess(String playerGuess,  PlayerDTO player)
    {
        boolean validationResult = true;
        String guessStatus = "";

        // Check that playerGuess is not NUll
        if (playerGuess == null) {
            validationResult = false;
             guessStatus = "Sorry the guess is invalid";
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        // Normalize and check format for the guess using regex
        String guess = playerGuess.toUpperCase().trim();
        if (!guess.matches("^[A-J](10|[1-9])$")) {
             guessStatus = "Sorry the guess is invalid";
            validationResult = false;
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        // Extract coordinates
        int row = guess.charAt(0) - 'A';
        int col = Integer.parseInt(guess.substring(1)) - 1;

        // Check the enum in the cell
        if (player.grid()[row][col] != Cell.WATER)
        {
            guessStatus = "Sorry the guess is invalid";
            validationResult = false;
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        return new PlayerDTO(player.grid(), validationResult, guessStatus);
    }
}
