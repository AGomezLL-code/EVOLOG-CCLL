package com.ccpll.gestor_logistico_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequerimientoRequestDTO {

    @NotBlank(message = "El área solicitante es obligatoria")
    private String areaSolicitante;

    @NotBlank(message = "El nombre del solicitante es obligatorio")
    private String solicitante;

    private String observacion;

    @NotEmpty(message = "Debe incluir al menos un ítem en la solicitud")
    @Valid
    private List<@Valid RequerimientoDetalleRequestDTO> detalles;
}