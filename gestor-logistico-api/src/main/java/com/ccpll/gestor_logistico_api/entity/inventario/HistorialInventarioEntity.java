package com.ccpll.gestor_logistico_api.entity.inventario;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "historial_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HistorialInventarioEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private InventarioEntity articulo;

    @Column(name = "sku_asignado", length = 50)
    private String skuAsignado;

    @Enumerated(EnumType.STRING)
@Column(name = "tipo", length = 20, nullable = false)
private TipoMovimiento tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "proveedor_cliente", length = 200)
    private String proveedorCliente;

    @Column(length = 100)
    private String area;

    @Column(name = "stock_anterior", precision = 10, scale = 2)
    private BigDecimal stockAnterior;

    @Column(name = "stock_nuevo", precision = 10, scale = 2)
    private BigDecimal stockNuevo;

    @Column(name = "usuario_registra", length = 50)
    private String usuarioRegistra;

    public enum TipoMovimiento {
        ENTRADA, SALIDA, PRESTAMO,  
    DEVOLUCION
    }
}