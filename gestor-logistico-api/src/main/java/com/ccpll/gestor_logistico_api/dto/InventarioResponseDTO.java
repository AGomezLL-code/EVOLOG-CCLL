package com.ccpll.gestor_logistico_api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioResponseDTO {
    private Integer id;
    private String codigo;
    private String descripcion;
    private String skuAsignado;
    private String unidad;
    private BigDecimal stockActual;
    private BigDecimal costoPromedio;
    private BigDecimal stockMinimo;
    private Integer version;
    
    // Aplanamiento de la relacion Categoria
    private Integer categoriaId;
    private String categoriaNombre;
    
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private Boolean requiereSku;        // <--- NUEVO
    private String ubicacionFisica;
}