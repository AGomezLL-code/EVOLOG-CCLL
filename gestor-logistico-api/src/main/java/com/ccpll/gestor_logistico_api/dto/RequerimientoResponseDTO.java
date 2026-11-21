package com.ccpll.gestor_logistico_api.dto;

import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity.EstadoRequerimiento;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequerimientoResponseDTO {
    private Integer id;
    private String codigo;
    private String areaSolicitante;
    private String solicitante;
    private String observacion;
    private EstadoRequerimiento estado;
    private LocalDate fechaSolicitud;
    private LocalDateTime fechaDespacho;
    
    private List<DetalleResponseDTO> detalles;

    // Campos de Auditoría
    private String usuarioCreacion;
    private LocalDateTime fechaCreacion;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleResponseDTO {
        private Integer id;
        private Integer articuloId;
        private String articuloCodigo;
        private String articuloDescripcion;
        private String skuAsignado;
        private BigDecimal cantidadSolicitada;
        private BigDecimal cantidadAtendida;
        private Boolean devuelto;
        private BigDecimal stockDisponible;
        private String caracteristicas;
        private String sustento;
    }
}