package org.fdu;

/**
 * Welcome to Battleship!
 */
public class App 
{
    /**
     * Prints a welcome message to standard output.
     * This method serves as a simple validation target to confirm
     * that automated testing is functioning correctly in the CI pipeline.
     */
    public String DisplayText()
    {
        return "Welcome to FDU Battleship!";
    }

    public static void main( String[] args )
    {
        App app = new App();
        System.out.println(app.DisplayText());
    }
}
