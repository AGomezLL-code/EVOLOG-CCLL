package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.entity.seguridad.AuditoriaEntity;
import com.ccpll.gestor_logistico_api.repository.AuditoriaRepository;
import com.ccpll.gestor_logistico_api.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String entidad, Integer entidadId, String detalle) {
        registrarConUsuario(obtenerUsuarioActual(), accion, entidad, entidadId, detalle);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarConUsuario(String usuario, String accion, String entidad, Integer entidadId, String detalle) {
        AuditoriaEntity log = AuditoriaEntity.builder()
                .accion(accion)
                .entidad(entidad)
                .entidadId(entidadId)
                .detalle(detalle)
                .fechaHora(LocalDateTime.now())
                .ipOrigen(obtenerIpCliente())
                .usuario(usuario != null ? usuario : "SISTEMA")
                .build();

        auditoriaRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaEntity> listarTodo() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "SISTEMA";
        }
        return auth.getName();
    }

    private String obtenerIpCliente() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}