package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.dto.*;
import com.ccpll.gestor_logistico_api.service.DashboardService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpiDTO> getKpis() {
        return ResponseEntity.ok(dashboardService.obtenerKpisGlobales());
    }

    @GetMapping("/solicitudes-resumen")
    public ResponseEntity<SolicitudesEstadoDTO> getSolicitudesResumen() {
        return ResponseEntity.ok(dashboardService.obtenerResumenSolicitudes());
    }

    @GetMapping("/valor-categoria")
    public ResponseEntity<List<Map<String, Object>>> getValorPorCategoria() {
        return ResponseEntity.ok(dashboardService.obtenerValorPorCategoria());
    }

    @GetMapping("/movimientos-recientes")
    public ResponseEntity<List<Map<String, Object>>> getMovimientosRecientes() {
        return ResponseEntity.ok(dashboardService.obtenerMovimientosRecientes());
    }

    @GetMapping("/movimientos-mensuales")
public ResponseEntity<List<Map<String, Object>>> getMovimientosMensuales() {
    return ResponseEntity.ok(dashboardService.obtenerMovimientosMensualesReales());
}
}