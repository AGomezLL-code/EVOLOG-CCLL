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
import java.util.HashMap;
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
        BigDecimal valorTotal = inventarioRepository.obtenerValorTotalInventario();
        Long totalArticulos = inventarioRepository.countByActivoTrue();
        Long bajoStock = inventarioRepository.contarProductosConStockBajo();

        Long pendientes = requerimientoRepository.contarPorEstado(EstadoRequerimiento.PENDIENTE);
        Long totalRequerimientos = requerimientoRepository.count();

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
                .equiposVencidos((long) porcentajeAtencion)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudesEstadoDTO obtenerResumenSolicitudes() {
        Long aprobadas = requerimientoRepository.contarPorEstado(EstadoRequerimiento.APROBADO)
                       + requerimientoRepository.contarPorEstado(EstadoRequerimiento.DESPACHADO)
                       + requerimientoRepository.contarPorEstado(EstadoRequerimiento.ENTREGADO)
                       + requerimientoRepository.contarPorEstado(EstadoRequerimiento.DEVUELTO);

        return SolicitudesEstadoDTO.builder()
                .aprobadas(aprobadas)
                .pendientes(requerimientoRepository.contarPorEstado(EstadoRequerimiento.PENDIENTE))
                .enRevision(0L)
                .rechazadas(requerimientoRepository.contarPorEstado(EstadoRequerimiento.RECHAZADO))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List< Map<String, Object> > obtenerValorPorCategoria() {
        return dashboardRepository.getValorPorCategoria().stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("categoria", row[0]);
                    m.put("valor_total", row[1]);
                    return m;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List< Map<String, Object> > obtenerMovimientosRecientes() {
        return dashboardRepository.getUltimosMovimientos().stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("fecha", row[0]);
                    m.put("codigo", row[1]);
                    m.put("descripcion", row[2]);
                    m.put("tipo", row[3]);
                    m.put("cantidad", row[4]);
                    m.put("costo_unitario", row[5]);
                    m.put("subtotal", row[6]);
                    m.put("area", row[7]);
                    return m;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List< Map<String, Object> > obtenerMovimientosMensualesReales() {
        return dashboardRepository.getMovimientosMensualesReales().stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("mes", row[0]);
                    m.put("entradas", row[1]);
                    m.put("salidas", row[2]);
                    return m;
                })
                .toList();
    }
}
