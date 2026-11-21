package com.ccpll.gestor_logistico_api.service;

import java.util.List;
import java.util.Map;

import com.ccpll.gestor_logistico_api.dto.DashboardKpiDTO;
import com.ccpll.gestor_logistico_api.dto.SolicitudesEstadoDTO;

public interface DashboardService {
    /**
     * Obtiene los indicadores clave (KPIs) del encabezado superior:
     * Inventario valorizado, unidades disponibles, productos por reponer, etc.
     */
    DashboardKpiDTO obtenerKpisGlobales();

    /**
     * Obtiene la distribución de solicitudes por estado para el gráfico de dona.
     */
    SolicitudesEstadoDTO obtenerResumenSolicitudes();

    List<Map<String, Object>> obtenerValorPorCategoria();

    List<Map<String, Object>> obtenerMovimientosRecientes();

    List<Map<String, Object>> obtenerMovimientosMensualesReales();
}
