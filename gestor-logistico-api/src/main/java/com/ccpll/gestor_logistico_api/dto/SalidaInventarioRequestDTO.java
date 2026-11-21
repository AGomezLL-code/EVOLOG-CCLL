package com.ccpll.gestor_logistico_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalidaInventarioRequestDTO {

    @NotNull(message = "El ID del artículo es obligatorio")
    private Integer articuloId;

    private String sku;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "El cliente es obligatorio") // <--- OBLIGATORIO
    private String cliente;

    @NotBlank(message = "El área solicitante es obligatoria") // <--- OBLIGATORIO
    private String areaSolicita;

    private String observacion;
}