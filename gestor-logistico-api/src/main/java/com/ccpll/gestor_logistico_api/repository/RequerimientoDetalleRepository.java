package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;



public interface RequerimientoDetalleRepository extends JpaRepository<RequerimientoDetalleEntity, Integer> {
}