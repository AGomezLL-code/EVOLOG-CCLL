package com.ccpll.gestor_logistico_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPerfilResponseDTO {
    private Integer id;
    private String username;
    private String nombreCompleto;
    private String area;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private String fotoUrl;
}
