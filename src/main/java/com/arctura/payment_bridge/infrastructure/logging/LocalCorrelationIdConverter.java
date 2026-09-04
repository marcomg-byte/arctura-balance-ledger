package com.arctura.payment_bridge.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;

public class LocalCorrelationIdConverter extends DynamicConverter<ILoggingEvent> {
  private static final String CORRELATION_ID = "correlationId";

  @Override
  public String convert(ILoggingEvent event) {
    String correlationId = event.getMDCPropertyMap().getOrDefault(CORRELATION_ID, "none");

    return "[{\"correlationId\":\"" + JsonLogValueEscaper.escape(correlationId) + "\"}]";
  }
}
