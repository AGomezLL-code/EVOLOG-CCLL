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
public class EntradaInventarioRequestDTO {

    @NotNull(message = "El ID del artículo es obligatorio")
    private Integer articuloId;

    private String sku;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitario;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

   @NotBlank(message = "El proveedor es obligatorio") // <--- OBLIGATORIO
    private String proveedor;

    @NotBlank(message = "El área de destino es obligatoria") // <--- OBLIGATORIO
    private String areaDestino;
}
