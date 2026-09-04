package com.arctura.balance_ledger.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * Renders the mapped diagnostic context correlation id as a structured local
 * logging field.
 *
 * <p>The converter is wired from Logback configuration and emits a compact JSON
 * fragment for local console logs.</p>
 */
public class LocalCorrelationIdConverter extends DynamicConverter<ILoggingEvent> {
  private static final String CORRELATION_ID = "correlationId";

  /**
   * Converts the current logging event into a correlation id JSON fragment.
   *
   * @param event Logback logging event
   * @return JSON fragment containing the correlation id, or {@code none}
   */
  @Override
  public String convert(ILoggingEvent event) {
    String correlationId = event.getMDCPropertyMap().getOrDefault(CORRELATION_ID, "none");

    return "[{\"correlationId\":\"" + JsonLogValueEscaper.escape(correlationId) + "\"}]";
  }
}
