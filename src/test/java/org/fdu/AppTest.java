package org.fdu;

/*
import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;
*/

class AppTest {
    /*
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn  = System.in;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // Reads "Ship is at: B3" debug line and returns e.g. "B3"
    private String shipCoord(String output) {
        return output.lines()
                .filter(l -> l.startsWith("Ship is at: "))
                .findFirst()
                .map(l -> l.replace("Ship is at: ", "").trim())
                .orElseThrow();
    }

    // Returns a coordinate guaranteed NOT to be the ship
    private String missCoord(String shipCoord) {
        return shipCoord.startsWith("A") ? "B1" : "A1";
    }

    // Runs a full discovery game to drain the loop, returns the ship coord
    private String discover() {
        // Feed 10 dummy inputs so the loop always finishes regardless of ship position
        String dummyInputs = "A1\nA2\nA3\nA4\nA5\nA6\nA7\nA8\nA9\nA10\n";
        System.setIn(new ByteArrayInputStream(dummyInputs.getBytes()));
        App.main(new String[]{});
        String coord = shipCoord(out.toString());

        // Reset output for the real test run
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        return coord;
    }

    @Test
    @DisplayName("Miss: wrong coordinate prints Miss!")
    void wrongGuessPrintsMiss() {
        String coord = discover();
        String miss = missCoord(coord);

        String inputs = (miss + "\n").repeat(10);
        System.setIn(new ByteArrayInputStream(inputs.getBytes()));
        App.main(new String[]{});

        assertTrue(out.toString().contains("Miss!"));
    }

    @Test
    @DisplayName("Loss: exhausting all guesses on water prints lose message")
    void exhaustingGuessesPrintsLoss() {
        String coord = discover();

        char safeCol = coord.startsWith("A") ? 'B' : 'A';
        StringBuilder inputs = new StringBuilder();
        for (int i = 1; i <= 10; i++) inputs.append(safeCol).append(i).append("\n");

        System.setIn(new ByteArrayInputStream(inputs.toString().getBytes()));
        App.main(new String[]{});

        assertTrue(out.toString().contains("No guesses remaining. You lose!"));
    } */
}