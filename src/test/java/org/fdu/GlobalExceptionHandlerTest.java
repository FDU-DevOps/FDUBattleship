package org.fdu;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("IllegalArgumentException -> 400")
    void testBadRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/testFake/fire");

        ResponseEntity<ProblemDetail> response =
                handler.handleBadRequest(
                        new IllegalArgumentException("Wrong input"),
                        request
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getTitle());
        assertEquals("Wrong input", response.getBody().getDetail());
        assert response.getBody().getType() != null;
        assertEquals("/problems/bad-request", response.getBody().getType().toString());
        assert response.getBody().getInstance() != null;
        assertEquals("/testFake/fire", response.getBody().getInstance().toString());
        assert response.getBody().getProperties() != null;
        assertNotNull(response.getBody().getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("NoSuchElementException -> 404")
    void testNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/games/999");

        ResponseEntity<ProblemDetail> response =
                handler.handleNotFound(
                        new NoSuchElementException("Game not found"),
                        request
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource Not Found", response.getBody().getTitle());
        assertEquals("Game not found", response.getBody().getDetail());
    }

    @Test
    @DisplayName("IllegalStateException -> 422")
    void testUnprocessable() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/game/1/fire");

        ResponseEntity<ProblemDetail> response =
                handler.handleUnprocessable(
                        new IllegalStateException("Already attacked"),
                        request
                );

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unprocessable Entity", response.getBody().getTitle());
        assertEquals("Already attacked", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Exception -> 500")
    void testUnexpected() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/testFake/error");

        ResponseEntity<ProblemDetail> response =
                handler.handleUnexpected(
                        new RuntimeException("BOOOOOOMMMMMMM WOW"),
                        request
                );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getTitle());
        assertEquals("An unexpected error occurred", response.getBody().getDetail());
    }
}