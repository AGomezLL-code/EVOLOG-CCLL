package com.ccpll.gestor_logistico_api.entity.seguridad;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String accion; // LOGIN, CREAR_REQUERIMIENTO, APROBAR, DESPACHAR, RECHAZAR, DEVOLUCION,
                           // ENTRADA_STOCK

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false, length = 60)
    private String entidad; // Usuario, Requerimiento, Inventario, ReservaEvento

    @Column(name = "entidad_id")
    private Integer entidadId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(nullable = false, length = 100)
    private String usuario;
}
