package com.ccpll.gestor_logistico_api.entity.eventos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity;

@Entity
@Table(name = "reservas_eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaEventoEntity {

    public enum EstadoReserva {
        PENDIENTE, APROBADO, RECHAZADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 25)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ambiente_id", nullable = false)
    private AmbienteEntity ambiente;

    @Column(name = "nombre_evento", nullable = false, length = 150)
    private String nombreEvento;

    @Column(nullable = false, length = 100)
    private String solicitante;

    @Column(name = "area_solicitante", nullable = false, length = 80)
    private String areaSolicitante;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(length = 300)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requerimiento_id")
    private RequerimientoEntity requerimiento;
}
