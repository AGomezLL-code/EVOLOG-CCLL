package com.ccpll.gestor_logistico_api.service;

import com.ccpll.gestor_logistico_api.entity.seguridad.AuditoriaEntity;
import java.util.List;

public interface AuditoriaService {

    // Registra tomando automáticamente el usuario autenticado del SecurityContext (JWT)
    void registrar(String accion, String entidad, Integer entidadId, String detalle);

    // Registra especificando el usuario explícitamente (ideal para el Login)
    void registrarConUsuario(String usuario, String accion, String entidad, Integer entidadId, String detalle);

    // Consulta la bitácora ordenada por fecha más reciente
    List<AuditoriaEntity> listarTodo();
}