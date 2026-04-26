package org.fdu;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

/**
 * Centralized REST exception mapper for the Battleship API.
 * <p>
 * Converts Java exceptions thrown by controllers/services into
 * standardized HTTP error responses using {@link ProblemDetail}.
 * By handling failures in one place, controllers remain focused on
 * happy-path orchestration while error semantics stay consistent.
 * </p>
 * <p>
 * Status-code strategy:
 * </p>
 * <ul>
 *   <li><b>400 Bad Request</b>, malformed/invalid client input</li>
 *   <li><b>404 Not Found</b>, required resource does not exist</li>
 *   <li><b>422 Unprocessable Entity</b>, invalid game actions</li>
 *   <li><b>500 Internal Server Error</b>, unexpected failures</li>
 * </ul>
 * <p>
 * Every mapped response includes common ProblemDetail fields plus
 * a custom {@code timestamp} property for production observability.
 * </p>
 * <p>
 * Extends {@link ResponseEntityExceptionHandler} to integrate with
 * Spring MVC's built-in exception resolution flow.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Logger for this class.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles invalid request arguments, maps to HTTP 400.
     * <p>
     * Typical examples include out-of-range coordinates or other input
     * values that fail service-level argument checks.
     * </p>
     *
     * @param ex      thrown {@link IllegalArgumentException}
     * @param request current HTTP servlet request
     * @return {@link ResponseEntity} with {@link ProblemDetail}, status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(
            final IllegalArgumentException ex,
            final HttpServletRequest request) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                "/problems/bad-request",
                request.getRequestURI()
        );
    }

    /**
     * Handles missing resources, maps to HTTP 404.
     * <p>
     * Commonly indicates that no active game/session resource exists
     * for the current request context.
     * </p>
     *
     * @param ex      thrown {@link NoSuchElementException}
     * @param request current HTTP servlet request
     * @return {@link ResponseEntity} with {@link ProblemDetail}, status 404
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            final NoSuchElementException ex,
            final HttpServletRequest request) {
        return buildProblem(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                "/problems/not-found",
                request.getRequestURI()
        );
    }

    /**
     * Handles state/rule violations, maps to HTTP 422.
     * <p>
     * Used when input is syntactically valid but the action is not
     * allowed by the current domain state (e.g., attacking an already
     * attacked cell, placing a ship illegally, acting on finished game).
     * </p>
     *
     * @param ex      thrown {@link IllegalStateException}
     * @param request current HTTP servlet request
     * @return {@link ResponseEntity} with {@link ProblemDetail}, status 422
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleUnprocessable(
            final IllegalStateException ex,
            final HttpServletRequest request) {
        return buildProblem(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Unprocessable Entity",
                ex.getMessage(),
                "/problems/unprocessable-entity",
                request.getRequestURI()
        );
    }

    /**
     * Handles bean validation failures, maps to HTTP 400.
     * <p>
     * Invoked by Spring MVC when request-body validation fails during
     * argument binding (e.g., when using {@code @Valid} on DTO fields).
     * </p>
     *
     * @param ex      thrown {@link MethodArgumentNotValidException}
     * @param headers response headers prepared by Spring
     * @param status  suggested HTTP status from framework
     * @param request current web request context
     * @return {@link ResponseEntity} with {@link ProblemDetail}, status 400
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @Nullable final MethodArgumentNotValidException ex,
            @Nullable final HttpHeaders headers,
            @Nullable final HttpStatusCode status,
            @Nullable final WebRequest request) {

        final ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("/problems/validation-failed"));
        problem.setTitle("Validation Failed");
        problem.setDetail("Request validation failed");
        problem.setProperty(
                "timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Catch-all fallback for unexpected exceptions, maps to HTTP 500.
     * <p>
     * Ensures unhandled failures return a consistent error contract
     * instead of leaking stack traces or HTML error pages.
     * </p>
     *
     * @param ex      unexpected exception
     * @param request current HTTP servlet request
     * @return {@link ResponseEntity} with {@link ProblemDetail}, status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            final Exception ex,
            final HttpServletRequest request) {

        LOG.error(
                "Unhandled exception at {}",
                request.getRequestURI(), ex);
        return buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                "/problems/internal-server-error",
                request.getRequestURI()
        );
    }

    /**
     * Builds a standardized {@link ProblemDetail} payload.
     * <p>
     * Populates canonical ProblemDetail fields and appends a custom
     * {@code timestamp} property for diagnostics and log correlation.
     * </p>
     *
     * @param status       HTTP status to return
     * @param title        short human-readable error title
     * @param detail       detailed message explaining the failure
     * @param type         URI-like identifier for the error category
     * @param instancePath request URI that produced the error
     * @return response entity containing the completed ProblemDetail
     */
    private ResponseEntity<ProblemDetail> buildProblem(
            final HttpStatus status,
            final String title,
            final String detail,
            final String type,
            final String instancePath) {

        final ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(type));
        problem.setInstance(URI.create(instancePath));
        problem.setProperty(
                "timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.status(status).body(problem);
    }
}