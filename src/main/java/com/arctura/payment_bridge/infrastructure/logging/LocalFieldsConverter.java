package com.arctura.payment_bridge.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;
import org.slf4j.event.KeyValuePair;

import java.util.List;

public class LocalFieldsConverter extends DynamicConverter<ILoggingEvent> {
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
