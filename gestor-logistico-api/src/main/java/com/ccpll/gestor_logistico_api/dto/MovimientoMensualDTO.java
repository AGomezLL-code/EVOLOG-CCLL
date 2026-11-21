package com.ccpll.gestor_logistico_api.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoMensualDTO {
    private String mes;           // Ene, Feb, Mar, etc.
    private BigDecimal entradas; // Cantidad total ingresada
    private BigDecimal salidas;  // Cantidad total despachada
}
