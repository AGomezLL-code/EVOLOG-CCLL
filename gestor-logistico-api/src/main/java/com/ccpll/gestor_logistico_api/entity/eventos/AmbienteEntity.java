package com.ccpll.gestor_logistico_api.entity.eventos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ambientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AmbienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(length = 150)
    private String ubicacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
