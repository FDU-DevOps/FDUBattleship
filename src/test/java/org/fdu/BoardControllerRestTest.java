package org.fdu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;   // for printing out the board

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {   // quiet error messages locally, can change if needed
                "logging.level.root=ERROR",
                "logging.level.org.springframework=ERROR"
        } )
@AutoConfigureRestTestClient

class BoardControllerRestTest {
    @Autowired
    private RestTestClient restClient;
    private RestTestClient userA;
    private RestTestClient userB;

    @BeforeEach
    void setUp() {
        // Build specific clients with separate sessions, otherwise springboot creates new sessions for each call
        // .mutate() preserves the server URL and port configuration
        String cookieA = createSessionCookie(restClient);
        userA = restClient.mutate()
                .defaultHeader("Cookie", cookieA)
                .build();
        String cookieB = createSessionCookie(restClient);
        userB = restClient.mutate()
                .defaultHeader("Cookie", cookieB)
                .build();
    }

    @Test
    @DisplayName("Start a new game and verify allowed guesses is correct (at Max)")
    // note: since we always reset b4 a test case, the reset will actually overwrite the original game board
    void testStartGame() {
        // start-game returns initial number of guesses allowed
        assertEquals (BattleshipManager.getMaxGuesses(), reset(userA));
        // retrieve the BoardManager DTOs (internal not passed to client), verify as expected & print
        PlayerDTO humanStatus = getHumanStatus(userA);
        assertNotNull(humanStatus);
        assertEquals(BattleshipManager.getMaxGuesses(), humanStatus.guessesLeft());
        System.out.println(humanStatus);
        System.out.println(Arrays.deepToString(humanStatus.grid()));

        PlayerDTO computerStatus = getComputerStatus(userA);
        assertEquals(0, computerStatus.guessesLeft());  // computer currently has no guesses
        System.out.println(computerStatus);
        System.out.println(Arrays.deepToString(computerStatus.grid()));
    }

    @Test
    @DisplayName("Verify correct number of cells populated with ship cells - 5+4+3*2+2 = 17")
    void testNumCellsPopulated() {
        PlayerDTO computerStatus = getComputerStatus(userA);
        int shipCells = 0;
        for  (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (computerStatus.grid()[i][j] == Cell.SHIP) {
                    shipCells++;
                }
            }
        }
        assertEquals(17, shipCells, "Incorrect number of cells populated with ships, should be 17");
    }

    @Test
    @DisplayName("Clear computer board, and place a 5 cell ship at 0,0")
    //  void placeShip(PlayerDTO dto, int shipLength, boolean isHorizontal, int startCol, int startRow)
    void testClearComputerBoard() {

        BattleshipManager manager = getBattleshipManager(userA);
        PlayerDTO computerStatus = manager.getComputerDTO();
        // clear the grid and verify 0, 0 has no ships
        manager.clearGrid(computerStatus);
        assertEquals(Cell.WATER, computerStatus.grid()[0][0]);
        // place a 5 cell ship horizontally starting from 0,0 and verify
        manager.placeShip(computerStatus, 5, true, 0, 0);
        assertEquals(Cell.SHIP, computerStatus.grid()[0][0]);
        assertEquals(Cell.SHIP, computerStatus.grid()[0][4]);
        assertEquals(Cell.WATER, computerStatus.grid()[0][5]);
    }

    @Test
    @DisplayName("Place a ship - validate both hit and misses, including guess counter behavior")
    void attackTest() {
        BattleshipManager manager = getBattleshipManager(userA);  // initial call to get the Battleship game object

        PlayerDTO computerStatus = manager.getComputerDTO();
        manager.clearGrid(computerStatus);
        // place a 5 cell ship vertically starting from column J and row 6 and verify
        manager.placeShip(computerStatus, 5, false, 9, 5);
        // sync and update the local reference
        userA.post().uri("/api/battleship/debug/set-manager").body(manager).exchange();
        System.out.println(Arrays.deepToString(computerStatus.grid()));

        // a hit - message contains "hit", guesses no changed, cell = HIT
        AttackResponseDTO attackResponse = attack(userA, 6, 9);
        // get the reesult
        PlayerDTO humanStatus = getHumanStatus(userA);
        computerStatus = getComputerStatus(userA);

        // Assert the hit
        System.out.println("Attack(J7): " + attackResponse.message());
        System.out.println(" Response: Guesses Left: " + attackResponse.guessesLeft());
        System.out.println(" Human DTO: Guesses Left: " + humanStatus.guessesLeft());
        assertThat(attackResponse.message()).containsIgnoringCase("hit");
        assertThat(attackResponse.guessesLeft()).isEqualTo(BattleshipManager.getMaxGuesses());
        assertThat(computerStatus.grid()[6][9]).isEqualTo(Cell.HIT);


        // a miss - message contains "miss", num guesses decreases by 1, cell = MISS
        AttackResponseDTO missResponse = attack(userA, 0, 0);
        PlayerDTO computerAfterMiss = getComputerStatus(userA);
        PlayerDTO humanAfterMiss = getHumanStatus(userA);

        assertThat(missResponse.message()).containsIgnoringCase("miss");
        assertThat(missResponse.guessesLeft()).isEqualTo(BattleshipManager.getMaxGuesses()-1);
        assertThat(humanAfterMiss.guessesLeft()).isEqualTo(BattleshipManager.getMaxGuesses()-1);
        assertThat(computerAfterMiss.grid()[0][0]).isEqualTo(Cell.MISS);

        // a 2nd hit - use assertResponse helper function
        AttackResponseDTO hitResponse = attack(userA, 9, 9);
        assertResponse(hitResponse, 29, "in_progress", "hit", false);

        // print out the DTOs for reference
        computerStatus = getComputerStatus(userA);
        humanStatus = getHumanStatus(userA);
        System.out.println(computerStatus);
        System.out.println(Arrays.deepToString(computerStatus.grid()));
        System.out.println(humanStatus);
        System.out.println(Arrays.deepToString(humanStatus.grid()));
    }

    @Test
    @DisplayName("Win - 3 cell vertical ship at 0,0 - with interleaved misses")
    void winTest() {
        BattleshipManager manager = getBattleshipManager(userA);  // initial call to get the Battleship game object

        PlayerDTO computerStatus = manager.getComputerDTO();
        manager.clearGrid(computerStatus);
        // place a 5 cell ship vertically starting from column J and row 6 and verify
        manager.placeShip(computerStatus, 3, false, 0, 0);
        // sync and update the local reference
        userA.post().uri("/api/battleship/debug/set-manager").body(manager).exchange();
        System.out.println(Arrays.deepToString(computerStatus.grid()));

        // let's try a miss, 2 hits, a miss and another hit
        AttackResponseDTO hitResponse = attack(userA, 5, 5);
        assertResponse(hitResponse, 29, "in_progress", "miss", false);

        hitResponse = attack(userA, 1, 0);
        assertResponse(hitResponse, 29, "in_progress", "hit", false);

        hitResponse = attack(userA, 2, 0);
        assertResponse(hitResponse, 29, "in_progress", "hit", false);

        hitResponse = attack(userA, 9, 0);  // miss
        assertResponse(hitResponse, 28, "in_progress", "miss", false);

        hitResponse = attack(userA, 0, 0);  // sunk my ship
        assertResponse(hitResponse, 28, "win", "You win", false);

    }

    @Test
    @DisplayName("Invalid attacks - 2 cell vertical ship at 5,5")
    void invalidAttacksTest() {
        BattleshipManager manager = getBattleshipManager(userA);  // initial call to get the Battleship game object

        PlayerDTO computerStatus = manager.getComputerDTO();
        manager.clearGrid(computerStatus);
        // place a 5 cell ship vertically starting from column J and row 6 and verify
        manager.placeShip(computerStatus, 2, true, 5, 5);
        // sync and update the local reference
        userA.post().uri("/api/battleship/debug/set-manager").body(manager).exchange();

        // should now show 5, 5 as a hit and 4, 4 as a miss
        AttackResponseDTO hitResponse = attack(userA, 5, 5);
        assertResponse(hitResponse, 30, "in_progress", "hit", false);
        // should now show 5, 5 as a hit and 4, 4 as a miss
        hitResponse = attack(userA, 4, 4);
        assertResponse(hitResponse, 29, "in_progress", "miss", false);

        // invalid row and columns - guesses don't change
        hitResponse = attack(userA, -1, 4);
        assertResponse(hitResponse, 29, "in_progress", "", true);
        hitResponse = attack(userA, 1, 10);
        assertResponse(hitResponse, 29, "in_progress", "", true);
        hitResponse = attack(userA, 5, 5);
        assertResponse(hitResponse, 29, "in_progress", "already attacked", true);
        hitResponse = attack(userA, 4, 4);
        assertResponse(hitResponse, 29, "in_progress", "already attacked", true);
    }

    @Test
    @DisplayName("Two users playing concurrently")
    void twoConcurrentUserTest() {
        BattleshipManager managerA = getBattleshipManager(userA);  // initial call to get the Battleship game object

        PlayerDTO computerStatusA = managerA.getComputerDTO();
        managerA.clearGrid(computerStatusA);
        // place a 5 cell ship vertically starting from column J and row 6 and verify
        managerA.placeShip(computerStatusA, 2, true, 5, 5);
        // sync and update the local reference
        userA.post().uri("/api/battleship/debug/set-manager").body(managerA).exchange();


        // start a 2nd game
        reset(userB);
        BattleshipManager managerB = getBattleshipManager(userB);

        PlayerDTO computerStatusB = managerB.getComputerDTO();
        managerB.clearGrid(computerStatusB);
        // place a 5 cell ship vertically starting from column J and row 6 and verify
        managerB.placeShip(computerStatusB, 3, true, 0, 0);
        // sync and update the local reference
        userB.post().uri("/api/battleship/debug/set-manager").body(managerB).exchange();

        // 5, 5 is a hit, 0, 0 a miss
        AttackResponseDTO response = attack(userA, 5, 5);
        assertResponse(response, 30, "in_progress", "hit", false);
        // should now show 5, 5 as a hit and 4, 4 as a miss
        response = attack(userA, 0, 0);
        assertResponse(response, 29, "in_progress", "miss", false);

        // 0, 0 is a hit, 5, 5 a miss
        response = attack(userB, 5, 5);
        assertResponse(response, 29, "in_progress", "miss", false);
        // should now show 5, 5 as a hit and 4, 4 as a miss
        response = attack(userB, 0, 0);
        assertResponse(response, 29, "in_progress", "hit", false);

    }

    @Test
    @DisplayName("Computer move coordinates are valid after a normal attack")
    void computerMoveCoordinatesAreValid() {
        AttackResponseDTO response = attack(userA, 0, 0);
        assertThat(response.computerRow()).isBetween(0, 9);
        assertThat(response.computerCol()).isBetween(0, 9);
    }

    @Test
    @DisplayName("Computer message is not empty after a normal attack")
    void computerMessageIsNotEmptyAfterNormalAttack() {
        AttackResponseDTO response = attack(userA, 0, 0);
        assertThat(response.computerMessage()).isNotBlank();
        assertThat(response.computerMessage())
                .matches(msg -> msg.contains("hit") || msg.contains("missed"));
    }

    @Test
    @DisplayName("Computer message is empty when player wins on that attack")
    void computerMessageIsEmptyOnPlayerWin() {
        BattleshipManager manager = getBattleshipManager(userA);
        PlayerDTO computerStatus = manager.getComputerDTO();
        manager.clearGrid(computerStatus);
        manager.placeShip(computerStatus, 2, true, 0, 0);
        userA.post().uri("/api/battleship/debug/set-manager").body(manager).exchange();

        attack(userA, 0, 0); // hit 1
        AttackResponseDTO response = attack(userA, 0, 1); // sinks last ship, player wins

        assertThat(response.computerMessage()).isEmpty();
        assertThat(response.computerRow()).isEqualTo(-1);
        assertThat(response.computerCol()).isEqualTo(-1);
    }

    // ----------------------   HELPER FUNCTIONS          --------------------------------
    // helper function for setting up the clients -
    private String createSessionCookie(RestTestClient client) {
        var result = client.post().uri("/api/battleship/start-game").exchange().returnResult(String.class);
        // Captures "JSESSIONID=XXXXX; Path=/; HttpOnly"
        return result.getResponseHeaders().getFirst("Set-Cookie");
    }

    // Other helper functions
    private int reset(RestTestClient restClient) {
        return restClient.post()
                .uri("/api/battleship/start-game")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .returnResult()
                .getResponseBody();
    }

    private AttackResponseDTO attack(RestTestClient restClient, int row, int col) {
        AttackRequestDTO attackDTO = new AttackRequestDTO(row, col);
        RestTestClient.RequestBodySpec spec = (RestTestClient.RequestBodySpec) restClient.post()
                .uri("/api/battleship/attack");
        return spec.body(attackDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AttackResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    // Assertion helper
    private void assertResponse(AttackResponseDTO response, int guessesLeft,
                                String expectedStatus, String message, boolean isError) {
        assertThat(response)
                .as("The server returned a null AttackResponseDTO. Check for 500 errors in the console.")
                .isNotNull();
        assertThat(response.guessesLeft()).isEqualTo(guessesLeft);
        assertThat(response.gameStatus()).containsIgnoringCase(expectedStatus);
        assertThat(response.message()).containsIgnoringCase(message);
        assertThat(response.isError()).isEqualTo(isError);
    }

    private PlayerDTO getHumanStatus(RestTestClient client) {
        PlayerDTO humanStatus = client.get().uri("/api/battleship/humanStatus")
                .exchange()
                .expectBody(PlayerDTO.class)
                .returnResult()
                .getResponseBody();
        return humanStatus;
    }

    private PlayerDTO getComputerStatus(RestTestClient client) {
        PlayerDTO computerStatus = client.get().uri("/api/battleship/computerStatus")
                .exchange()
                .expectBody(PlayerDTO.class)
                .returnResult()
                .getResponseBody();
        return computerStatus;
    }

    private BattleshipManager getBattleshipManager(RestTestClient client) {
        BattleshipManager manager = client.get().uri("/api/battleship/battleshipManager")
                .exchange()
                .expectBody(BattleshipManager.class)
                .returnResult()
                .getResponseBody();
        return manager;
    }
}