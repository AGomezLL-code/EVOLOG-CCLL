package com.ccpll.gestor_logistico_api.controller.Auth;

import com.ccpll.gestor_logistico_api.dto.auth.JwtResponseDTO;
import com.ccpll.gestor_logistico_api.dto.auth.LoginRequestDTO;
import com.ccpll.gestor_logistico_api.entity.seguridad.UsuarioEntity;
import com.ccpll.gestor_logistico_api.repository.UsuarioRepository;
import com.ccpll.gestor_logistico_api.security.JwtService;
import com.ccpll.gestor_logistico_api.service.AuditoriaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        // 1. Validar credenciales con Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Obtener entidad completa de BD
        UsuarioEntity usuario = usuarioRepository.findByUsernameAndActivoTrue(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o inactivo"));

        // 3. Generar token JWT
        String jwtToken = jwtService.generarToken(usuario);


        auditoriaService.registrarConUsuario(
    usuario.getUsername(),
    "LOGIN",
    "Usuario",
    usuario.getId(),
    "Inicio de sesión exitoso - " + usuario.getNombreCompleto() + " (" + usuario.getRol().name() + ")"
);
        // 4. Devolver token + datos de perfil para Angular
        return ResponseEntity.ok(JwtResponseDTO.builder()
                .token(jwtToken)
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .area(usuario.getArea())
                .rol(usuario.getRol().name())
                .build());
    }
}
