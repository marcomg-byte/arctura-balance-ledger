package com.arctura.payment_bridge.infrastructure.logging;

final class JsonLogValueEscaper {
  private JsonLogValueEscaper() {
  }

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
