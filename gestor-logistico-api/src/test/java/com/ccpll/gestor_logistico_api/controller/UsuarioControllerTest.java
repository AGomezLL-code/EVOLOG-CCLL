package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.config.GlobalExceptionHandler;
import com.ccpll.gestor_logistico_api.dto.ActualizarPerfilRequestDTO;
import com.ccpll.gestor_logistico_api.dto.CambiarPasswordRequestDTO;
import com.ccpll.gestor_logistico_api.dto.UsuarioPerfilResponseDTO;
import com.ccpll.gestor_logistico_api.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/usuario/perfil debe retornar los datos del usuario en HTTP 200")
    void obtenerPerfil_Retorna200() throws Exception {
        UsuarioPerfilResponseDTO responseDTO = UsuarioPerfilResponseDTO.builder()
                .id(1)
                .username("carlos.silva")
                .nombreCompleto("Carlos Silva")
                .area("Logística")
                .rol("JEFE_LOGISTICA")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(usuarioService.obtenerPerfilActual()).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/usuario/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("carlos.silva"))
                .andExpect(jsonPath("$.nombreCompleto").value("Carlos Silva"))
                .andExpect(jsonPath("$.area").value("Logística"))
                .andExpect(jsonPath("$.rol").value("JEFE_LOGISTICA"));
    }

    @Test
    @DisplayName("PUT /api/v1/usuario/perfil con datos válidos debe retornar HTTP 200")
    void actualizarPerfil_DatosValidos_Retorna200() throws Exception {
        UsuarioPerfilResponseDTO responseDTO = UsuarioPerfilResponseDTO.builder()
                .id(1)
                .username("carlos.silva")
                .nombreCompleto("Carlos Actualizado")
                .area("Operaciones")
                .rol("JEFE_LOGISTICA")
                .activo(true)
                .build();

        when(usuarioService.actualizarPerfil(any(ActualizarPerfilRequestDTO.class))).thenReturn(responseDTO);

        String jsonBody = """
                {
                    "nombreCompleto": "Carlos Actualizado",
                    "area": "Operaciones"
                }
                """;

        mockMvc.perform(put("/api/v1/usuario/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("Carlos Actualizado"))
                .andExpect(jsonPath("$.area").value("Operaciones"));
    }

    @Test
    @DisplayName("PUT /api/v1/usuario/perfil con datos vacíos debe retornar HTTP 400 por validación")
    void actualizarPerfil_DatosInvalidos_Retorna400() throws Exception {
        String jsonBody = """
                {
                    "nombreCompleto": "",
                    "area": ""
                }
                """;

        mockMvc.perform(put("/api/v1/usuario/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/usuario/password con datos válidos debe retornar HTTP 200")
    void cambiarPassword_Valido_Retorna200() throws Exception {
        when(usuarioService.cambiarPassword(any(CambiarPasswordRequestDTO.class)))
                .thenReturn(Map.of("message", "Contraseña actualizada exitosamente."));

        String jsonBody = """
                {
                    "passwordActual": "clave123",
                    "passwordNuevo": "claveNueva456"
                }
                """;

        mockMvc.perform(put("/api/v1/usuario/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada exitosamente."));
    }

    @Test
    @DisplayName("PUT /api/v1/usuario/password con contraseña nueva demasiado corta debe retornar HTTP 400")
    void cambiarPassword_PasswordCorto_Retorna400() throws Exception {
        String jsonBody = """
                {
                    "passwordActual": "clave123",
                    "passwordNuevo": "123"
                }
                """;

        mockMvc.perform(put("/api/v1/usuario/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
