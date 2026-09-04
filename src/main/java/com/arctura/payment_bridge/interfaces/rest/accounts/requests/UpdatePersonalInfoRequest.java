package com.arctura.payment_bridge.interfaces.rest.accounts.requests;

/**
 * Request payload for replacing an account holder's personal information.
 *
 * @param name account holder replacement given name
 * @param paternalSurname account holder replacement paternal surname
 * @param maternalSurname account holder replacement maternal surname
 */
public record UpdatePersonalInfoRequest(
  String name,
  String paternalSurname,
  String maternalSurname
) {}
