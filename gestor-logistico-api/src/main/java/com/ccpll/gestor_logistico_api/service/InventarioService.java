package com.ccpll.gestor_logistico_api.service;

import com.ccpll.gestor_logistico_api.dto.InventarioRequestDTO;
import com.ccpll.gestor_logistico_api.dto.InventarioResponseDTO;
import com.ccpll.gestor_logistico_api.dto.ResumenInventarioDTO;
import com.ccpll.gestor_logistico_api.dto.ActivoSerieDTO;
import com.ccpll.gestor_logistico_api.dto.EntradaInventarioRequestDTO;
import com.ccpll.gestor_logistico_api.dto.SalidaInventarioRequestDTO;
import com.ccpll.gestor_logistico_api.dto.HistoriaInventarioResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface InventarioService {

    List<InventarioResponseDTO> listarTodos();

    InventarioResponseDTO obtenerPorId(Integer id);

    InventarioResponseDTO crear(InventarioRequestDTO dto);

    InventarioResponseDTO registrarEntrada(EntradaInventarioRequestDTO dto);

    InventarioResponseDTO registrarSalida(SalidaInventarioRequestDTO dto);

    List<HistoriaInventarioResponseDTO> obtenerKardexPorProducto(Integer articuloId, LocalDate fechaInicio, LocalDate fechaFin);

    ResumenInventarioDTO obtenerResumenKPI();

    List<InventarioResponseDTO> obtenerProductosStockBajo();

    List<ActivoSerieDTO> obtenerSkusPorProducto(Integer productoId);
}
