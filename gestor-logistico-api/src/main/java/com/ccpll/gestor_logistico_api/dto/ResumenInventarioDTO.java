package com.ccpll.gestor_logistico_api.dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenInventarioDTO {
    private Long totalProductos;
    private BigDecimal valorTotalInventario;
    private Long cantidadStockBajo;
}
