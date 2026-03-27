package org.fdu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void helloAppTest() {
        assertTrue(true);
    }

    @DisplayName("Useless test to test JaCoCo")
    @Test
    void testUselessMethod() {
        App app = new App();
        assertEquals(5, app.uselessMethodToTestJaCoCo(2, 3));
    }
}
