package com.arctura.payment_bridge.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * Renders formatted log messages as JSON-safe local log fragments.
 *
 * <p>The converter is used by Logback local patterns to keep message output
 * aligned with the structured fields emitted by companion converters.</p>
 */
public class LocalMessageConverter extends DynamicConverter<ILoggingEvent> {
  /**
   * Converts the formatted log message into a JSON fragment.
   *
   * @param event Logback logging event
   * @return JSON fragment containing the escaped message
   */
  @Override
  public String convert(ILoggingEvent event) {
    return "[{\"message\":\"" + JsonLogValueEscaper.escape(event.getFormattedMessage()) + "\"}]";
  }
}
