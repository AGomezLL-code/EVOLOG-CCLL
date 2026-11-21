package com.ccpll.gestor_logistico_api.entity.gestion;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Entity
@Table(name = "actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActividadEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(name = "fecha_plazo")
    private LocalDate fechaPlazo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoActividad estado = EstadoActividad.PENDIENTE;

    public enum EstadoActividad {
        PENDIENTE, REALIZADO
    }
}
