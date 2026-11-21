package com.ccpll.gestor_logistico_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtenderRequerimientoRequestDTO {
    
    private Integer requerimientoId;
    private String usuarioAtiende;
    private List<DetalleSkuAsignacionDTO> detallesSku;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleSkuAsignacionDTO {
        private Integer detalleId;
        private String sku;
    }
}
