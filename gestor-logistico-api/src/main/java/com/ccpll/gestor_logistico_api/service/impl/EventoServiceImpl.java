package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.dto.*;
import com.ccpll.gestor_logistico_api.entity.eventos.AmbienteEntity;
import com.ccpll.gestor_logistico_api.entity.eventos.ReservaEventoEntity;
import com.ccpll.gestor_logistico_api.entity.eventos.ReservaEventoEntity.EstadoReserva;
import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity;
import com.ccpll.gestor_logistico_api.repository.AmbienteRepository;
import com.ccpll.gestor_logistico_api.repository.RequerimientoRepository;
import com.ccpll.gestor_logistico_api.repository.ReservaEventoRepository;
import com.ccpll.gestor_logistico_api.service.AuditoriaService;
import com.ccpll.gestor_logistico_api.service.EventoService;
import com.ccpll.gestor_logistico_api.service.RequerimientoService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private final AmbienteRepository ambienteRepository;
    private final ReservaEventoRepository reservaRepository;
    private final AuditoriaService auditoriaService;
    private final RequerimientoRepository requerimientoRepository;
    private final RequerimientoService requerimientoService;
    

    @Override
    @Transactional(readOnly = true)
    public List<AmbienteEntity> listarAmbientes() {
        return ambienteRepository.findByActivoTrueOrderByIdAsc();
    }

    @Override
    @Transactional
    public AmbienteEntity crearAmbiente(AmbienteEntity ambiente) {
        ambiente.setActivo(true);
        AmbienteEntity guardado = ambienteRepository.save(ambiente);
        auditoriaService.registrar("CREAR_AMBIENTE", "Ambiente", guardado.getId(),
                "Creó ambiente: " + guardado.getNombre());
        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaEventoResponseDTO> listarReservasPorFecha(LocalDate fecha) {
        List<ReservaEventoEntity> lista = (fecha != null)
                ? reservaRepository.findByFechaEventoOrderByHoraInicioAsc(fecha)
                : reservaRepository.findAllByOrderByFechaEventoDescHoraInicioAsc();

        return lista.stream().map(ReservaEventoResponseDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public ReservaEventoResponseDTO solicitarReserva(SolicitudReservaDTO dto) {
        validarDatosReserva(dto, -1);

        AmbienteEntity ambiente = ambienteRepository.findById(dto.getAmbienteId())
                .orElseThrow(() -> new RuntimeException("Ambiente no encontrado"));

        String codigoEvento = String.format("EVT-%d-%04d", LocalDate.now().getYear(), reservaRepository.count() + 1);

        // NO se crea requerimiento ni se separan productos todavía; se guardan en itemsJson hasta confirmar
        String itemsSerializados = serializarItems(dto.getItems());

        ReservaEventoEntity reserva = ReservaEventoEntity.builder()
                .codigo(codigoEvento)
                .ambiente(ambiente)
                .nombreEvento(dto.getNombreEvento())
                .solicitante(dto.getSolicitante())
                .areaSolicitante(dto.getAreaSolicitante())
                .fechaEvento(dto.getFechaEvento())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .observaciones(dto.getObservaciones())
                .itemsJson(itemsSerializados)
                .requerimiento(null)
                .estado(EstadoReserva.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        ReservaEventoEntity guardada = reservaRepository.save(reserva);

        auditoriaService.registrar(
                "SOLICITAR_EVENTO",
                "ReservaEvento",
                guardada.getId(),
                "Registró solicitud " + guardada.getCodigo() + " (" + ambiente.getNombre()
                        + ") en estado PENDIENTE (sin separar stock aún)");

        return ReservaEventoResponseDTO.fromEntity(guardada);
    }

    @Override
    @Transactional
    public ReservaEventoResponseDTO editarReserva(Integer id, SolicitudReservaDTO dto) {
        ReservaEventoEntity reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden editar reservas en estado PENDIENTE. Esta reserva ya está " + reserva.getEstado().name() + ".");
        }

        validarDatosReserva(dto, id);

        AmbienteEntity ambiente = ambienteRepository.findById(dto.getAmbienteId())
                .orElseThrow(() -> new RuntimeException("Ambiente no encontrado"));

        reserva.setAmbiente(ambiente);
        reserva.setNombreEvento(dto.getNombreEvento());
        reserva.setFechaEvento(dto.getFechaEvento());
        reserva.setHoraInicio(dto.getHoraInicio());
        reserva.setHoraFin(dto.getHoraFin());
        reserva.setObservaciones(dto.getObservaciones());
        reserva.setItemsJson(serializarItems(dto.getItems()));

        ReservaEventoEntity actualizada = reservaRepository.save(reserva);

        auditoriaService.registrar(
                "EDITAR_EVENTO",
                "ReservaEvento",
                actualizada.getId(),
                "Editó solicitud pendiente " + actualizada.getCodigo() + " (" + ambiente.getNombre()
                        + " - " + actualizada.getFechaEvento() + ")");

        return ReservaEventoResponseDTO.fromEntity(actualizada);
    }

    @Override
    @Transactional
    public ReservaEventoResponseDTO cambiarEstadoReserva(Integer id, String nuevoEstadoStr) {
        final ReservaEventoEntity reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        EstadoReserva nuevoEstado = EstadoReserva.valueOf(nuevoEstadoStr.toUpperCase());

        if (nuevoEstado == EstadoReserva.APROBADO) {
            boolean cruce = reservaRepository.existeCruceAprobado(
                    reserva.getAmbiente().getId(),
                    reserva.getFechaEvento(),
                    reserva.getHoraInicio(),
                    reserva.getHoraFin(),
                    reserva.getId());
            if (cruce) {
                throw new IllegalStateException("Conflicto horario: ya existe un evento aprobado en ese horario.");
            }

            // RECIÉN AL CONFIRMAR Y BLOQUEAR SE CREA EL REQUERIMIENTO Y SE SEPARAN LOS PRODUCTOS
            if (reserva.getRequerimiento() == null && reserva.getItemsJson() != null && !reserva.getItemsJson().isBlank()) {
                List<ReservaEventoResponseDTO.ItemReservaDTO> items = deserializarItems(reserva.getItemsJson());
                if (!items.isEmpty()) {
                    RequerimientoRequestDTO reqDto = new RequerimientoRequestDTO();
                    reqDto.setAreaSolicitante(reserva.getAreaSolicitante());
                    reqDto.setSolicitante(reserva.getSolicitante());
                    reqDto.setObservacion("Recursos confirmados para evento [" + reserva.getCodigo() + "]: "
                            + reserva.getNombreEvento() + " en " + reserva.getAmbiente().getNombre()
                            + " (" + reserva.getFechaEvento() + ")");

                    List<RequerimientoDetalleRequestDTO> detalles = items.stream().map(item -> {
                        RequerimientoDetalleRequestDTO det = new RequerimientoDetalleRequestDTO();
                        det.setArticuloId(item.getArticuloId());
                        det.setCantidadSolicitada(
                                item.getCantidad() != null ? BigDecimal.valueOf(item.getCantidad()) : BigDecimal.ONE);
                        det.setSustento("Uso en evento confirmado: " + reserva.getNombreEvento());
                        return det;
                    }).toList();

                    reqDto.setDetalles(detalles);
                    RequerimientoResponseDTO reqCreado = requerimientoService.crearRequerimiento(reqDto);
                    RequerimientoEntity reqVinculado = requerimientoRepository.findById(reqCreado.getId()).orElse(null);
                    reserva.setRequerimiento(reqVinculado);
                }
            }
        }

        reserva.setEstado(nuevoEstado);
        ReservaEventoEntity guardada = reservaRepository.save(reserva);

        auditoriaService.registrar(
                nuevoEstado.name() + "_EVENTO",
                "ReservaEvento",
                guardada.getId(),
                "Reserva " + guardada.getCodigo() + " (" + guardada.getAmbiente().getNombre() + ") pasó a "
                        + nuevoEstado.name()
                        + (guardada.getRequerimiento() != null
                                ? " y separó recursos en " + guardada.getRequerimiento().getCodigo()
                                : ""));

        return ReservaEventoResponseDTO.fromEntity(guardada);
    }
    @Override
    @Transactional
    public Map<String, String> eliminarReserva(Integer id) {
        ReservaEventoEntity reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        RequerimientoEntity reqVinculado = reserva.getRequerimiento();
        String notaRequerimiento = "";

        if (reqVinculado != null) {
            String estadoReq = reqVinculado.getEstado().name();

            if ("DESPACHADO".equals(estadoReq) || "ENTREGADO".equals(estadoReq)) {
                throw new IllegalStateException("No se puede eliminar el evento porque el requerimiento "
                        + reqVinculado.getCodigo() + " ya fue despachado. Registre primero la devolución en almacén.");
            }

            reqVinculado.setEstado(RequerimientoEntity.EstadoRequerimiento.CANCELADO);

            String obsActual = reqVinculado.getObservacion() != null ? reqVinculado.getObservacion() + " | " : "";
            reqVinculado.setObservacion(
                    obsActual + "CANCELADO AUTOMÁTICAMENTE por eliminación del evento " + reserva.getCodigo());
            requerimientoRepository.save(reqVinculado);

            auditoriaService.registrar(
                    "CANCELAR_REQUERIMIENTO_EVENTO",
                    "Requerimiento",
                    reqVinculado.getId(),
                    "Requerimiento " + reqVinculado.getCodigo() + " pasó a CANCELADO al eliminar el evento "
                            + reserva.getCodigo());

            notaRequerimiento = " y se marcó como CANCELADO su requerimiento vinculado " + reqVinculado.getCodigo();
        }

        String detalleAuditoria = String.format(
                "Eliminó reserva %s (%s) del %s (%s - %s) solicitada por %s%s",
                reserva.getCodigo(),
                reserva.getAmbiente().getNombre(),
                reserva.getFechaEvento(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getSolicitante(),
                notaRequerimiento);

        reservaRepository.delete(reserva);

        auditoriaService.registrar("ELIMINAR_EVENTO", "ReservaEvento", id, detalleAuditoria);

        return Map.of("message", "Reserva eliminada" + notaRequerimiento + ".");
    }

    private void validarDatosReserva(SolicitudReservaDTO dto, Integer excluirReservaId) {
        if (dto.getFechaEvento() == null || !dto.getFechaEvento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Los eventos deben separarse con al menos 1 día de anticipación (a partir de mañana).");
        }

        if (dto.getHoraInicio() == null || dto.getHoraFin() == null || !dto.getHoraFin().isAfter(dto.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        boolean ocupado = reservaRepository.existeCruceAprobado(
                dto.getAmbienteId(), dto.getFechaEvento(), dto.getHoraInicio(), dto.getHoraFin(), excluirReservaId);
        if (ocupado) {
            throw new IllegalStateException("El ambiente ya está bloqueado en ese rango horario.");
        }
    }
    

    private String serializarItems(List<?> items) {
        if (items == null || items.isEmpty()) return null;
        return items.stream()
                .map(it -> {
                    try {
                        Integer artId = (Integer) it.getClass().getMethod("getArticuloId").invoke(it);
                        Integer cant = (Integer) it.getClass().getMethod("getCantidad").invoke(it);
                        return (artId != null && cant != null) ? artId + ":" + cant : null;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(s -> s != null)
                .collect(Collectors.joining(";"));
    }

    private List<ReservaEventoResponseDTO.ItemReservaDTO> deserializarItems(String raw) {
        return ReservaEventoResponseDTO.parsearItems(raw);
    }
}