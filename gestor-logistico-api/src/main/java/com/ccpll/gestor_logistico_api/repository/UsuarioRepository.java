package com.ccpll.gestor_logistico_api.repository;

import com.ccpll.gestor_logistico_api.entity.seguridad.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    Optional<UsuarioEntity> findByUsername(String username);

    Optional<UsuarioEntity> findByUsernameAndActivoTrue(String username);

    boolean existsByUsername(String username);
}
