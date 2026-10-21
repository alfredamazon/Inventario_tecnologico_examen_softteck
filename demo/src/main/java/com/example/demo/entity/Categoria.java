

package com.example.demo.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(nullable = false, unique = true, length = 3)
    private String codigoPrefijo;

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoPrefijo() {
        return codigoPrefijo;
    }

    public void setCodigoPrefijo(String codigoPrefijo) {
        this.codigoPrefijo = codigoPrefijo;
    }
} 