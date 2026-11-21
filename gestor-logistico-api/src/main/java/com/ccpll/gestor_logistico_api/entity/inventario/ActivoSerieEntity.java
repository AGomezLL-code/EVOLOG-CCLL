package com.ccpll.gestor_logistico_api.entity.inventario;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activos_serie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivoSerieEntity extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioEntity producto;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoActivo estado;

    public enum EstadoActivo {
        DISPONIBLE,
        PRESTADO,
        EN_MANTENIMIENTO
    }
}
