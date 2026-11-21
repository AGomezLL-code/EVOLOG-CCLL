package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.dto.HistoriaInventarioResponseDTO;
import com.ccpll.gestor_logistico_api.repository.HistorialInventarioRepository;
import com.ccpll.gestor_logistico_api.service.HistorialInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialInventarioServiceImpl implements HistorialInventarioService {

    private final HistorialInventarioRepository historialInventarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaInventarioResponseDTO> listarTodos() {
        return historialInventarioRepository.findAll().stream()
                .map(entity -> HistoriaInventarioResponseDTO.builder()
                        .id(entity.getId())
                        .articuloId(entity.getArticulo() != null ? entity.getArticulo().getId() : null)
                        .articuloCodigo(entity.getArticulo() != null ? entity.getArticulo().getCodigo() : "")
                        .articuloDescripcion(entity.getArticulo() != null ? entity.getArticulo().getDescripcion() : "")
                        .tipo(entity.getTipo())
                        .cantidad(entity.getCantidad())
                        .costoUnitario(entity.getCostoUnitario())
                        .subtotal(entity.getSubtotal())
                        .fecha(entity.getFecha())
                        .proveedorCliente(entity.getProveedorCliente())
                        .area(entity.getArea())
                        .stockAnterior(entity.getStockAnterior())
                        .stockNuevo(entity.getStockNuevo())
                        .fechaCreacion(entity.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());
    }
}