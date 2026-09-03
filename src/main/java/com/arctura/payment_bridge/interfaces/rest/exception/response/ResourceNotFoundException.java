package com.arctura.payment_bridge.interfaces.rest.exception.response;

public class ResourceNotFoundException extends ResponseException {
  private static final String CODE = "NOT_FOUND";
  private final String resource;

  public ResourceNotFoundException(String resource) {
    this.resource = resource;
    String message = String.format("Resource not found: %s", resource);
    super(CODE, message);
  }

  public String getResource() {
    return this.resource;
  }
}
