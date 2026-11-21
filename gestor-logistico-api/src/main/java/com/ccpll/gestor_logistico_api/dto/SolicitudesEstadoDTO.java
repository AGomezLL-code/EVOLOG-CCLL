package com.ccpll.gestor_logistico_api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudesEstadoDTO {
    private Long aprobadas;
    private Long pendientes;
    private Long enRevision;
    private Long rechazadas;
}
