
package com.example.demo.dto;

public record LoginResponse(
    String token,
    String tokenType,
    String username,
    String rol
) {}
