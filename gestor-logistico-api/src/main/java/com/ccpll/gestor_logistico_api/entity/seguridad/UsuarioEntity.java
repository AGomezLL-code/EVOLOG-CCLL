package com.ccpll.gestor_logistico_api.entity.seguridad;

import com.ccpll.gestor_logistico_api.entity.AuditoriaBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UsuarioEntity extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(nullable = false, length = 80)
    private String area; // <--- AGREGADO PARA AUTOCOMPLETAR REQUERIMIENTOS

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "foto_url", columnDefinition = "LONGTEXT")
    private String fotoUrl;

    public enum RolUsuario {
        JEFE_LOGISTICA, // Admin / Aprobador total (Aprueba, Rechaza, Despacha, Inventario)
        ASISTENTE, // Gestión de Almacén y Despachos
        AUXILIAR, // Soporte de Inventario y Kardex
        CLIENTE_INTERNO, // Solo crea requerimientos de su área y ve los suyos
        AREA_COMERCIAL
    }
}
