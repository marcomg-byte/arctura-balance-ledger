package com.arctura.balance_ledger.interfaces.rest.exception;

import com.arctura.balance_ledger.domain.exception.DomainException;
import com.arctura.balance_ledger.domain.exception.InsufficientFundsException;
import com.arctura.balance_ledger.domain.exception.TransactionAlreadyCancelledException;
import com.arctura.balance_ledger.domain.exception.TransactionNotFoundException;
import com.arctura.balance_ledger.interfaces.rest.error.ErrorResponse;
import com.arctura.balance_ledger.interfaces.rest.exception.request.RequestException;
import com.arctura.balance_ledger.interfaces.rest.exception.response.ConflictException;
import com.arctura.balance_ledger.interfaces.rest.exception.response.ResponseException;
import com.arctura.balance_ledger.interfaces.rest.exception.response.ServiceUnavailableException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

import com.arctura.balance_ledger.domain.exception.AccountNotFoundException;

/**
 * Centralizes exception-to-response mapping so controllers can throw typed
 * application exceptions and still return consistent error payloads.
 *
 * <p>The handler maps domain failures, request validation failures, framework
 * routing/parsing errors, and unexpected exceptions to the shared
 * {@link ErrorResponse} contract.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String CORRELATION_HEADER = "X-Correlation-Id";

  /**
   * Handles request-shape errors raised directly by controllers.
   *
   * @param exception request exception containing an API error code
   * @param request current servlet request
   * @return HTTP 400 response with the shared error payload
   */
  @ExceptionHandler(RequestException.class)
  public ResponseEntity<ErrorResponse> handleRequestException(
    RequestException exception,
    HttpServletRequest request
  ) {
    return this.buildResponse(exception, request, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles requests that do not match any registered endpoint.
   *
   * @param exception framework exception describing the missing route
   * @param request current servlet request
   * @return HTTP 404 response with endpoint and correlation information
   */
  @ExceptionHandler({
    NoHandlerFoundException.class,
    NoResourceFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleRouteNotFound(
    Exception exception,
    HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.NOT_FOUND;

    ErrorResponse response = new ErrorResponse(
      Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      String.format("No endpoint found for %s %s", request.getMethod(), request.getRequestURI()),
      request.getRequestURI(),
      "PATH_NOT_FOUND",
      request.getHeader(CORRELATION_HEADER)
    );

    return new ResponseEntity<>(response, status);
  }

  /**
   * Handles business-rule exceptions raised by the domain or application layer.
   *
   * @param exception domain exception containing a stable domain error code
   * @param request current servlet request
   * @return response whose HTTP status is derived from the exception type
   */
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomainException(
    DomainException exception,
    HttpServletRequest request
  ) {
    HttpStatus status = mapDomainStatus(exception);
    log.atWarn()
      .addKeyValue("exceptionType", exception.getClass().getSimpleName())
      .addKeyValue("errorCode", exception.getErrorCode())
      .addKeyValue("path", request.getRequestURI())
      .log("Domain exception");

    return buildResponse(exception, request, status);
  }

  /**
   * Handles interface-layer conflict exceptions.
   *
   * @param exception conflict exception containing API error details
   * @param request current servlet request
   * @return HTTP 409 response with the shared error payload
   */
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
    ConflictException exception,
    HttpServletRequest request
  ) {
    return this.buildResponse(exception, request, HttpStatus.CONFLICT);
  }

  /**
   * Handles temporary dependency or service availability failures.
   *
   * @param exception service-unavailable exception containing API error details
   * @param request current servlet request
   * @return HTTP 503 response with the shared error payload
   */
  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleServiceUnavailable(
    ServiceUnavailableException exception,
    HttpServletRequest request
  ) {
    return this.buildResponse(exception, request, HttpStatus.SERVICE_UNAVAILABLE);
  }

  /**
   * Handles bean validation failures raised by Spring MVC.
   *
   * @param exception validation exception containing field-level errors
   * @param request current servlet request
   * @return HTTP 400 response with a joined validation message
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
    MethodArgumentNotValidException exception,
    HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String correlationId = request.getHeader(CORRELATION_HEADER);

    String message = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .collect(Collectors.joining("; "));

    log.atInfo()
      .addKeyValue("path", request.getRequestURI())
      .addKeyValue("errorCode", "VALIDATION_ERROR")
      .log("Validation failure: {}", message);

    ErrorResponse resp = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        "VALIDATION_ERROR",
        correlationId);
    return new ResponseEntity<>(resp, status);
  }

  /**
   * Handles malformed JSON and enum/value conversion failures in request
   * bodies.
   *
   * @param exception framework exception raised while reading the request body
   * @param request current servlet request
   * @return HTTP 400 response using the malformed-body error code
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableMessage(
    HttpMessageNotReadableException exception,
    HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String correlationId = request.getHeader(CORRELATION_HEADER);

    log.atInfo()
      .addKeyValue("path", request.getRequestURI())
      .addKeyValue("errorCode", "MALFORMED_REQUEST_BODY")
      .log("Malformed request body");

    ErrorResponse response = new ErrorResponse(
      Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      "Request body is malformed or contains invalid values",
      request.getRequestURI(),
      "MALFORMED_REQUEST_BODY",
      correlationId
    );

    return new ResponseEntity<>(response, status);
  }

  /**
   * Handles requests that target an existing route with an unsupported HTTP
   * method.
   *
   * @param exception framework exception describing the unsupported method
   * @param request current servlet request
   * @return HTTP 405 response with the shared error payload
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
    HttpRequestMethodNotSupportedException exception,
    HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
    String correlationId = request.getHeader(CORRELATION_HEADER);

    log.atInfo()
      .addKeyValue("path", request.getRequestURI())
      .addKeyValue("method", request.getMethod())
      .addKeyValue("errorCode", "METHOD_NOT_ALLOWED")
      .log("HTTP method not allowed");

    ErrorResponse response = new ErrorResponse(
      Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      exception.getMessage(),
      request.getRequestURI(),
      "METHOD_NOT_ALLOWED",
      correlationId
    );

    return new ResponseEntity<>(response, status);
  }

  /**
   * Maps domain exception types to the HTTP status that best represents the
   * business failure.
   *
   * @param exception domain exception to classify
   * @return HTTP status for the API response
   */
  private HttpStatus mapDomainStatus(DomainException exception) {
    if (exception instanceof AccountNotFoundException || exception instanceof TransactionNotFoundException) {
        return HttpStatus.NOT_FOUND;
    }

    if (
      exception instanceof InsufficientFundsException ||
      exception instanceof TransactionAlreadyCancelledException
    ) {
        return HttpStatus.CONFLICT;
    }

    return HttpStatus.BAD_REQUEST;
  }

  /**
   * Handles unexpected exceptions without leaking internal implementation
   * details to API clients.
   *
   * @param exception unhandled exception
   * @param request current servlet request
   * @return HTTP 500 response with a generic error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(
    Exception exception,
    HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String correlationId = request.getHeader(CORRELATION_HEADER);

    log.atError()
      .addKeyValue("path", request.getRequestURI())
      .addKeyValue("errorCode", "UNKNOWN_ERROR")
      .setCause(exception)
      .log("Unexpected error");

    ErrorResponse response = new ErrorResponse(
      Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      "An unexpected error occurred",
      request.getRequestURI(),
      "UNKNOWN_ERROR",
      correlationId
    );

    return new ResponseEntity<>(response, status);
  }
  
  /**
   * Builds the shared error response for known runtime exception families.
   *
   * @param exception handled runtime exception
   * @param request current servlet request
   * @param status HTTP status to return
   * @param <Ex> handled runtime exception type
   * @return response entity containing the shared error payload
   */
  private <Ex extends RuntimeException> ResponseEntity<ErrorResponse> buildResponse(
    Ex exception,
    HttpServletRequest request,
    HttpStatus status
  ) {
    String correlationId = request.getHeader(CORRELATION_HEADER);
    String errorCode;

    if (exception instanceof RequestException requestException) {
      errorCode = requestException.getErrorCode();
    } else if(exception instanceof ResponseException responseException) {
      errorCode = responseException.getErrorCode();
    } else if(exception instanceof DomainException domainException) {
      errorCode = domainException.getErrorCode();
    } else {
      errorCode = "UNKNOWN_ERROR";
    }

    ErrorResponse response = new ErrorResponse(
      Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      exception.getMessage(),
      request.getRequestURI(),
      errorCode,
      correlationId
    );

    return new ResponseEntity<>(response, status);
  }
}
