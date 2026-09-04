package com.arctura.payment_bridge.infrastructure.logging;

/**
 * Escapes strings so custom Logback converters can safely embed values in JSON
 * fragments.
 *
 * <p>The helper is package-private because it only supports local logging
 * infrastructure and is not part of the application API.</p>
 */
final class JsonLogValueEscaper {
  /**
   * Prevents instantiation of this stateless utility class.
   */
  private JsonLogValueEscaper() {
  }

  /**
   * Escapes a string according to JSON string rules.
   *
   * @param value raw value to escape
   * @return JSON-safe escaped value without surrounding quotes
   */
  static String escape(String value) {
    StringBuilder escaped = new StringBuilder();

    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);

      switch (current) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (current < 0x20) {
            escaped.append(String.format("\\u%04x", (int) current));
          } else {
            escaped.append(current);
          }
        }
      }
    }

    return escaped.toString();
  }
}
