package org.fdu;

/**
 * Data Transfer Object representing the difficulty selected by the player
 * @param difficulty the difficulty selected by the player and set during confirmPlacement in the javascript
 */
public record StartGameRequestDTO(Difficulty difficulty) {}