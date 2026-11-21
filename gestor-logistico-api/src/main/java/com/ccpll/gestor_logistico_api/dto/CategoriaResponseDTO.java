package com.ccpll.gestor_logistico_api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponseDTO {
    private Integer id;
    private String nombre;
}