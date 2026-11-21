package com.ccpll.gestor_logistico_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivoSerieDTO {
    private Integer id;
    private String sku;
    private String estado; // DISPONIBLE, PRESTADO, EN_MANTENIMIENTO
}
