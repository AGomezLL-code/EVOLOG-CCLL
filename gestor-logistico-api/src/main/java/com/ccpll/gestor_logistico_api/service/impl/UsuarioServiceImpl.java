package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.dto.ActualizarPerfilRequestDTO;
import com.ccpll.gestor_logistico_api.dto.CambiarPasswordRequestDTO;
import com.ccpll.gestor_logistico_api.dto.UsuarioPerfilResponseDTO;
import com.ccpll.gestor_logistico_api.entity.seguridad.UsuarioEntity;
import com.ccpll.gestor_logistico_api.repository.UsuarioRepository;
import com.ccpll.gestor_logistico_api.service.AuditoriaService;
import com.ccpll.gestor_logistico_api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional(readOnly = true)
    public UsuarioPerfilResponseDTO obtenerPerfilActual() {
        UsuarioEntity usuario = obtenerUsuarioAutenticado();
        return mapToDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioPerfilResponseDTO actualizarPerfil(ActualizarPerfilRequestDTO dto) {
        UsuarioEntity usuario = obtenerUsuarioAutenticado();

        usuario.setNombreCompleto(dto.getNombreCompleto().trim());
        usuario.setArea(dto.getArea().trim());

        UsuarioEntity guardado = usuarioRepository.save(usuario);

        auditoriaService.registrar(
                "ACTUALIZAR_PERFIL",
                "Usuario",
                guardado.getId(),
                "Actualizó sus datos de perfil: " + guardado.getNombreCompleto() + " (" + guardado.getArea() + ")"
        );

        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public Map<String, String> cambiarPassword(CambiarPasswordRequestDTO dto) {
        UsuarioEntity usuario = obtenerUsuarioAutenticado();

        // 1. Validar que la contraseña actual coincida
        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        // 2. Validar políticas de seguridad (longitud > 10, especial, mayúscula, número y sin datos personales/cargos)
        validarPasswordSegura(dto.getPasswordNuevo(), usuario);

        // 3. Validar que la nueva contraseña no sea idéntica a la anterior
        if (passwordEncoder.matches(dto.getPasswordNuevo(), usuario.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual.");
        }

        // 4. Encriptar y persistir la nueva contraseña
        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNuevo()));
        usuarioRepository.save(usuario);

        // 5. Registrar en la bitácora de auditoría
        auditoriaService.registrar(
                "CAMBIO_PASSWORD",
                "Usuario",
                usuario.getId(),
                "El usuario " + usuario.getUsername() + " actualizó su contraseña con éxito."
        );

        return Map.of("message", "Contraseña actualizada exitosamente.");
    }

    private void validarPasswordSegura(String nuevaPwd, UsuarioEntity usuario) {
        if (nuevaPwd == null || nuevaPwd.length() <= 10) {
            throw new IllegalArgumentException("La nueva contraseña debe tener más de 10 caracteres (mínimo 11).");
        }
        if (!nuevaPwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?¡¿°¬~`].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un carácter especial.");
        }
        if (!nuevaPwd.matches(".*[A-Z].*") || !nuevaPwd.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una letra mayúscula y un número.");
        }

        String pwdNormalizada = normalizarTexto(nuevaPwd);

        List<String> fuentesProhibidas = List.of(
                usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : "",
                usuario.getUsername() != null ? usuario.getUsername() : "",
                usuario.getArea() != null ? usuario.getArea() : "",
                usuario.getRol() != null ? usuario.getRol().name() : "",
                "jefe logistica asistente almacen auxiliar inventario comercial cliente interno ccll evolog"
        );

        for (String fuente : fuentesProhibidas) {
            String norm = normalizarTexto(fuente);
            for (String token : norm.split("[\\s._-]+")) {
                if (token.length() >= 3 && pwdNormalizada.contains(token)) {
                    throw new IllegalArgumentException(
                            "Por seguridad, la contraseña no puede contener tu nombre, usuario, área ni cargo.");
                }
            }
        }
    }

    private String normalizarTexto(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private UsuarioEntity obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("No existe una sesión autenticada activa.");
        }

        String username = auth.getName();
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o inactivo: " + username));
    }

    private UsuarioPerfilResponseDTO mapToDTO(UsuarioEntity entity) {
        return UsuarioPerfilResponseDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .nombreCompleto(entity.getNombreCompleto())
                .area(entity.getArea())
                .rol(entity.getRol() != null ? entity.getRol().name() : null)
                .activo(entity.getActivo())
                .fechaCreacion(entity.getFechaCreacion())
                .fotoUrl(entity.getFotoUrl())
                                .build();
    }

    @Override
@Transactional
public UsuarioPerfilResponseDTO actualizarFotoPerfil(String fotoBase64) {
    UsuarioEntity usuario = obtenerUsuarioAutenticado();
    usuario.setFotoUrl(fotoBase64);
    UsuarioEntity guardado = usuarioRepository.save(usuario);

    auditoriaService.registrar(
            "ACTUALIZAR_FOTO_PERFIL",
            "Usuario",
            guardado.getId(),
            "Actualizó su fotografía de perfil"
    );

    return mapToDTO(guardado);
}
}