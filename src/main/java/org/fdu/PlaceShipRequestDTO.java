package org.fdu;

/**
 * Data Transfer Object sent from the frontend when the player places a ship.
 *
 * @param row        Start row of the ship (0-9)
 * @param col        Start column of the ship (0-9)
 * @param shipLength Length of the ship being placed
 * @param horizontal true = horizontal placement, false = vertical
 */
public record PlaceShipRequestDTO(int row, int col, int shipLength, boolean horizontal) {}