package org.fdu;

public class BattleShipManager
{

    /** Validates User Coordinate Guess - returns isValid object in DTO <br>
     * Scope: Guess is normalized before it comes in
     * @param playerGuess - guess coming in from the system
     * @return isValid DTO Field is returned as true or false
     */
    boolean validatePlayerGuess(String playerGuess)
    {
        // normalize playerGuess - toUpperCase, truncate if guess is longer than index 3
        //Return false for all these checks
        // Check if guess is proper format (A5, not 5A)
        // Check if guess is out of bounds
        // Check if cell has already been marked
        //otherwise return true
        // Check if guess is out of bounds
        return true;
    }
}
