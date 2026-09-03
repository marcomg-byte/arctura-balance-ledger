package com.arctura.payment_bridge.interfaces.rest.exception.response;

public class ConflictException extends ResponseException {
  private static final String CODE = "CONFLICT";
  private final String identifier;
  private final String entity;

  public ConflictException(String message) {
    super(CODE, message);
    this.entity = null;
    this.identifier = null;
  }
  
  public ConflictException(String message, String detail) {
    super(CODE, message, detail);
    this.entity = null;
    this.identifier = null;
  }

  public ConflictException(String message, String entity, String identifier) {
    super(CODE, message);
    this.entity = entity;
    this.identifier = identifier;
  }

  public static ConflictException duplicate(String entity, String identifier) {
    String message = String.format("%s with identifier %s already exists", entity, identifier);
    return new ConflictException(message, entity, identifier);
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public String getEntity() {
    return this.entity;
  }
}
