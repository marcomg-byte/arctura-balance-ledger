package com.arctura.payment_bridge.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;

public class LocalMessageConverter extends DynamicConverter<ILoggingEvent> {
  @Override
  public String convert(ILoggingEvent event) {
    return "[{\"message\":\"" + JsonLogValueEscaper.escape(event.getFormattedMessage()) + "\"}]";
  }
}
