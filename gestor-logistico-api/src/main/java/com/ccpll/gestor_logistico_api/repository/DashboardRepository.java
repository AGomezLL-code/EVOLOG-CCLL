package com.ccpll.gestor_logistico_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.ccpll.gestor_logistico_api.entity.inventario.InventarioEntity;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardRepository extends JpaRepository<InventarioEntity, Integer> {

    // 1. KPI: Valor Total
    @Query(value = "SELECT COALESCE(SUM(stock_actual * costo_promedio), 0) FROM inventario WHERE activo = 1", nativeQuery = true)
    BigDecimal getValorTotalInventario();

    // 2. KPI: Artículos bajo stock
    @Query(value = "SELECT COUNT(*) FROM inventario WHERE activo = 1 AND stock_actual <= stock_minimo", nativeQuery = true)
    Long getArticulosBajoStock();

    // 3. Gráfico: Valor por Categoría
    @Query(value = """
            SELECT c.nombre AS categoria,
                   COALESCE(SUM(i.stock_actual * i.costo_promedio), 0) AS valor_total
            FROM inventario i
            INNER JOIN categorias c ON i.categoria_id = c.id
            WHERE i.activo = 1
            GROUP BY c.id, c.nombre
            HAVING COALESCE(SUM(i.stock_actual * i.costo_promedio), 0) > 0
            ORDER BY valor_total DESC
            """, nativeQuery = true)
    List<Object[]> getValorPorCategoria();

    // 4. Tabla: Últimos 10 movimientos del Kardex
    @Query(value = """
            SELECT h.fecha AS fecha,
                   i.codigo AS codigo,
                   i.descripcion AS descripcion,
                   h.tipo AS tipo,
                   h.cantidad AS cantidad,
                   h.costo_unitario AS costo_unitario,
                   h.subtotal AS subtotal,
                   COALESCE(h.area, h.proveedor_cliente, 'ALMACÉN CENTRAL') AS area
            FROM historial_inventario h
            INNER JOIN inventario i ON h.articulo_id = i.id
            ORDER BY h.fecha DESC, h.id DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> getUltimosMovimientos();

    // 5. Gráfico de Barras: Entradas vs Salidas por mes real
    @Query(value = """
            SELECT DATE_FORMAT(h.fecha, '%Y-%m') AS mes,
                   COALESCE(SUM(CASE WHEN h.tipo = 'ENTRADA' THEN h.cantidad ELSE 0 END), 0) AS entradas,
                   COALESCE(SUM(CASE WHEN h.tipo = 'SALIDA' THEN h.cantidad ELSE 0 END), 0) AS salidas
            FROM historial_inventario h
            GROUP BY DATE_FORMAT(h.fecha, '%Y-%m')
            ORDER BY DATE_FORMAT(h.fecha, '%Y-%m') ASC
            LIMIT 12
            """, nativeQuery = true)
    List<Object[]> getMovimientosMensualesReales();
}