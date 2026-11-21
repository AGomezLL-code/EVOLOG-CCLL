package com.ccpll.gestor_logistico_api.entity.inventario;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificacionStockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private InventarioEntity articulo;

    @Column(name = "stock_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockActual;

    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    @Column(name = "fecha_notificacion", nullable = false)
    private LocalDateTime fechaNotificacion;

    @Builder.Default
    @Column(length = 20)
    private String estado = "PENDIENTE";

    @Column(length = 500)
    private String mensaje;
}
