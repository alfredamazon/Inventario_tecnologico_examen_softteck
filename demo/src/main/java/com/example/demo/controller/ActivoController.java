
package com.example.demo.controller;

import com.example.demo.dto.ActivoRequestDTO;
import com.example.demo.dto.ActivoResponseDTO;
import com.example.demo.dto.ReporteResponse;
import com.example.demo.entity.EstadoActivo;
import com.example.demo.service.ActivoService;
import com.example.demo.service.ReporteService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/api/activos")
public class ActivoController {

    private final ActivoService service;
    private final ReporteService reporteService;

    public ActivoController(
            ActivoService service,
            ReporteService reporteService) {
        this.service = service;
        this.reporteService = reporteService;
    }

    @GetMapping
    public List<ActivoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/pagina")
    public Page<ActivoResponseDTO> listarPagina(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    public ActivoResponseDTO buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @GetMapping("/buscar")
    public Page<ActivoResponseDTO> buscarConFiltros(
            @RequestParam(required = false) String numeroSerie,
            @RequestParam(required = false) String marcaModelo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoActivo estado,
            @RequestParam(required = false) BigDecimal costoMin,
            @RequestParam(required = false) BigDecimal costoMax,
            @PageableDefault(size = 5) Pageable pageable) {

        return service.buscarConFiltros(
                numeroSerie,
                marcaModelo,
                categoriaId,
                estado,
                costoMin,
                costoMax,
                pageable
        );
    }

    @GetMapping("/reportes/zip")
    public ReporteResponse exportarReporte(
            @RequestParam(required = false) String numeroSerie,
            @RequestParam(required = false) String marcaModelo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoActivo estado,
            @RequestParam(required = false) BigDecimal costoMin,
            @RequestParam(required = false) BigDecimal costoMax)
            throws IOException {

        return reporteService.generar(
                numeroSerie,
                marcaModelo,
                categoriaId,
                estado,
                costoMin,
                costoMax
        );
    }



    @PostMapping
    public ActivoResponseDTO guardar(
            @Valid @RequestBody ActivoRequestDTO dto) {
        return service.guardar(dto);
    }

    @PutMapping("/{id}")
    public ActivoResponseDTO actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActivoRequestDTO dto) {
        return service.actualizar(id, dto);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ActivoResponseDTO cambiarEstado(
            @PathVariable UUID id,
            @RequestParam EstadoActivo estado) {
        return service.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id);
    }
}
