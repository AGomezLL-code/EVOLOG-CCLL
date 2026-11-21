package com.ccpll.gestor_logistico_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEventoDTO {
    private Integer articuloId;
    private Integer cantidad;
}
