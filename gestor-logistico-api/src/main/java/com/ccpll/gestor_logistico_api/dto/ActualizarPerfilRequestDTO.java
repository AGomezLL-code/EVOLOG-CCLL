package com.ccpll.gestor_logistico_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre completo no puede superar los 100 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El área es obligatoria")
    @Size(max = 80, message = "El área no puede superar los 80 caracteres")
    private String area;
}
