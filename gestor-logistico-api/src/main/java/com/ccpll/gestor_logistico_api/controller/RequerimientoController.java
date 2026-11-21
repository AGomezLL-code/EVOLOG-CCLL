package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.dto.DevolucionActivoRequestDTO;
import com.ccpll.gestor_logistico_api.dto.RequerimientoRequestDTO;
import com.ccpll.gestor_logistico_api.dto.RequerimientoResponseDTO;
import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity.EstadoRequerimiento;
import com.ccpll.gestor_logistico_api.service.RequerimientoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requerimientos")
@RequiredArgsConstructor
public class RequerimientoController {

    private final RequerimientoService requerimientoService;

    // Cualquier usuario autenticado (Cliente Interno, Asistente, Jefe, etc.) puede registrar solicitudes
    @PostMapping
    public ResponseEntity<RequerimientoResponseDTO> crear(@Valid @RequestBody RequerimientoRequestDTO dto) {
        return new ResponseEntity<>(requerimientoService.crearRequerimiento(dto), HttpStatus.CREATED);
    }

    // Listado filtrado automáticamente en el Service según el rol (CLIENTE_INTERNO solo ve los suyos)
    @GetMapping
    public ResponseEntity<List<RequerimientoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(requerimientoService.listarTodos());
    }

    // Consulta de detalle por ID
    @GetMapping("/{id}")
    public ResponseEntity<RequerimientoResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(requerimientoService.obtenerPorId(id));
    }

    // RBAC: Solo JEFE_LOGISTICA aprueba/rechaza; JEFE_LOGISTICA y ASISTENTE pueden despachar
    @PreAuthorize("hasAnyRole('JEFE_LOGISTICA', 'ASISTENTE')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RequerimientoResponseDTO> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam EstadoRequerimiento nuevoEstado,
            @RequestParam(required = false) String observaciones) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esJefe = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_JEFE_LOGISTICA"));

        if ((nuevoEstado == EstadoRequerimiento.APROBADO || nuevoEstado == EstadoRequerimiento.RECHAZADO) && !esJefe) {
            throw new AccessDeniedException("Solo el Jefe de Logística puede aprobar o rechazar requerimientos.");
        }

        return ResponseEntity.ok(requerimientoService.cambiarEstado(id, nuevoEstado, observaciones));
    }

    // RBAC: Solo personal de logística/almacén puede registrar devoluciones de activos
    @PreAuthorize("hasAnyRole('JEFE_LOGISTICA', 'ASISTENTE', 'AUXILIAR')")
    @PostMapping("/devolucion")
    public ResponseEntity<RequerimientoResponseDTO> registrarDevolucion(@RequestBody DevolucionActivoRequestDTO dto) {
        RequerimientoResponseDTO response = requerimientoService.registrarDevolucionActivo(dto);
        return ResponseEntity.ok(response);
    }
}