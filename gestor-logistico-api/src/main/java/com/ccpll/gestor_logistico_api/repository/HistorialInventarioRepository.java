package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.inventario.HistorialInventarioEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface HistorialInventarioRepository extends JpaRepository<HistorialInventarioEntity, Integer> {
List<HistorialInventarioEntity> findByArticuloIdOrderByFechaDescFechaCreacionDesc(Integer articuloId);

// Consulta con rango de fechas para reportes detallados
    @Query("SELECT h FROM HistorialInventarioEntity h JOIN FETCH h.articulo " +
           "WHERE h.articulo.id = :articuloId " +
           "AND (:fechaInicio IS NULL OR h.fecha >= :fechaInicio) " +
           "AND (:fechaFin IS NULL OR h.fecha <= :fechaFin) " +
           "ORDER BY h.fecha DESC, h.fechaCreacion DESC")
    List<HistorialInventarioEntity> obtenerKardexPorProductoYRangoFechas(
            @Param("articuloId") Integer articuloId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    List<HistorialInventarioEntity> findByFechaBetweenOrderByIdDesc(LocalDate fechaInicio, LocalDate fechaFin);
}
