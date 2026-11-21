package com.ccpll.gestor_logistico_api.service.impl;


import com.ccpll.gestor_logistico_api.dto.CategoriaRequestDTO;
import com.ccpll.gestor_logistico_api.dto.CategoriaResponseDTO;
import com.ccpll.gestor_logistico_api.repository.CategoriaRepository;
import com.ccpll.gestor_logistico_api.service.CategoriaService;
import com.ccpll.gestor_logistico_api.entity.inventario.CategoriaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Integer id) {
        CategoriaEntity entity = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una categoría registrada con el nombre: " + dto.getNombre());
        }

        CategoriaEntity nuevaCategoria = CategoriaEntity.builder()
                .nombre(dto.getNombre())
                .build();

        return mapToDTO(categoriaRepository.save(nuevaCategoria));
    }

    private CategoriaResponseDTO mapToDTO(CategoriaEntity entity) {
        return CategoriaResponseDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .build();
    }

    
}