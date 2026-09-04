package com.arctura.balance_ledger.interfaces.rest.exception.response;

/**
 * API-layer exception used when a downstream dependency or service is
 * temporarily unavailable.
 *
 * <p>The exception maps to HTTP 503 and can include the unavailable dependency
 * name as diagnostic detail.</p>
 */
public class ServiceUnavailableException extends ResponseException {
  private static final String CODE = "SERVICE_UNAVAILABLE";

  /**
   * Creates a service-unavailable exception.
   *
   * @param message human-readable availability message
   */
  public ServiceUnavailableException(String message) {
    super(CODE, message);
  }

  /**
   * Creates a service-unavailable exception with diagnostic detail.
   *
   * @param message human-readable availability message
   * @param detail detail value included in the API error model
   */
  public ServiceUnavailableException(String message, String detail) {
    super(CODE, message, detail);
  }

  /**
   * Builds a service-unavailable exception for an unreachable downstream
   * dependency.
   *
   * @param serviceName downstream service name
   * @return service-unavailable exception containing the dependency name
   */
  public static ServiceUnavailableException downstream(String serviceName) {
    String message = String.format("Unable to reach downstream service %s", serviceName);
    return new ServiceUnavailableException(message, serviceName);
  }
}
