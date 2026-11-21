package com.ccpll.gestor_logistico_api.entity.compras;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrdenDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompraEntity ordenCompra;

    @Column(name = "bloque_numero", nullable = false)
    private Integer bloqueNumero;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "unidad_medida", nullable = false, length = 10)
    private String unidadMedida;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
}
