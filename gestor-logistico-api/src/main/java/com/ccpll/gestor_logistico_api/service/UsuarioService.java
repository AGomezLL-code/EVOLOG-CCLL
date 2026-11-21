package com.ccpll.gestor_logistico_api.service;

import com.ccpll.gestor_logistico_api.dto.ActualizarPerfilRequestDTO;
import com.ccpll.gestor_logistico_api.dto.CambiarPasswordRequestDTO;
import com.ccpll.gestor_logistico_api.dto.UsuarioPerfilResponseDTO;

import java.util.Map;

public interface UsuarioService {

    UsuarioPerfilResponseDTO obtenerPerfilActual();

    UsuarioPerfilResponseDTO actualizarPerfil(ActualizarPerfilRequestDTO dto);

    Map<String, String> cambiarPassword(CambiarPasswordRequestDTO dto);

    UsuarioPerfilResponseDTO actualizarFotoPerfil(String fotoBase64);
}
