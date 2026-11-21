package com.ccpll.gestor_logistico_api.entity.licitacion;

import com.ccpll.gestor_logistico_api.entity.compras.ProveedorEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Entity
@Table(name = "seleccion_proveedor_item_precio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SeleccionProveedorItemPrecioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private SeleccionProveedorItemEntity item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private ProveedorEntity proveedor;

    @Column(precision = 10, scale = 2)
    private BigDecimal precio;
}
