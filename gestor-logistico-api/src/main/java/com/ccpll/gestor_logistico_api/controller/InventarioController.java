package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.dto.*;
import com.ccpll.gestor_logistico_api.service.AuditoriaService;
import com.ccpll.gestor_logistico_api.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;

    // Abierto para todos los autenticados (Logística, Cliente Interno y Área
    // Comercial)
    @GetMapping
    public ResponseEntity<List<InventarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioService.obtenerPorId(id));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<InventarioResponseDTO>> obtenerProductosStockBajo() {
        return ResponseEntity.ok(inventarioService.obtenerProductosStockBajo());
    }

    // Endpoints de escritura: solo personal de Logística + registro automático en
    // Auditoría
    @PostMapping
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA', 'ASISTENTE', 'ROLE_ASISTENTE', 'AUXILIAR', 'ROLE_AUXILIAR')")
    public ResponseEntity<InventarioResponseDTO> crear(@Valid @RequestBody InventarioRequestDTO dto) {
        InventarioResponseDTO creado = inventarioService.crear(dto);
        boolean conSku = Boolean.TRUE.equals(creado.getRequiereSku());

        auditoriaService.registrar(
                conSku ? "CREAR_ARTICULO_SKU" : "CREAR_ARTICULO",
                "Inventario",
                creado.getId(),
                "Registró artículo " + creado.getCodigo() + ": " + creado.getDescripcion()
                        + (conSku ? " [Control por SKU]" : "")
                        + " (Stock inicial: " + creado.getStockActual() + " " + creado.getUnidad() + ")");
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/movimiento/entrada")
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA', 'ASISTENTE', 'ROLE_ASISTENTE', 'AUXILIAR', 'ROLE_AUXILIAR')")
    public ResponseEntity<InventarioResponseDTO> registrarEntrada(@Valid @RequestBody EntradaInventarioRequestDTO dto) {
        InventarioResponseDTO actualizado = inventarioService.registrarEntrada(dto);
        boolean conSku = Boolean.TRUE.equals(actualizado.getRequiereSku());

        auditoriaService.registrar(
                conSku ? "INGRESO_ACTIVOS_SKU" : "INGRESO_STOCK",
                "Inventario",
                actualizado.getId(),
                "Ingreso de " + dto.getCantidad() + " " + actualizado.getUnidad()
                        + " en " + actualizado.getCodigo() + " (" + actualizado.getDescripcion() + ")"
                        + (conSku ? " con generación de SKUs individuales" : "")
                        + ". Stock actual: " + actualizado.getStockActual());
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/movimiento/salida")
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA', 'ASISTENTE', 'ROLE_ASISTENTE', 'AUXILIAR', 'ROLE_AUXILIAR')")
    public ResponseEntity<InventarioResponseDTO> registrarSalida(@Valid @RequestBody SalidaInventarioRequestDTO dto) {
        InventarioResponseDTO actualizado = inventarioService.registrarSalida(dto);
        auditoriaService.registrar(
                "SALIDA_STOCK",
                "Inventario",
                actualizado.getId(),
                "Salida de " + dto.getCantidad() + " " + actualizado.getUnidad()
                        + " en " + actualizado.getCodigo() + " (" + actualizado.getDescripcion()
                        + "). Stock actual: " + actualizado.getStockActual());
        return ResponseEntity.ok(actualizado);
    }

    // Endpoints GET internos de Kardex, KPIs valorizados y SKUs: restringidos a
    // Logística
    @GetMapping("/{id}/kardex")
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA', 'ASISTENTE', 'ROLE_ASISTENTE', 'AUXILIAR', 'ROLE_AUXILIAR')")
    public ResponseEntity<List<HistoriaInventarioResponseDTO>> obtenerKardexPorProducto(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(inventarioService.obtenerKardexPorProducto(id, fechaInicio, fechaFin));
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA', 'ASISTENTE', 'ROLE_ASISTENTE', 'AUXILIAR', 'ROLE_AUXILIAR')")
    public ResponseEntity<ResumenInventarioDTO> obtenerResumenKPI() {
        return ResponseEntity.ok(inventarioService.obtenerResumenKPI());
    }

    @GetMapping("/{id}/skus")
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA', 'ASISTENTE', 'ROLE_ASISTENTE', 'AUXILIAR', 'ROLE_AUXILIAR')")
    public ResponseEntity<List<ActivoSerieDTO>> obtenerSkusPorProducto(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioService.obtenerSkusPorProducto(id));
    }
}
