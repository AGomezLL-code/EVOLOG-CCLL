package com.ccpll.gestor_logistico_api.service;

import com.ccpll.gestor_logistico_api.dto.CategoriaRequestDTO;
import com.ccpll.gestor_logistico_api.dto.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDTO> listarTodas();

    CategoriaResponseDTO obtenerPorId(Integer id);

    CategoriaResponseDTO crear(CategoriaRequestDTO dto);
}
