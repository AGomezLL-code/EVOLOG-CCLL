package com.ccpll.gestor_logistico_api.entity.licitacion;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seleccion_proveedor_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SeleccionProveedorItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seleccion_proveedor_id", nullable = false)
    private SeleccionProveedorEntity seleccionProveedor;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "unidad_medida", nullable = false, length = 10)
    private String unidadMedida;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalle;

    @Builder.Default
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeleccionProveedorItemPrecioEntity> precios = new ArrayList<>();
}
