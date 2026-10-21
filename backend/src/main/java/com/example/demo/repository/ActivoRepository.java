
package com.example.demo.repository;

import com.example.demo.entity.Activo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.UUID;

public interface ActivoRepository
        extends JpaRepository<Activo, UUID>,
                JpaSpecificationExecutor<Activo> {

    boolean existsByNumeroSerie(String numeroSerie);

    long countByFolioInventarioStartingWith(String prefijo);
}
