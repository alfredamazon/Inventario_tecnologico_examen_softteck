package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record ActivoRequestDTO(

    @NotBlank
    @Size(max = 80)
    String numeroSerie,

    @NotBlank
    @Size(max = 150)
    String marcaModelo,

    @NotNull
    @DecimalMin("0.00")
    BigDecimal costoAdquisicion,

    @NotNull
    Long categoriaId,

    UUID id
) {}