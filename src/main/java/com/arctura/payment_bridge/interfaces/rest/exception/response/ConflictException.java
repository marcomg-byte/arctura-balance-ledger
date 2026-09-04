package com.arctura.payment_bridge.interfaces.rest.exception.response;

/**
 * API-layer exception used when a request conflicts with the current state of a
 * resource.
 *
 * <p>The exception can also carry the entity and identifier that caused a
 * duplicate-resource conflict.</p>
 */
public class ConflictException extends ResponseException {
  private static final String CODE = "CONFLICT";
  private final String identifier;
  private final String entity;

  /**
   * Creates a generic conflict response exception.
   *
   * @param message human-readable conflict message
   */
  public ConflictException(String message) {
    super(CODE, message);
    this.entity = null;
    this.identifier = null;
  }
  
  /**
   * Creates a conflict response exception with diagnostic detail.
   *
   * @param message human-readable conflict message
   * @param detail detail value included in the API error model
   */
  public ConflictException(String message, String detail) {
    super(CODE, message, detail);
    this.entity = null;
    this.identifier = null;
  }

  /**
   * Creates a conflict response exception tied to a specific entity identifier.
   *
   * @param message human-readable conflict message
   * @param entity resource type involved in the conflict
   * @param identifier resource identifier involved in the conflict
   */
  public ConflictException(String message, String entity, String identifier) {
    super(CODE, message);
    this.entity = entity;
    this.identifier = identifier;
  }

  /**
   * Builds a duplicate-resource conflict for the supplied entity and identifier.
   *
   * @param entity resource type that already exists
   * @param identifier duplicate resource identifier
   * @return conflict exception describing the duplicate resource
   */
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
