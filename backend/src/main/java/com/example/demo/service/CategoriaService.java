package com.example.demo.service;


import com.example.demo.dto.CategoriaDTO;
import com.example.demo.entity.Categoria;
import com.example.demo.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaDTO> listar() {
        return repository.findAll().stream()
                .map(c -> new CategoriaDTO(
                        c.getId(),
                        c.getNombre(),
                        c.getCodigoPrefijo()))
                .toList();
    }

    public CategoriaDTO guardar(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.nombre());
        categoria.setCodigoPrefijo(dto.codigoPrefijo());

        Categoria guardada = repository.save(categoria);

        return new CategoriaDTO(
                guardada.getId(),
                guardada.getNombre(),
                guardada.getCodigoPrefijo());
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Categoría no encontrada");
        }
        repository.deleteById(id);
    }
}