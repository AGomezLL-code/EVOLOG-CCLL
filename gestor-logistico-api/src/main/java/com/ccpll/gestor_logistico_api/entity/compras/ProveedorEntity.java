package com.ccpll.gestor_logistico_api.entity.compras;
import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProveedorEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 20)
    private String ruc;

    @Column(name = "actividad_comercial", length = 200)
    private String actividadComercial;

    @Column(length = 100)
    private String telefono;

    @Column(length = 300)
    private String direccion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
