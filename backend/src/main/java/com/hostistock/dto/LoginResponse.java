package com.hostistock.dto;

public record LoginResponse(
    String token,
    Long barId,
    String nombreBar
) {}