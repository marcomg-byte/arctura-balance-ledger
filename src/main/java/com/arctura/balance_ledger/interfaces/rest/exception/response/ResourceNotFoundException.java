package com.arctura.balance_ledger.interfaces.rest.exception.response;

/**
 * API-layer exception used when an addressed resource does not exist.
 *
 * <p>This response exception is available for interface-level resources that do
 * not map cleanly to a domain-specific not-found exception.</p>
 */
public class ResourceNotFoundException extends ResponseException {
  private static final String CODE = "NOT_FOUND";
  private final String resource;

  /**
   * Creates the exception for a missing API resource.
   *
   * @param resource resource name or identifier that was not found
   */
  public ResourceNotFoundException(String resource) {
    this.resource = resource;
    String message = String.format("Resource not found: %s", resource);
    super(CODE, message);
  }

  public String getResource() {
    return this.resource;
  }
}
