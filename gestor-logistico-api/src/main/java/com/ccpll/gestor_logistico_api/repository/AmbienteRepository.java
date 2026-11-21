package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.eventos.AmbienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AmbienteRepository extends JpaRepository<AmbienteEntity, Integer> {
    List<AmbienteEntity> findByActivoTrueOrderByIdAsc();
}
