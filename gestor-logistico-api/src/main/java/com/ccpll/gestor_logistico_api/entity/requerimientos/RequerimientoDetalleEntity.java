package com.ccpll.gestor_logistico_api.entity.requerimientos;

import com.ccpll.gestor_logistico_api.entity.inventario.InventarioEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "requerimiento_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RequerimientoDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requerimiento_id", nullable = false)
    private RequerimientoEntity requerimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private InventarioEntity articulo;

    @Column(name = "sku_asignado", columnDefinition = "TEXT")
private String skuAsignado;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_atendida", precision = 10, scale = 2)
    private BigDecimal cantidadAtendida;

    @Column(name = "unidad_medida", length = 50)
private String unidadMedida;

    // AGREGAR ESTA PROPIEDAD PARA QUE CUMPLA CON LA COLUMNA DE MYSQL
    @Column(name = "producto_servicio", length = 255)
    private String productoServicio;

    @Column(name = "caracteristicas", length = 255)
    private String caracteristicas;

    @Column(name = "sustento", length = 255)
    private String sustento;

    @Builder.Default
@Column(name = "devuelto", nullable = false)
private Boolean devuelto = false;
}
