package com.ccpll.gestor_logistico_api.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ccpll.gestor_logistico_api.entity.inventario.InventarioEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardRepository extends JpaRepository<InventarioEntity, Integer> {

    // 1. KPI: Valor Total
    @Query(value = "SELECT valor_total_inventario FROM vista_kpi_valor_total", nativeQuery = true)
    BigDecimal getValorTotalInventario();

    // 2. KPI: Artículos bajo stock
    @Query(value = "SELECT articulos_bajo_stock FROM vista_kpi_bajo_stock", nativeQuery = true)
    Long getArticulosBajoStock();

    // 3. Gráfico: Valor por Categoría
    @Query(value = "SELECT categoria, valor_total FROM vista_valor_por_categoria", nativeQuery = true)
    List<Map<String, Object>> getValorPorCategoria();

    // 4. Tabla: Últimos 10 movimientos del Kardex
    @Query(value = "SELECT * FROM vista_movimientos_recientes", nativeQuery = true)
    List<Map<String, Object>> getUltimosMovimientos();

    @Query(value = "SELECT mes_nombre AS mes, total_entradas AS entradas, total_salidas AS salidas FROM vista_movimientos_mensuales", nativeQuery = true)
List<Map<String, Object>> getMovimientosMensualesReales();
}