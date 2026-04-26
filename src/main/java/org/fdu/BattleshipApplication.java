/**
 * Battleship application package.
 */
package org.fdu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Battleship Spring Boot application.
 */
@SpringBootApplication
public class BattleshipApplication {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private BattleshipApplication() {
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(BattleshipApplication.class, args);
    }
}