package com.arctura.payment_bridge.interfaces.rest.exception.request;

/**
 * Raised when a request tries to mutate immutable or server-controlled fields.
 *
 * <p>Read-only property names are included in the detail field so clients can
 * remove the unsupported fields from subsequent requests.</p>
 */
public class ReadOnlyRequestBodyPropsException extends RequestException {
  private static final String CODE = "READ_ONLY_REQUEST_BODY_PROPS";

  /**
   * Creates the exception for read-only request body properties.
   *
   * @param readOnlyProps names of properties that cannot be changed
   */
  public ReadOnlyRequestBodyPropsException(String... readOnlyProps) {
    super(CODE, buildMessage(readOnlyProps), buildDetail(readOnlyProps));
  }

  /**
   * Builds a human-readable validation message for read-only properties.
   *
   * @param readOnlyProps names of properties that cannot be changed
   * @return message suitable for the API error response
   */
  private static String buildMessage(String... readOnlyProps) {
    if (readOnlyProps == null || readOnlyProps.length == 0) {
      return "Request body contains read-only properties";
    }

    if (readOnlyProps.length == 1) {
      return "Request body property '" + readOnlyProps[0] + "' is read-only";
    }

    return "Request body properties " + String.join(", ", readOnlyProps) + " are read-only";
  }

  /**
   * Builds the machine-readable detail value for read-only properties.
   *
   * @param readOnlyProps names of properties that cannot be changed
   * @return comma-separated property names, or an empty string when none are
   *         supplied
   */
  private static String buildDetail(String... readOnlyProps) {
    return readOnlyProps == null ? "" : String.join(",", readOnlyProps);
  }
}
