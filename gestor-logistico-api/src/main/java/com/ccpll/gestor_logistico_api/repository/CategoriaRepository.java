package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.inventario.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Integer> {
    Optional<CategoriaEntity> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
