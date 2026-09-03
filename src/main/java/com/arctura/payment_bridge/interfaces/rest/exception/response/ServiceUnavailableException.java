package com.arctura.payment_bridge.interfaces.rest.exception.response;

public class ServiceUnavailableException extends ResponseException {
  private static final String CODE = "SERVICE_UNAVAILABLE";

  public ServiceUnavailableException(String message) {
    super(CODE, message);
  }

  public ServiceUnavailableException(String message, String detail) {
    super(CODE, message, detail);
  }

  public static ServiceUnavailableException downstream(String serviceName) {
    String message = String.format("Unable to reach downstream service %s", serviceName);
    return new ServiceUnavailableException(message, serviceName);
  }
}
