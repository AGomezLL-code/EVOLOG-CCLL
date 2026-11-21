package com.ccpll.gestor_logistico_api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class JwtResponseDTO {
    private String token;
    private String username;
    private String nombreCompleto;
    private String area;
    private String rol;
}