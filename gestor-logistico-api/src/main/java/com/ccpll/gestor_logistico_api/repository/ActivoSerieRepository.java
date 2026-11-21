package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.inventario.ActivoSerieEntity;
import com.ccpll.gestor_logistico_api.entity.inventario.ActivoSerieEntity.EstadoActivo;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;


public interface ActivoSerieRepository extends JpaRepository<ActivoSerieEntity, Integer> {

    // Obtener todas las series/SKUs asociadas a un producto
    List<ActivoSerieEntity> findByProductoId(Integer productoId);

    // Obtener series filtradas por estado (ej: DISPONIBLE para préstamos)
    List<ActivoSerieEntity> findByProductoIdAndEstado(Integer productoId, ActivoSerieEntity.EstadoActivo estado);

    // Buscar una serie específica por su SKU
    Optional<ActivoSerieEntity> findBySku(String sku);

    // Verificar si un SKU ya existe en la base de datos
    boolean existsBySku(String sku);

    long countByProductoId(Integer productoId);

    long countByProductoIdAndEstado(Integer productoId, EstadoActivo estado);
}