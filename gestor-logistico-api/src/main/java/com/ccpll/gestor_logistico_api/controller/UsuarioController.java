package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.dto.ActualizarPerfilRequestDTO;
import com.ccpll.gestor_logistico_api.dto.CambiarPasswordRequestDTO;
import com.ccpll.gestor_logistico_api.dto.UsuarioPerfilResponseDTO;
import com.ccpll.gestor_logistico_api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Obtener los datos del perfil del usuario actualmente autenticado
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioPerfilResponseDTO> obtenerPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerPerfilActual());
    }

    // Actualizar datos básicos (nombre completo, área)
    @PutMapping("/perfil")
    public ResponseEntity<UsuarioPerfilResponseDTO> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(dto));
    }

    // Cambiar contraseña verificando la actual
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> cambiarPassword(@Valid @RequestBody CambiarPasswordRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.cambiarPassword(dto));
    }

    @PutMapping("/foto")
    public ResponseEntity<UsuarioPerfilResponseDTO> actualizarFoto(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(usuarioService.actualizarFotoPerfil(body.get("fotoUrl")));
    }
}
