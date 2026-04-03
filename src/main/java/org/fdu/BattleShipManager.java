package org.fdu;

/**
 *  Service Class Responsible for initializing the game and being an entry point for the game
 *  Processes anything relating to the game (i.e. guesses, validation)
 *  Tracks and update game state based on multiple factors
 */
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
     * @param player - passing playerDTO to be updated based on validity of the guess
     * @return isValid DTO Field is returned as true or false
     */
    public PlayerDTO validatePlayerGuess(String playerGuess,  PlayerDTO player)
    {
        boolean validationResult = false; // Assume guess is false until it can be checked to be true
        String guessStatus;

        // Check that playerGuess is not NUll
        if (playerGuess == null) {
             guessStatus = "Input Error: Missing User Input";
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        // Normalize and check format for the guess using regex
        String guess = playerGuess.toUpperCase().trim();
        if (!guess.matches("^[A-J](10|[1-9])$")) {
             guessStatus = "Format Error: Column needs to be A-J and Row needs to 1-10";
            return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }

        // Extract coordinates
        int row = guess.charAt(0) - 'A';
        int col = Integer.parseInt(guess.substring(1)) - 1;

        // Check the enum in the cell
        Cell currentCell = player.grid()[row][col];

        // Check the cell status and update guessStatus and validationResult accordingly
        switch(currentCell)
        {
            case Cell.WATER:
                guessStatus = "Valid Guess";
                validationResult = true;
                return new PlayerDTO(player.grid(), validationResult, guessStatus);
            case Cell.MISS:
                guessStatus = "Invalid Guess: MISS Cell already guessed";
                return new PlayerDTO(player.grid(), validationResult, guessStatus);
            case Cell.HIT:
                guessStatus = "Invalid Guess: HIT Cell already guessed";
                return new PlayerDTO(player.grid(), validationResult, guessStatus);
            default:
                guessStatus = "Sorry the guess is invalid";
                return new PlayerDTO(player.grid(), validationResult, guessStatus);
        }
    }
}
