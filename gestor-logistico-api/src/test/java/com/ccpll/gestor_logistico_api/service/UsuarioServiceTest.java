package com.ccpll.gestor_logistico_api.service;

import com.ccpll.gestor_logistico_api.dto.ActualizarPerfilRequestDTO;
import com.ccpll.gestor_logistico_api.dto.CambiarPasswordRequestDTO;
import com.ccpll.gestor_logistico_api.dto.UsuarioPerfilResponseDTO;
import com.ccpll.gestor_logistico_api.entity.seguridad.UsuarioEntity;
import com.ccpll.gestor_logistico_api.entity.seguridad.UsuarioEntity.RolUsuario;
import com.ccpll.gestor_logistico_api.repository.UsuarioRepository;
import com.ccpll.gestor_logistico_api.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioEntity usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = UsuarioEntity.builder()
                .id(1)
                .username("carlos.silva")
                .password("$2a$10$encodedHashEjemplo123")
                .nombreCompleto("Carlos Silva")
                .area("Logística y Almacén")
                .rol(RolUsuario.JEFE_LOGISTICA)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Configurar simulación del usuario autenticado en SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("carlos.silva");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Debe obtener el perfil del usuario autenticado exitosamente")
    void obtenerPerfilActual_Exitoso() {
        when(usuarioRepository.findByUsernameAndActivoTrue("carlos.silva")).thenReturn(Optional.of(usuarioMock));

        UsuarioPerfilResponseDTO response = usuarioService.obtenerPerfilActual();

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("carlos.silva", response.getUsername());
        assertEquals("Carlos Silva", response.getNombreCompleto());
        assertEquals("Logística y Almacén", response.getArea());
        assertEquals("JEFE_LOGISTICA", response.getRol());
        assertTrue(response.getActivo());

        verify(usuarioRepository).findByUsernameAndActivoTrue("carlos.silva");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario autenticado no existe en la BD")
    void obtenerPerfilActual_UsuarioNoExiste_LanzaExcepcion() {
        when(usuarioRepository.findByUsernameAndActivoTrue("carlos.silva")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                usuarioService.obtenerPerfilActual()
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado o inactivo"));
        verify(usuarioRepository).findByUsernameAndActivoTrue("carlos.silva");
    }

    @Test
    @DisplayName("Debe actualizar los datos de perfil y registrar en auditoría")
    void actualizarPerfil_Exitoso() {
        when(usuarioRepository.findByUsernameAndActivoTrue("carlos.silva")).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarPerfilRequestDTO request = ActualizarPerfilRequestDTO.builder()
                .nombreCompleto("Carlos A. Silva Perez")
                .area("Gerencia de Operaciones")
                .build();

        UsuarioPerfilResponseDTO response = usuarioService.actualizarPerfil(request);

        assertNotNull(response);
        assertEquals("Carlos A. Silva Perez", response.getNombreCompleto());
        assertEquals("Gerencia de Operaciones", response.getArea());

        verify(usuarioRepository).save(usuarioMock);
        verify(auditoriaService).registrar(
                eq("ACTUALIZAR_PERFIL"),
                eq("Usuario"),
                eq(1),
                contains("Carlos A. Silva Perez")
        );
    }

    @Test
    @DisplayName("Debe cambiar la contraseña con éxito cuando la contraseña actual es correcta")
    void cambiarPassword_Exitoso() {
        when(usuarioRepository.findByUsernameAndActivoTrue("carlos.silva")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("claveActual123", usuarioMock.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("claveNuevaSegura456", usuarioMock.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("claveNuevaSegura456")).thenReturn("$2a$10$nuevoHashSeguro456");

        CambiarPasswordRequestDTO request = CambiarPasswordRequestDTO.builder()
                .passwordActual("claveActual123")
                .passwordNuevo("claveNuevaSegura456")
                .build();

        Map<String, String> response = usuarioService.cambiarPassword(request);

        assertNotNull(response);
        assertEquals("Contraseña actualizada exitosamente.", response.get("message"));
        assertEquals("$2a$10$nuevoHashSeguro456", usuarioMock.getPassword());

        verify(usuarioRepository).save(usuarioMock);
        verify(auditoriaService).registrar(
                eq("CAMBIO_PASSWORD"),
                eq("Usuario"),
                eq(1),
                contains("actualizó su contraseña")
        );
    }

    @Test
    @DisplayName("Debe fallar si la contraseña actual ingresada es incorrecta")
    void cambiarPassword_PasswordActualIncorrecto_LanzaExcepcion() {
        when(usuarioRepository.findByUsernameAndActivoTrue("carlos.silva")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("claveErronea", usuarioMock.getPassword())).thenReturn(false);

        CambiarPasswordRequestDTO request = CambiarPasswordRequestDTO.builder()
                .passwordActual("claveErronea")
                .passwordNuevo("claveNueva123")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.cambiarPassword(request)
        );

        assertEquals("La contraseña actual es incorrecta.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(auditoriaService, never()).registrar(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe fallar si la nueva contraseña es idéntica a la contraseña actual")
    void cambiarPassword_PasswordNuevoIgualAlAnterior_LanzaExcepcion() {
        when(usuarioRepository.findByUsernameAndActivoTrue("carlos.silva")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("claveActual123", usuarioMock.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("claveActual123", usuarioMock.getPassword())).thenReturn(true);

        CambiarPasswordRequestDTO request = CambiarPasswordRequestDTO.builder()
                .passwordActual("claveActual123")
                .passwordNuevo("claveActual123")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.cambiarPassword(request)
        );

        assertEquals("La nueva contraseña no puede ser igual a la actual.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(auditoriaService, never()).registrar(any(), any(), any(), any());
    }
}
