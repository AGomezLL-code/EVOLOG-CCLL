package com.ccpll.gestor_logistico_api.service;

import java.util.List;

import com.ccpll.gestor_logistico_api.dto.DevolucionActivoRequestDTO;
import com.ccpll.gestor_logistico_api.dto.RequerimientoRequestDTO;
import com.ccpll.gestor_logistico_api.dto.RequerimientoResponseDTO;
import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity.EstadoRequerimiento;

public interface RequerimientoService {
    RequerimientoResponseDTO crearRequerimiento(RequerimientoRequestDTO dto);
    List<RequerimientoResponseDTO> listarTodos();
    RequerimientoResponseDTO obtenerPorId(Integer id);
    RequerimientoResponseDTO cambiarEstado(Integer id, EstadoRequerimiento nuevoEstado, String observaciones);
    RequerimientoResponseDTO registrarDevolucionActivo(DevolucionActivoRequestDTO dto);
}