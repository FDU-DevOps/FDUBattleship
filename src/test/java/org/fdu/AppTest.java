package org.fdu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void helloAppTest() {
        assertTrue(true);
    }

    @Test
    void displayTextTest() {
        App app = new App();
        assertEquals("Welcome to FDU Battleship!", app.DisplayText());
    }

    @DisplayName("Useless test to test JaCoCo")
    @Test
    void testUselessMethod() {
        App app = new App();
        assertEquals(5, app.uselessMethodToTestJaCoCo(2, 3));
    }

    @DisplayName("Useless test to test JaCoCo")
    @Test
    void testGetGameTitle() {
        App app = new App();
        assertEquals("FDU Battleship v1.0", app.uselessMethodGetGameTitle());
    }
}
