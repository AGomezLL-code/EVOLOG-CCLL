package com.ccpll.gestor_logistico_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
    private String descripcion;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 20, message = "La unidad no puede superar los 20 caracteres")
    private String unidad;

    private BigDecimal stockActual;   // <--- SE AGREGA ESTE CAMPO
    private BigDecimal costoPromedio;

    @NotNull(message = "El stock mínimo es obligatorio")
    @DecimalMin(value = "0.00", message = "El stock mínimo no puede ser negativo")
    private BigDecimal stockMinimo;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;

    private Boolean requiereSku;        // <--- NUEVO
    private String ubicacionFisica;

    @Builder.Default
    private Boolean activo = true;
}
