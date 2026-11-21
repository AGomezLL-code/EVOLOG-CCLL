package com.ccpll.gestor_logistico_api.dto;

import com.ccpll.gestor_logistico_api.entity.eventos.AmbienteEntity;
import com.ccpll.gestor_logistico_api.entity.eventos.ReservaEventoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ReservaEventoResponseDTO {
    private Integer id;
    private String codigo;
    private AmbienteEntity ambiente;
    private String nombreEvento;
    private String solicitante;
    private String areaSolicitante;
    private LocalDate fechaEvento;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private RequerimientoResumenDTO requerimiento;
    private List<ItemReservaDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemReservaDTO {
        private Integer articuloId;
        private Integer cantidad;
    }

    @Data
    @Builder
    public static class RequerimientoResumenDTO {
        private Integer id;
        private String codigo;
        private String estado;
        private List<DetalleResumenDTO> detalles;
    }

    @Data
    @Builder
    public static class DetalleResumenDTO {
        private Integer articuloId;
        private String articuloDescripcion;
        private BigDecimal cantidadSolicitada;
    }

    public static ReservaEventoResponseDTO fromEntity(ReservaEventoEntity entity) {
        RequerimientoResumenDTO reqResumen = null;
        if (entity.getRequerimiento() != null) {
            List<DetalleResumenDTO> detallesResumen = entity.getRequerimiento().getDetalles() != null
                    ? entity.getRequerimiento().getDetalles().stream()
                        .map(d -> DetalleResumenDTO.builder()
                                .articuloId(d.getArticulo().getId())
                                .articuloDescripcion(d.getArticulo().getCodigo() + " - " + d.getArticulo().getDescripcion())
                                .cantidadSolicitada(d.getCantidadSolicitada())
                                .build())
                        .toList()
                    : List.of();

            reqResumen = RequerimientoResumenDTO.builder()
                    .id(entity.getRequerimiento().getId())
                    .codigo(entity.getRequerimiento().getCodigo())
                    .estado(entity.getRequerimiento().getEstado().name())
                    .detalles(detallesResumen)
                    .build();
        }

        return ReservaEventoResponseDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .ambiente(entity.getAmbiente())
                .nombreEvento(entity.getNombreEvento())
                .solicitante(entity.getSolicitante())
                .areaSolicitante(entity.getAreaSolicitante())
                .fechaEvento(entity.getFechaEvento())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .estado(entity.getEstado().name())
                .observaciones(entity.getObservaciones())
                .fechaCreacion(entity.getFechaCreacion())
                .requerimiento(reqResumen)
                .items(parsearItems(entity.getItemsJson()))
                .build();
    }

    public static List<ItemReservaDTO> parsearItems(String raw) {
        List<ItemReservaDTO> lista = new ArrayList<>();
        if (raw == null || raw.isBlank()) return lista;

        for (String par : raw.split(";")) {
            String[] partes = par.split(":");
            if (partes.length == 2) {
                try {
                    lista.add(new ItemReservaDTO(
                            Integer.parseInt(partes[0].trim()),
                            Integer.parseInt(partes[1].trim())
                    ));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return lista;
    }
}