package com.ccpll.gestor_logistico_api.service;

import com.ccpll.gestor_logistico_api.dto.HistoriaInventarioResponseDTO;

import java.util.List;

public interface HistorialInventarioService {
    List<HistoriaInventarioResponseDTO> listarTodos();

}
