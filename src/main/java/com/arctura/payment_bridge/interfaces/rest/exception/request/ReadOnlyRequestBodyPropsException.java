package com.arctura.payment_bridge.interfaces.rest.exception.request;

public class ReadOnlyRequestBodyPropsException extends RequestException {
  private static final String CODE = "READ_ONLY_REQUEST_BODY_PROPS";

  public ReadOnlyRequestBodyPropsException(String... readOnlyProps) {
    super(CODE, buildMessage(readOnlyProps), buildDetail(readOnlyProps));
  }

  private static String buildMessage(String... readOnlyProps) {
    if (readOnlyProps == null || readOnlyProps.length == 0) {
      return "Request body contains read-only properties";
    }

    if (readOnlyProps.length == 1) {
      return "Request body property '" + readOnlyProps[0] + "' is read-only";
    }

    return "Request body properties " + String.join(", ", readOnlyProps) + " are read-only";
  }

  private static String buildDetail(String... readOnlyProps) {
    return readOnlyProps == null ? "" : String.join(",", readOnlyProps);
  }
}
