package com.ccpll.gestor_logistico_api.config;

import com.ccpll.gestor_logistico_api.entity.eventos.AmbienteEntity;
import com.ccpll.gestor_logistico_api.entity.seguridad.UsuarioEntity;
import com.ccpll.gestor_logistico_api.entity.seguridad.UsuarioEntity.RolUsuario;
import com.ccpll.gestor_logistico_api.repository.AmbienteRepository;
import com.ccpll.gestor_logistico_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AmbienteRepository ambienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (ambienteRepository.count() == 0) {
            ambienteRepository.saveAll(List.of(
                    AmbienteEntity.builder()
                            .nombre("Auditorio Principal")
                            .capacidad(200)
                            .ubicacion("Piso 1 - Sede Central")
                            .activo(true)
                            .build(),
                    AmbienteEntity.builder()
                            .nombre("Sala de Directorio")
                            .capacidad(25)
                            .ubicacion("Piso 3 - Presidencia")
                            .activo(true)
                            .build(),
                    AmbienteEntity.builder()
                            .nombre("Sala de Capacitación")
                            .capacidad(50)
                            .ubicacion("Piso 2 - Ala Norte")
                            .activo(true)
                            .build(),
                    AmbienteEntity.builder()
                            .nombre("Hall / Patio Institucional")
                            .capacidad(120)
                            .ubicacion("Piso 1 - Ingreso")
                            .activo(true)
                            .build()
            ));
        }

        if (usuarioRepository.findByUsername("comercial.eventos").isEmpty()) {
            usuarioRepository.save(UsuarioEntity.builder()
                    .username("comercial.eventos")
                    .password(passwordEncoder.encode("123456"))
                    .nombreCompleto("Valeria Mendoza")
                    .area("Área Comercial y Eventos")
                    .rol(RolUsuario.AREA_COMERCIAL)
                    .activo(true)
                    .build());
        }
    }
}