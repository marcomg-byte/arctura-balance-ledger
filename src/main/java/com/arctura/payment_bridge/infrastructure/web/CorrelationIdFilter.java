package com.arctura.payment_bridge.infrastructure.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that propagates or creates a correlation id for each request
 * and attaches it to logs and responses.
 *
 * <p>The filter stores the correlation id in the SLF4J mapped diagnostic context
 * for the duration of the request, writes it back to the response header, and
 * logs request start/completion metadata.</p>
 */
@Component
public class CorrelationIdFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
  private static final String HEADER = "X-Correlation-Id";

  /**
   * Applies correlation id propagation and request logging around the servlet
   * filter chain.
   *
   * @param req servlet request
   * @param res servlet response
   * @param chain remaining filter chain
   * @throws IOException when downstream request processing fails with I/O errors
   * @throws ServletException when downstream request processing fails
   */
  @Override
  public void doFilter(
    ServletRequest req,
    ServletResponse res,
    FilterChain chain
  ) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    String correlationId = request.getHeader(HEADER);

    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }

    MDC.put("correlationId", correlationId);
    response.setHeader(HEADER, correlationId);

    log.atInfo()
      .addKeyValue("method", request.getMethod())
      .addKeyValue("path", request.getRequestURI())
      .log("HTTP request received");

    long startNanos = System.nanoTime();

    try {
      chain.doFilter(request, response);
    } finally {
      long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

      log.atInfo()
        .addKeyValue("method", request.getMethod())
        .addKeyValue("path", request.getRequestURI())
        .addKeyValue("status", response.getStatus())
        .addKeyValue("durationMs", durationMs)
        .log("HTTP request completed");

      MDC.remove("correlationId");
    }
  }
}
