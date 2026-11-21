package com.ccpll.gestor_logistico_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity.EstadoRequerimiento;
import com.ccpll.gestor_logistico_api.dto.DashboardKpiDTO;
import com.ccpll.gestor_logistico_api.dto.SolicitudesEstadoDTO;
import com.ccpll.gestor_logistico_api.repository.DashboardRepository;
import com.ccpll.gestor_logistico_api.repository.InventarioRepository;
import com.ccpll.gestor_logistico_api.repository.RequerimientoRepository;
import com.ccpll.gestor_logistico_api.service.DashboardService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final InventarioRepository inventarioRepository;
    private final RequerimientoRepository requerimientoRepository;
    private final DashboardRepository dashboardRepository;

    @Override
@Transactional(readOnly = true)
public DashboardKpiDTO obtenerKpisGlobales() {
    // 1. Obtener valores de inventario
    BigDecimal valorTotal = inventarioRepository.obtenerValorTotalInventario();
    Long totalArticulos = inventarioRepository.count();
    Long bajoStock = inventarioRepository.contarProductosConStockBajo();

    // 2. Obtener conteo de requerimientos
    Long pendientes = requerimientoRepository.contarPorEstado(EstadoRequerimiento.PENDIENTE);
    Long totalRequerimientos = requerimientoRepository.count();

    // 3. Calcular Porcentaje de Atención (% Procesados: DESPACHADO + RECHAZADO)
    Long procesados = requerimientoRepository.countByEstadoIn(
        List.of(EstadoRequerimiento.DESPACHADO, EstadoRequerimiento.RECHAZADO)
    );

    double porcentajeAtencion = 100.0;
    if (totalRequerimientos != null && totalRequerimientos > 0) {
        porcentajeAtencion = Math.round(((double) procesados / totalRequerimientos) * 100.0);
    }

    return DashboardKpiDTO.builder()
            .inventarioValorizado(valorTotal != null ? valorTotal : BigDecimal.ZERO)
            .unidadesDisponibles(totalArticulos != null ? totalArticulos : 0L)
            .solicitudesPendientes(pendientes != null ? pendientes : 0L)
            .productosPorReponer(bajoStock != null ? bajoStock : 0L)
            .equiposVencidos((long) porcentajeAtencion) // Usamos un campo para enviar el % o agregamos el campo al DTO
            .build();
}
   @Override
@Transactional(readOnly = true)
public SolicitudesEstadoDTO obtenerResumenSolicitudes() {
    // 1. Sumamos todas las solicitudes que están en flujo exitoso o atendido
    Long aprobadas = requerimientoRepository.contarPorEstado(EstadoRequerimiento.APROBADO)
                   + requerimientoRepository.contarPorEstado(EstadoRequerimiento.DESPACHADO)
                   + requerimientoRepository.contarPorEstado(EstadoRequerimiento.ENTREGADO) // <--- ESTE FALTABA PARA TU LAPTOP
                   + requerimientoRepository.contarPorEstado(EstadoRequerimiento.DEVUELTO);  // <--- PARA EQUIPOS RETORNADOS

    // 2. Construimos el DTO que espera el Frontend
    return SolicitudesEstadoDTO.builder()
            .aprobadas(aprobadas)
            .pendientes(requerimientoRepository.contarPorEstado(EstadoRequerimiento.PENDIENTE))
            .enRevision(0L) // Mantenemos 0L para compatibilidad
            .rechazadas(requerimientoRepository.contarPorEstado(EstadoRequerimiento.RECHAZADO))
            .build();
}
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerValorPorCategoria() {
        return dashboardRepository.getValorPorCategoria();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerMovimientosRecientes() {
        return dashboardRepository.getUltimosMovimientos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerMovimientosMensualesReales() {
        return dashboardRepository.getMovimientosMensualesReales();
    }
}
