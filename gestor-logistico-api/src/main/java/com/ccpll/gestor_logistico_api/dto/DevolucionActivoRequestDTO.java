package com.ccpll.gestor_logistico_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionActivoRequestDTO {
    private Integer requerimientoId;
    private Integer requerimientoDetalleId;
    private String sku;
    private String usuarioRecibe;
    private String observaciones;
}
