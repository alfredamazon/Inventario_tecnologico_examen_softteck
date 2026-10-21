
package com.example.demo.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activos")
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String folioInventario;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String numeroSerie;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String marcaModelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoActivo estado = EstadoActivo.DISPONIBLE;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoAdquisicion;

    @Column(nullable = false)
    private LocalDateTime fechaIngreso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @PrePersist
    public void antesDeGuardar() {
        if (fechaIngreso == null) {
            fechaIngreso = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public String getFolioInventario() { return folioInventario; }
    public void setFolioInventario(String folio) { this.folioInventario = folio; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String serie) { this.numeroSerie = serie; }
    public String getMarcaModelo() { return marcaModelo; }
    public void setMarcaModelo(String marca) { this.marcaModelo = marca; }
    public EstadoActivo getEstado() { return estado; }
    public void setEstado(EstadoActivo estado) { this.estado = estado; }
    public BigDecimal getCostoAdquisicion() { return costoAdquisicion; }
    public void setCostoAdquisicion(BigDecimal costo) { this.costoAdquisicion = costo; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}