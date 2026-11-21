package com.ccpll.gestor_logistico_api.entity.inventario;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "entrada_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EntradaInventarioEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private InventarioEntity articulo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 200)
    private String proveedor;

    @Column(name = "area_destino", length = 100)
    private String areaDestino;

    @Column(name = "usuario_registra", length = 50)
    private String usuarioRegistra;
}
