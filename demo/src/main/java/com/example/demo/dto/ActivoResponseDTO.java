
package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ActivoResponseDTO(
    UUID id,
    String folioInventario,
    String numeroSerie,
    String marcaModelo,
    String estado,
    BigDecimal costoAdquisicion,
    LocalDateTime fechaIngreso,
    Long categoriaId,
    String categoriaNombre
) {}