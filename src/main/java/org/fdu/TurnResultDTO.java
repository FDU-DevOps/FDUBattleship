package org.fdu;

import java.io.Serializable;

/**
 * Immutable result of one full game turn (player attack + computer retaliation).
 * <p>
 * Replaces the {@code PlayerDTO[]} array previously returned by
 * {@link AttackProcessor#processAttack}, eliminating the four mutable fields
 * that were used to smuggle extra data out of that method. Every piece of
 * information produced during a turn is carried here, so callers never need
 * to query the processor after the fact.
 * </p>
 *
 * @param updatedHuman    the human player's state after both moves this turn
 * @param updatedComputer the computer's state after the player's attack
 * @param sunkShip        the computer ship sunk by the player this turn,
 *                        or {@code null} if no ship was sunk
 * @param homeSunkShip    the player ship sunk by the computer this turn,
 *                        or {@code null} if no ship was sunk
 * @param computerRow     row index (0-9) of the computer's attack this turn,
 *                        or {@code -1} if the computer did not fire (player won)
 * @param computerCol     column index (0-9) of the computer's attack this turn,
 *                        or {@code -1} if the computer did not fire (player won)
 */
public record TurnResultDTO(
        PlayerDTO updatedHuman,
        PlayerDTO updatedComputer,
        Ship sunkShip,
        Ship homeSunkShip,
        int computerRow,
        int computerCol
) implements Serializable {}

