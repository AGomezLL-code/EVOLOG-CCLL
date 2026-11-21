package com.ccpll.gestor_logistico_api.entity.compras;
import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "evaluacion_proveedor")
public class EvaluacionProveedorEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompraEntity ordenCompra;

    @Column(name = "cantidad_porcentaje", precision = 5, scale = 2)
    private BigDecimal cantidadPorcentaje;

    @Column(name = "calidad_porcentaje", precision = 5, scale = 2)
    private BigDecimal calidadPorcentaje;

    @Column(name = "medio_ambiente_producto", precision = 5, scale = 2)
    private BigDecimal medioAmbienteProducto;

    @Column(name = "medio_ambiente_servicio", precision = 5, scale = 2)
    private BigDecimal medioAmbienteServicio;

    @Column(precision = 5, scale = 2)
    private BigDecimal antisoborno;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_evaluacion")
    private LocalDateTime fechaEvaluacion;

    @Column(length = 50)
    private String evaluador;

    @Column(name = "promedio_total", precision = 5, scale = 2)
    private BigDecimal promedioTotal;

    @Column(name = "nivel_desempeno", length = 50)
    private String nivelDesempeno;
}
