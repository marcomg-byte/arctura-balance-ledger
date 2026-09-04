package com.arctura.payment_bridge.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;
import org.slf4j.event.KeyValuePair;

import java.util.List;

/**
 * Renders SLF4J key-value logging pairs as a JSON object in local log output.
 *
 * <p>Values are serialized with simple JSON-compatible formatting so structured
 * logging calls remain readable in local development output.</p>
 */
public class LocalFieldsConverter extends DynamicConverter<ILoggingEvent> {
  /**
   * Converts SLF4J key-value pairs from the logging event into a JSON fragment.
   *
   * @param event Logback logging event
   * @return JSON fragment containing structured fields
   */
  @Override
  public String convert(ILoggingEvent event) {
    List<KeyValuePair> pairs = event.getKeyValuePairs();

    if (pairs == null || pairs.isEmpty()) {
      return "[{\"fields\":{}}]";
    }

    StringBuilder json = new StringBuilder("[{\"fields\":{");

    for (int index = 0; index < pairs.size(); index++) {
      KeyValuePair pair = pairs.get(index);

      if (index > 0) {
        json.append(',');
      }

      json.append('"')
        .append(JsonLogValueEscaper.escape(pair.key))
        .append("\":")
        .append(formatValue(pair.value));
    }

    json.append("}}]");
    return json.toString();
  }

  /**
   * Formats a structured logging value as JSON.
   *
   * @param value value supplied through SLF4J key-value logging
   * @return JSON representation for the value
   */
  private String formatValue(Object value) {
    if (value == null) {
      return "null";
    }

    if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }

    return "\"" + JsonLogValueEscaper.escape(value.toString()) + "\"";
  }
}
