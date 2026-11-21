package com.ccpll.gestor_logistico_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequerimientoDetalleRequestDTO {

    @NotNull(message = "El artículo es obligatorio")
    private Integer articuloId;

    private String skuAsignado;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidadSolicitada;

    private String caracteristicas;
    private String sustento;
}
