package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.inventario.InventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<InventarioEntity, Integer> {

    Optional<InventarioEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    // Carga ansiosa por ID para evitar LazyInitializationException en el DTO
    @Query("SELECT i FROM InventarioEntity i JOIN FETCH i.categoria WHERE i.id = :id")
    Optional<InventarioEntity> findByIdConCategoria(@Param("id") Integer id);

    List<InventarioEntity> findByCategoriaIdAndActivoTrue(Integer categoriaId);

    @Query("SELECT i FROM InventarioEntity i JOIN FETCH i.categoria WHERE i.activo = true")
    List<InventarioEntity> findByActivoTrue();

    @Query("SELECT i FROM InventarioEntity i JOIN FETCH i.categoria WHERE i.activo = true AND i.stockActual <= i.stockMinimo")
    List<InventarioEntity> findProductosConStockBajo();

    // Suma valorizada del inventario: SUM(stockActual * costoPromedio)
    @Query("SELECT COALESCE(SUM(i.stockActual * i.costoPromedio), 0) FROM InventarioEntity i WHERE i.activo = true")
    BigDecimal calcularValorTotalInventario();

    // Conteo de productos con stock bajo
    @Query("SELECT COUNT(i) FROM InventarioEntity i WHERE i.activo = true AND i.stockActual <= i.stockMinimo")
    Long contarProductosConStockBajo();

    // Conteo total de productos activos
    Long countByActivoTrue();

    @Query("SELECT SUM(i.stockActual * i.costoPromedio) FROM InventarioEntity i WHERE i.activo = true")
    BigDecimal obtenerValorTotalInventario();

    @Query("SELECT SUM(i.stockActual) FROM InventarioEntity i WHERE i.activo = true")
    Long obtenerTotalUnidades();

    @Query("SELECT COUNT(i) FROM InventarioEntity i WHERE i.activo = true AND i.stockActual <= i.stockMinimo")
    Long contarProductosPorReponer();
}
