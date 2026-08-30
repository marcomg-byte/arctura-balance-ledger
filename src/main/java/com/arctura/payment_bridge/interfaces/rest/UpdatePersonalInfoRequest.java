package com.arctura.payment_bridge.interfaces.rest;

public record UpdatePersonalInfoRequest(
  String name,
  String paternalSurname,
  String maternalSurname
) {}
