
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaDTO(

    Long id,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe superar 100 caracteres")
    String nombre,

    @NotBlank(message = "El prefijo es obligatorio")
    @Pattern(
        regexp = "[A-Z]{3}",
        message = "El prefijo debe tener exactamente 3 letras mayúsculas"
    )
    String codigoPrefijo

) {}
