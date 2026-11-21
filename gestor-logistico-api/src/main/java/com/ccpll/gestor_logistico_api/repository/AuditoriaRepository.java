package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.seguridad.AuditoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AuditoriaRepository extends JpaRepository<AuditoriaEntity, Integer> {
    List<AuditoriaEntity> findAllByOrderByFechaHoraDesc();
    List<AuditoriaEntity> findByEntidadOrderByFechaHoraDesc(String entidad);
}