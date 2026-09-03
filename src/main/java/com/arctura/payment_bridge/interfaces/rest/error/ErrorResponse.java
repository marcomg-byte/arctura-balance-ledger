package com.arctura.payment_bridge.interfaces.rest.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * Uniform JSON payload returned for any exception handled by
 * {@link com.arctura.payment_bridge.interfaces.rest.exception.GlobalExceptionHandler}.
 *
 * Fields:
 *  • timestamp      – when the error occurred (ISO‑8601 string)
 *  • status         – HTTP status code (e.g. 400, 404, 409, 503)
 *  • error          – standard reason phrase for the status
 *  • message        – human‑readable description (exception message)
 *  • path           – request URI that caused the error
 *  • errorCode      – application‑specific code (e.g. ACCOUNT_NOT_FOUND)
 *  • correlationId  – request‑scoped ID from {@code X‑Correlation‑Id}
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
