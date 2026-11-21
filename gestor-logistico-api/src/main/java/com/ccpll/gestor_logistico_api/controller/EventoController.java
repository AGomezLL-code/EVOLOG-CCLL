package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.dto.ReservaEventoResponseDTO;
import com.ccpll.gestor_logistico_api.dto.SolicitudReservaDTO;
import com.ccpll.gestor_logistico_api.entity.eventos.AmbienteEntity;
import com.ccpll.gestor_logistico_api.service.EventoService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    // --- AMBIENTES ---
    @GetMapping("/ambientes")
    public ResponseEntity<List<AmbienteEntity>> listarAmbientes() {
        return ResponseEntity.ok(eventoService.listarAmbientes());
    }

    @PostMapping("/ambientes")
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA')")
    public ResponseEntity<AmbienteEntity> crearAmbiente(@RequestBody AmbienteEntity ambiente) {
        return ResponseEntity.ok(eventoService.crearAmbiente(ambiente));
    }

    // --- RESERVAS / CALENDARIO ---
    @GetMapping("/reservas")
    public ResponseEntity<List<ReservaEventoResponseDTO>> listarReservasPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(eventoService.listarReservasPorFecha(fecha));
    }

    @PostMapping("/reservas")
    @PreAuthorize("hasAnyAuthority('AREA_COMERCIAL', 'ROLE_AREA_COMERCIAL', 'JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA')")
    public ResponseEntity<ReservaEventoResponseDTO> solicitarReserva(@RequestBody SolicitudReservaDTO dto) {
        return ResponseEntity.ok(eventoService.solicitarReserva(dto));
    }

    // --- CONFIRMAR Y BLOQUEAR RESERVA ---
    @PatchMapping("/reservas/{id}/estado")
    @PreAuthorize("hasAnyAuthority('JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA')")
    public ResponseEntity<ReservaEventoResponseDTO> cambiarEstadoReserva(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(eventoService.cambiarEstadoReserva(id, body.get("estado")));
    }

    // --- ELIMINAR RESERVA Y CANCELAR REQUERIMIENTO VINCULADO ---
    @DeleteMapping("/reservas/{id}")
    @PreAuthorize("hasAnyAuthority('AREA_COMERCIAL', 'ROLE_AREA_COMERCIAL', 'JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA')")
    public ResponseEntity<Map<String, String>> eliminarReserva(@PathVariable Integer id) {
        return ResponseEntity.ok(eventoService.eliminarReserva(id));
    }

    @PutMapping("/reservas/{id}")
    @PreAuthorize("hasAnyAuthority('AREA_COMERCIAL', 'ROLE_AREA_COMERCIAL', 'JEFE_LOGISTICA', 'ROLE_JEFE_LOGISTICA')")
    public ResponseEntity<ReservaEventoResponseDTO> editarReserva(
            @PathVariable Integer id,
            @RequestBody SolicitudReservaDTO dto) {
        return ResponseEntity.ok(eventoService.editarReserva(id, dto));
    }
}
