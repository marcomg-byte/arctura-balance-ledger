package com.arctura.balance_ledger.interfaces.rest.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * Uniform JSON payload returned for exceptions handled by the REST exception
 * layer.
 *
 * <p>Every handled error response includes a timestamp, HTTP status metadata, a
 * human-readable message, a stable application error code, and the request
 * correlation id when one is available.</p>
 *
 * @param timestamp instant when the error response was created
 * @param status HTTP status code
 * @param error HTTP reason phrase
 * @param message human-readable error description
 * @param path request path that caused the error
 * @param errorCode stable application-specific error code
 * @param correlationId request-scoped correlation id from
 *                      {@code X-Correlation-Id}
 */
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String errorCode,
        String correlationId
) {}
