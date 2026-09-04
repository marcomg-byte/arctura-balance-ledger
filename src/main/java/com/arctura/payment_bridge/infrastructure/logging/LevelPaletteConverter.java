package com.arctura.payment_bridge.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

/**
 * Applies role-aware ANSI colors to local Logback output based on the log level.
 *
 * <p>The converter is configured from Logback patterns and receives a role
 * option, such as level, pid, logger, fields, correlation, or message, to choose
 * a color that keeps local logs readable.</p>
 */
public class LevelPaletteConverter extends CompositeConverter<ILoggingEvent> {
  private static final String RESET = "\u001B[0;39m";

  /**
   * Wraps a rendered log pattern fragment in an ANSI color sequence.
   *
   * @param event Logback logging event
   * @param input rendered fragment from the nested pattern
   * @return colored fragment with a trailing reset sequence
   */
  @Override
  protected String transform(ILoggingEvent event, String input) {
    String role = getFirstOption();
    String level = event.getLevel().toString();

    return colorFor(level, role) + input + RESET;
  }

  /**
   * Selects the ANSI color for a log level and output role.
   *
   * @param level log level name
   * @param role configured output role
   * @return ANSI foreground color sequence
   */
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

  /**
   * Selects the local color palette for INFO events.
   *
   * @param role configured output role
   * @return ANSI foreground color sequence
   */
  private String infoColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(147);
      case "pid", "correlation" -> ansi256(75);
      case "logger", "fields" -> ansi256(87);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  /**
   * Selects the local color palette for WARN events.
   *
   * @param role configured output role
   * @return ANSI foreground color sequence
   */
  private String warnColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(214);
      case "pid", "correlation" -> ansi256(75);
      case "logger", "fields" -> ansi256(80);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  /**
   * Selects the local color palette for ERROR events.
   *
   * @param role configured output role
   * @return ANSI foreground color sequence
   */
  private String errorColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(203);
      case "pid", "correlation" -> ansi256(51);
      case "logger", "fields" -> ansi256(87);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  /**
   * Selects the local color palette for DEBUG events.
   *
   * @param role configured output role
   * @return ANSI foreground color sequence
   */
  private String debugColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(250);
      case "pid", "correlation" -> ansi256(245);
      case "logger", "fields" -> ansi256(153);
      case "message" -> ansi256(255);
      default -> ansi256(250);
    };
  }

  /**
   * Selects the local color palette for TRACE events.
   *
   * @param role configured output role
   * @return ANSI foreground color sequence
   */
  private String traceColor(String role) {
    return switch (roleOrDefault(role)) {
      case "level" -> ansi256(252);
      case "pid", "correlation" -> ansi256(247);
      case "logger", "fields" -> ansi256(159);
      case "message" -> ansi256(255);
      default -> ansi256(252);
    };
  }

  /**
   * Normalizes a missing or blank Logback option into the default role.
   *
   * @param role configured output role
   * @return supplied role or {@code default}
   */
  private String roleOrDefault(String role) {
    return role == null || role.isBlank() ? "default" : role;
  }

  /**
   * Builds an ANSI 256-color foreground escape sequence.
   *
   * @param color 256-color palette index
   * @return ANSI foreground color sequence
   */
  private static String ansi256(int color) {
    return "\u001B[38;5;" + color + "m";
  }
}
