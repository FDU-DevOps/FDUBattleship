package org.fdu;

/**
 * DTO representing an incoming attack request from the frontend.
 * @param row The 0-indexed row coordinate of the targeted cell (0-9).
 * @param column The 0-indexed column coordinate of the targeted cell (0-9).
 */

public record AttackRequestDTO(int row, int column) {
}
