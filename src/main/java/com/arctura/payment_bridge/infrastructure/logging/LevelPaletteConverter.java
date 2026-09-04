package com.arctura.payment_bridge.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

public class LevelPaletteConverter extends CompositeConverter<ILoggingEvent> {
  private static final String RESET = "\u001B[0;39m";

  @Override
  protected String transform(ILoggingEvent event, String input) {
    String role = getFirstOption();
    String level = event.getLevel().toString();

    return colorFor(level, role) + input + RESET;
  }

  private String colorFor(String level, String role) {
    return switch (level) {
      case "ERROR" -> errorColor(role);
      case "WARN" -> warnColor(role);
      case "INFO" -> infoColor(role);
      case "DEBUG" -> debugColor(role);
      case "TRACE" -> traceColor(role);
      default -> ansi256(252);
    };
  }

  private String infoColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(147);
      case "pid", "correlation" -> ansi256(75);
      case "logger", "fields" -> ansi256(87);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  private String warnColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(214);
      case "pid", "correlation" -> ansi256(75);
      case "logger", "fields" -> ansi256(80);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  private String errorColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(203);
      case "pid", "correlation" -> ansi256(51);
      case "logger", "fields" -> ansi256(87);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  private String debugColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(250);
      case "pid", "correlation" -> ansi256(245);
      case "logger", "fields" -> ansi256(153);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  private String traceColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(252);
      case "pid", "correlation" -> ansi256(247);
      case "logger", "fields" -> ansi256(159);
      case "message" -> ansi256(255);
      default -> ansi256(252);
    };
  }

  private String roleOrDefault(String role) {
    return role == null || role.isBlank() ? "default" : role;
  }

  private static String ansi256(int color) {
    return "\u001B[38;5;" + color + "m";
  }
}
