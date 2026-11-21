package com.ccpll.gestor_logistico_api.entity.compras;
import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrdenCompraEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_orden", nullable = false, unique = true, length = 20)
    private String numeroOrden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOrden tipo;

    @Column(name = "fecha_orden", nullable = false)
    private LocalDate fechaOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private ProveedorEntity proveedor;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String moneda = "PEN";

    @Column(length = 50)
    private String comprobante;

    @Column(name = "forma_pago", length = 100)
    private String formaPago;

    @Column(name = "fecha_requerida")
    private LocalDate fechaRequerida;

    @Column(name = "hora_requerida")
    private LocalTime horaRequerida;

    @Column(name = "lugar_entrega", length = 255)
    private String lugarEntrega;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(name = "conformidad_oc_os", length = 100)
    private String conformidadOcOs;

    @Column(name = "cantidad_porcentaje", precision = 5, scale = 2)
    private BigDecimal cantidadPorcentaje;

    @Column(name = "calidad_porcentaje", precision = 5, scale = 2)
    private BigDecimal calidadPorcentaje;

    @Column(name = "medio_ambiente_producto", precision = 10, scale = 2)
    private BigDecimal medioAmbienteProducto;

    @Column(name = "medio_ambiente_servicio", precision = 10, scale = 2)
    private BigDecimal medioAmbienteServicio;

    @Column(precision = 10, scale = 2)
    private BigDecimal antisoborno;

    @Column(columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "clausula_penalidad", columnDefinition = "TEXT")
    private String clausulaPenalidad;

    @Column(length = 255)
    private String penalidad;

    @Column(name = "centro_costo", length = 100)
    private String centroCosto;

    @Column(length = 100)
    private String area;

    @Column(name = "costo_total", precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenDetalleEntity> detalles = new ArrayList<>();

    public enum TipoOrden {
        COMPRA, SERVICIO
    }
}
