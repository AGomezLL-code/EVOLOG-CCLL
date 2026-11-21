package com.ccpll.gestor_logistico_api.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpiDTO {
    private BigDecimal inventarioValorizado;
    private Long unidadesDisponibles;
    private Long solicitudesPendientes;
    private Long productosPorReponer;
    private Long equiposVencidos;

}
