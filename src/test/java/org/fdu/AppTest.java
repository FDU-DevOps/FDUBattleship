package org.fdu;

import org.junit.jupiter.api.Test;

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
}
