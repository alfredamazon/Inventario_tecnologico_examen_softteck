
package com.example.demo.dto;

public record ReporteResponse(
        int status,
        String message,
        String fileName,
        String fileBase64
) {}
