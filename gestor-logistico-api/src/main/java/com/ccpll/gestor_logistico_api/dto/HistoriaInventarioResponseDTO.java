package com.ccpll.gestor_logistico_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ccpll.gestor_logistico_api.entity.inventario.HistorialInventarioEntity.TipoMovimiento;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriaInventarioResponseDTO {

    private Integer id;
    private Integer articuloId;
    private String articuloCodigo;
    private String articuloDescripcion;
    private String skuAsignado;
    private TipoMovimiento tipo;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;
    private LocalDate fecha;
    private String proveedorCliente;
    private String area;
    private BigDecimal stockAnterior;
    private BigDecimal stockNuevo;
    private LocalDateTime fechaCreacion;
}
