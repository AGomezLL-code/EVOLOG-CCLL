package com.ccpll.gestor_logistico_api.entity.licitacion;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;
import com.ccpll.gestor_logistico_api.entity.compras.ProveedorEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seleccion_proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SeleccionProveedorEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String descripcion;

    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_seleccionado_id")
    private ProveedorEntity proveedorSeleccionado;

    @Column(columnDefinition = "TEXT")
    private String justificacion;

    @Column(name = "condicion_pago", length = 100)
    private String condicionPago;

    @Column(name = "tiempo_entrega", length = 100)
    private String tiempoEntrega;

    @Column(name = "comprobante_pago", length = 50)
    private String comprobantePago;

    @Column(name = "buenas_practicas", length = 10)
    private String buenasPracticas;

    @Builder.Default
    @Column(name = "aprobado_logistica")
    private Boolean aprobadoLogistica = false;

    @Builder.Default
    @Column(name = "aprobado_gerencia")
    private Boolean aprobadoGerencia = false;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_logistica")
    private LocalDate fechaLogistica;

    @Column(name = "fecha_gerencia")
    private LocalDate fechaGerencia;

    @Column(name = "fecha_solicitante")
    private LocalDate fechaSolicitante;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "seleccionProveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeleccionProveedorItemEntity> items = new ArrayList<>();
}