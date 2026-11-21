package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RequerimientoRepository extends JpaRepository<RequerimientoEntity, Integer> {

    @Query("SELECT DISTINCT r FROM RequerimientoEntity r LEFT JOIN FETCH r.detalles d LEFT JOIN FETCH d.articulo ORDER BY r.id DESC")
    List<RequerimientoEntity> findAllConDetalles();

    @Query("SELECT r FROM RequerimientoEntity r LEFT JOIN FETCH r.detalles d LEFT JOIN FETCH d.articulo WHERE r.id = :id")
    Optional<RequerimientoEntity> findByIdConDetalles(@Param("id") Integer id);

    Long countByEstado(RequerimientoEntity.EstadoRequerimiento estado);

    @Query("SELECT COUNT(r) FROM RequerimientoEntity r WHERE r.estado = :estado")
Long contarPorEstado(@Param("estado") RequerimientoEntity.EstadoRequerimiento estado);

Long countByEstadoIn(Collection<RequerimientoEntity.EstadoRequerimiento> estados);

@Query("SELECT DISTINCT r FROM RequerimientoEntity r LEFT JOIN FETCH r.detalles d LEFT JOIN FETCH d.articulo WHERE r.creadoPor = :username ORDER BY r.id DESC")
List<RequerimientoEntity> findByCreadoPorConDetalles(@Param("username") String username);
}
