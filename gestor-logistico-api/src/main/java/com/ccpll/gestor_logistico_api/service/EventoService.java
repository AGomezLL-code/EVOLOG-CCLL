package com.ccpll.gestor_logistico_api.service;

import com.ccpll.gestor_logistico_api.dto.ReservaEventoResponseDTO;
import com.ccpll.gestor_logistico_api.dto.SolicitudReservaDTO;
import com.ccpll.gestor_logistico_api.entity.eventos.AmbienteEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EventoService {

    List<AmbienteEntity> listarAmbientes();

    AmbienteEntity crearAmbiente(AmbienteEntity ambiente);

    List<ReservaEventoResponseDTO> listarReservasPorFecha(LocalDate fecha);

    ReservaEventoResponseDTO solicitarReserva(SolicitudReservaDTO dto);

    ReservaEventoResponseDTO editarReserva(Integer id, SolicitudReservaDTO dto);

    ReservaEventoResponseDTO cambiarEstadoReserva(Integer id, String nuevoEstado);

    Map<String, String> eliminarReserva(Integer id);
}
