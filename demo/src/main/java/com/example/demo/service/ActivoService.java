
package com.example.demo.service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.ActivoRequestDTO;
import com.example.demo.dto.ActivoResponseDTO;
import com.example.demo.dto.CategoriaDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.ActivoRepository;
import com.example.demo.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
@Service
public class ActivoService {

    private final ActivoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public ActivoService(
            ActivoRepository repository,
            CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }
    @Transactional(readOnly = true)
    public List<ActivoResponseDTO> listar() {
        return repository.findAll().stream()
                .map(this::convertir)
                .toList();
    }
    @Transactional(readOnly = true)
    public Page<ActivoResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::convertir);
    }
    @Transactional(readOnly = true)
    public ActivoResponseDTO buscar(UUID id) {
        return convertir(obtenerActivo(id));
    }

    @Transactional
    public ActivoResponseDTO guardar(ActivoRequestDTO dto) {
        if (repository.existsByNumeroSerie(dto.numeroSerie())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Número de serie duplicado");
        }

        Categoria categoria = categoriaRepository
                .findById(dto.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        Activo activo = new Activo();
        activo.setNumeroSerie(dto.numeroSerie());
        activo.setMarcaModelo(dto.marcaModelo());
        activo.setCostoAdquisicion(dto.costoAdquisicion());
        activo.setCategoria(categoria);

        String prefijo = categoria.getCodigoPrefijo()
                + "-" + LocalDate.now().getYear() + "-";

        long consecutivo =
                repository.countByFolioInventarioStartingWith(prefijo) + 1;

        activo.setFolioInventario(
                prefijo + String.format("%03d", consecutivo));

        return convertir(repository.save(activo));
    }

    @Transactional
    public ActivoResponseDTO actualizar(UUID id, ActivoRequestDTO dto) {
        Activo activo = obtenerActivo(id);

        if (activo.getEstado() == EstadoActivo.BAJA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede modificar un activo dado de baja");
        }

        boolean serieUsada = repository.existsByNumeroSerie(dto.numeroSerie());

        if (serieUsada && !activo.getNumeroSerie().equals(dto.numeroSerie())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Número de serie duplicado");
        }

        Categoria categoria = categoriaRepository
                .findById(dto.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        activo.setNumeroSerie(dto.numeroSerie());
        activo.setMarcaModelo(dto.marcaModelo());
        activo.setCostoAdquisicion(dto.costoAdquisicion());
        activo.setCategoria(categoria);

        return convertir(repository.save(activo));
    }

    @Transactional
    public ActivoResponseDTO cambiarEstado(UUID id, EstadoActivo nuevoEstado) {
        Activo activo = obtenerActivo(id);

        if (activo.getEstado() == EstadoActivo.BAJA
                && nuevoEstado != EstadoActivo.BAJA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Un activo en BAJA no puede volver a un estado operativo");
        }

        activo.setEstado(nuevoEstado);
        return convertir(repository.save(activo));
    }


    public void eliminar(UUID id) {
        Activo activo = obtenerActivo(id);
        activo.setEstado(EstadoActivo.BAJA);
        repository.save(activo);
    }


    private Activo obtenerActivo(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Activo no encontrado"));
    }

    private ActivoResponseDTO convertir(Activo a) {
        return new ActivoResponseDTO(
                a.getId(),
                a.getFolioInventario(),
                a.getNumeroSerie(),
                a.getMarcaModelo(),
                a.getEstado().name(),
                a.getCostoAdquisicion(),
                a.getFechaIngreso(),
                a.getCategoria().getId(),
                a.getCategoria().getNombre());
    }
    

    
@Transactional(readOnly = true)
public Page<ActivoResponseDTO> buscarConFiltros(
        String numeroSerie,
        String marcaModelo,
        Long categoriaId,
        EstadoActivo estado,
        BigDecimal costoMin,
        BigDecimal costoMax,
        Pageable pageable) {

    return repository.findAll(
            construirFiltros(numeroSerie, marcaModelo, categoriaId,
                    estado, costoMin, costoMax),
            pageable
    ).map(this::convertir);
}

@Transactional(readOnly = true)
public List<ActivoResponseDTO> buscarTodosConFiltros(
        String numeroSerie,
        String marcaModelo,
        Long categoriaId,
        EstadoActivo estado,
        BigDecimal costoMin,
        BigDecimal costoMax) {

    return repository.findAll(
            construirFiltros(numeroSerie, marcaModelo, categoriaId,
                    estado, costoMin, costoMax),
            Sort.unsorted()
    ).stream().map(this::convertir).toList();
}

private Specification<Activo> construirFiltros(
        String numeroSerie,
        String marcaModelo,
        Long categoriaId,
        EstadoActivo estado,
        BigDecimal costoMin,
        BigDecimal costoMax) {

    return (root, query, cb) -> {
        List<Predicate> filtros = new ArrayList<>();

        if (numeroSerie != null && !numeroSerie.isBlank()) {
            filtros.add(cb.like(
                    cb.lower(root.get("numeroSerie")),
                    "%" + numeroSerie.toLowerCase() + "%"
            ));
        }

        if (marcaModelo != null && !marcaModelo.isBlank()) {
            filtros.add(cb.like(
                    cb.lower(root.get("marcaModelo")),
                    "%" + marcaModelo.toLowerCase() + "%"
            ));
        }

        if (categoriaId != null) {
            filtros.add(cb.equal(
                    root.join("categoria").get("id"), categoriaId
            ));
        }

        if (estado != null) {
            filtros.add(cb.equal(root.get("estado"), estado));
        }

        if (costoMin != null) {
            filtros.add(cb.greaterThanOrEqualTo(
                    root.get("costoAdquisicion"), costoMin
            ));
        }

        if (costoMax != null) {
            filtros.add(cb.lessThanOrEqualTo(
                    root.get("costoAdquisicion"), costoMax
            ));
        }

        return cb.and(filtros.toArray(new Predicate[0]));
    };
}

}