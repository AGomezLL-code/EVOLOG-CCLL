package com.ccpll.gestor_logistico_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudReservaDTO {
    private Integer ambienteId;
    private String nombreEvento;
    private String solicitante;
    private String areaSolicitante;
    private LocalDate fechaEvento;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String observaciones;
    private List<ItemEventoDTO> items;
}
