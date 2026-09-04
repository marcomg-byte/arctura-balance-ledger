package com.arctura.payment_bridge.interfaces.rest.exception;

import com.arctura.payment_bridge.domain.exception.DomainException;
import com.arctura.payment_bridge.domain.exception.InsufficientFundsException;
import com.arctura.payment_bridge.domain.exception.TransactionNotFoundException;
import com.arctura.payment_bridge.interfaces.rest.error.ErrorResponse;
import com.arctura.payment_bridge.interfaces.rest.exception.request.RequestException;
import com.arctura.payment_bridge.interfaces.rest.exception.response.ConflictException;
import com.arctura.payment_bridge.interfaces.rest.exception.response.ResponseException;
import com.arctura.payment_bridge.interfaces.rest.exception.response.ServiceUnavailableException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

import com.arctura.payment_bridge.domain.exception.AccountNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String CORRELATION_HEADER = "X-Correlation-Id";

  @ExceptionHandler(RequestException.class)
  public ResponseEntity<ErrorResponse> handleRequestException(
    RequestException exception,
    HttpServletRequest request
  ) {
    return this.buildResponse(exception, request, HttpStatus.BAD_REQUEST);
  }

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

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
    ConflictException exception,
    HttpServletRequest request
  ) {
    return this.buildResponse(exception, request, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleServiceUnavailable(
    ServiceUnavailableException exception,
    HttpServletRequest request
  ) {
    return this.buildResponse(exception, request, HttpStatus.SERVICE_UNAVAILABLE);
  }

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

  private HttpStatus mapDomainStatus(DomainException exception) {
    if (exception instanceof AccountNotFoundException || exception instanceof TransactionNotFoundException) {
        return HttpStatus.NOT_FOUND;
    }

    if (exception instanceof InsufficientFundsException) {
        return HttpStatus.CONFLICT;
    }

    return HttpStatus.BAD_REQUEST;
  }

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
