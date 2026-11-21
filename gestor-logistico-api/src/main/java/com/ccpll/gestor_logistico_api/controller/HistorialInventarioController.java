package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.dto.HistoriaInventarioResponseDTO;
import com.ccpll.gestor_logistico_api.service.HistorialInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/historial-inventario", "/api/historial-inventario"})
@RequiredArgsConstructor
public class HistorialInventarioController {

    private final HistorialInventarioService historialInventarioService;

    @GetMapping
    public ResponseEntity<List<HistoriaInventarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(historialInventarioService.listarTodos());
    }
}
