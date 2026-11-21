package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.eventos.ReservaEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public interface ReservaEventoRepository extends JpaRepository<ReservaEventoEntity, Integer> {

    List<ReservaEventoEntity> findByFechaEventoOrderByHoraInicioAsc(LocalDate fechaEvento);

    List<ReservaEventoEntity> findAllByOrderByFechaEventoDescHoraInicioAsc();

    // Detecta si ya existe una reserva APROBADA que se cruce con el rango horario solicitado
    @Query("""
        SELECT COUNT(r) > 0 FROM ReservaEventoEntity r
        WHERE r.ambiente.id = :ambienteId
          AND r.fechaEvento = :fecha
          AND r.estado = 'APROBADO'
          AND r.id <> :excludeId
          AND (r.horaInicio < :horaFin AND r.horaFin > :horaInicio)
    """)
    boolean existeCruceAprobado(
            @Param("ambienteId") Integer ambienteId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excludeId") Integer excludeId
    );
}
