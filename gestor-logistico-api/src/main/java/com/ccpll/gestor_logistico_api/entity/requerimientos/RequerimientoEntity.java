package com.ccpll.gestor_logistico_api.entity.requerimientos;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requerimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // Usar SuperBuilder para heredar correctamente los campos de AuditoriaBase
public class RequerimientoEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo; // Ej: REQ-2026-001

    @Column(nullable = false, length = 100)
    private String areaSolicitante; // Ej: TI, Contabilidad, Almacén

    @Column(nullable = false, length = 100)
    private String solicitante; // Nombre del usuario que solicita

    @Column(length = 255)
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRequerimiento estado;

    @Column(nullable = false)
    private LocalDate fechaSolicitud;

    private LocalDateTime fechaDespacho;

    @OneToMany(mappedBy = "requerimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequerimientoDetalleEntity> detalles = new ArrayList<>();

    public enum EstadoRequerimiento {
        PENDIENTE, 
    APROBADO, 
    DESPACHADO, 
    ENTREGADO, // <--- NUEVO (Para activos/equipos en préstamo)
    DEVUELTO,  // <--- NUEVO (Cuando el equipo retorna a Almacén)
    RECHAZADO,
    CANCELADO
    }

    // Método helper para mantener sincronizada la relación bidireccional
    public void agregarDetalle(RequerimientoDetalleEntity detalle) {
        if (detalles == null) {
            detalles = new ArrayList<>();
        }
        detalles.add(detalle);
        detalle.setRequerimiento(this);
    }
}