package com.arctura.payment_bridge.interfaces.rest.accounts.requests;

public record UpdatePersonalInfoRequest(
  String name,
  String paternalSurname,
  String maternalSurname
) {}
