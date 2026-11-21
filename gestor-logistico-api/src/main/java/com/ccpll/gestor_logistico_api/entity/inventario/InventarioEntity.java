package com.ccpll.gestor_logistico_api.entity.inventario;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventarioEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String unidad;

    @Builder.Default
    @Column(name = "stock_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "costo_promedio", nullable = false, precision = 10, scale = 4)
    private BigDecimal costoPromedio = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo = BigDecimal.ZERO; // <--- Ajustado a ZERO

    @Version
    @Column(nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(name = "requiere_sku", nullable = false)
    private Boolean requiereSku = false;

    @Column(name = "ubicacion_fisica", length = 100)
    private String ubicacionFisica;
}