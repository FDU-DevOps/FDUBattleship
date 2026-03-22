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

    /**
     * USELESS METHODS FOR JACOCO TEST
     */
    public int uselessMethodToTestJaCoCo(int a, int b) {
        return a + b;
    }

    public String uselessMethodGetGameTitle() {
        String title = "FDU Battleship";
        String version = "v1.0";
        return title + " " + version;
    }

    public static void main( String[] args )
    {
        App app = new App();
        if (args.length == 0) {
            System.out.println(app.DisplayText());
        } else {
            System.out.println("Args provided");
        }
    }
}
