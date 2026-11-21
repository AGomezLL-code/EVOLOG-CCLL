package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.dto.*;
import com.ccpll.gestor_logistico_api.repository.ActivoSerieRepository;
import com.ccpll.gestor_logistico_api.repository.CategoriaRepository;
import com.ccpll.gestor_logistico_api.repository.HistorialInventarioRepository;
import com.ccpll.gestor_logistico_api.service.InventarioService;
import com.ccpll.gestor_logistico_api.repository.InventarioRepository;
import com.ccpll.gestor_logistico_api.entity.inventario.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final HistorialInventarioRepository historialRepository;
    private final ActivoSerieRepository activoSerieRepository; // <--- NUEVO REPOSITORIO DE SERIES

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponseDTO> listarTodos() {
        return inventarioRepository.findByActivoTrue().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponseDTO obtenerPorId(Integer id) {
        InventarioEntity entity = inventarioRepository.findByIdConCategoria(id)
                .orElseThrow(() -> new RuntimeException("Producto de inventario no encontrado con ID: " + id));
        return mapToResponseDTO(entity);
    }

   @Override
@Transactional
public InventarioResponseDTO crear(InventarioRequestDTO dto) {
    if (inventarioRepository.existsByCodigo(dto.getCodigo())) {
        throw new IllegalArgumentException("Ya existe un producto registrado con el código: " + dto.getCodigo());
    }

    CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getCategoriaId()));

    Boolean requiereSku = dto.getRequiereSku() != null ? dto.getRequiereSku() : false;
    BigDecimal stockInicial = dto.getStockActual() != null ? dto.getStockActual() : BigDecimal.ZERO;

    InventarioEntity nuevoProducto = InventarioEntity.builder()
            .codigo(dto.getCodigo())
            .descripcion(dto.getDescripcion())
            .unidad(dto.getUnidad())
            .stockActual(stockInicial)
            .costoPromedio(dto.getCostoPromedio() != null ? dto.getCostoPromedio() : BigDecimal.ZERO)
            .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : BigDecimal.ZERO)
            .categoria(categoria)
            .activo(dto.getActivo() != null ? dto.getActivo() : true)
            .requiereSku(requiereSku)
            .ubicacionFisica(dto.getUbicacionFisica())
            .build();

    InventarioEntity guardado = inventarioRepository.save(nuevoProducto);

    // Si requiere SKU y se especificó un stock inicial > 0, genera las series de inmediato
    if (Boolean.TRUE.equals(requiereSku) && stockInicial.compareTo(BigDecimal.ZERO) > 0) {
        generarSkusAutomaticos(guardado, stockInicial.intValue());
    }

    return mapToResponseDTO(guardado);
}

@Override
@Transactional
public InventarioResponseDTO registrarEntrada(EntradaInventarioRequestDTO dto) {
    InventarioEntity producto = inventarioRepository.findByIdConCategoria(dto.getArticuloId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + dto.getArticuloId()));

    BigDecimal stockAnterior = producto.getStockActual();
    BigDecimal costoAnterior = producto.getCostoPromedio();
    BigDecimal valorTotalAnterior = stockAnterior.multiply(costoAnterior);

    BigDecimal subtotalEntrada = dto.getCantidad().multiply(dto.getCostoUnitario());
    BigDecimal nuevoStock = stockAnterior.add(dto.getCantidad());

    BigDecimal nuevoCostoPromedio = BigDecimal.ZERO;
    if (nuevoStock.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal valorTotalNuevo = valorTotalAnterior.add(subtotalEntrada);
        nuevoCostoPromedio = valorTotalNuevo.divide(nuevoStock, 4, RoundingMode.HALF_UP);
    }

    producto.setStockActual(nuevoStock);
    producto.setCostoPromedio(nuevoCostoPromedio);
    inventarioRepository.save(producto);

    // LÓGICA DE SKUs AUTOMÁTICOS EN ENTRADAS DE ALMACÉN
    String skuParaHistorial = dto.getSku();
    if (Boolean.TRUE.equals(producto.getRequiereSku())) {
        if (dto.getSku() != null && !dto.getSku().isBlank()) {
            // Si el usuario ingresó un SKU manual para 1 equipo
            ActivoSerieEntity activo = ActivoSerieEntity.builder()
                    .producto(producto)
                    .sku(dto.getSku().trim().toUpperCase())
                    .estado(ActivoSerieEntity.EstadoActivo.DISPONIBLE)
                    .build();
            activoSerieRepository.save(activo);
        } else {
            // Generación automática de SKUs para el lote de equipos que ingresa
            generarSkusAutomaticos(producto, dto.getCantidad().intValue());
            skuParaHistorial = "LOTE-AUTOGENERADO (" + dto.getCantidad().intValue() + " SKUs)";
        }
    }

    HistorialInventarioEntity historial = HistorialInventarioEntity.builder()
            .articulo(producto)
            .skuAsignado(skuParaHistorial)
            .tipo(HistorialInventarioEntity.TipoMovimiento.ENTRADA)
            .cantidad(dto.getCantidad())
            .costoUnitario(dto.getCostoUnitario())
            .subtotal(subtotalEntrada)
            .fecha(dto.getFecha())
            .proveedorCliente(dto.getProveedor())
            .area(dto.getAreaDestino())
            .stockAnterior(stockAnterior)
            .stockNuevo(nuevoStock)
            .build();

    historialRepository.save(historial);

    return mapToResponseDTO(producto);
}

// MÉTODO AUXILIAR PARA GENERACIÓN AUTOMÁTICA DE SKUs ÚNICOS
private void generarSkusAutomaticos(InventarioEntity producto, int cantidad) {
    long correlativoActual = activoSerieRepository.countByProductoId(producto.getId());
    for (int i = 1; i <= cantidad; i++) {
        long numeroSerie = correlativoActual + i;
        String skuGenerado = String.format("SKU-%s-%04d", producto.getCodigo().trim().toUpperCase(), numeroSerie);
        
        ActivoSerieEntity activo = ActivoSerieEntity.builder()
                .producto(producto)
                .sku(skuGenerado)
                .estado(ActivoSerieEntity.EstadoActivo.DISPONIBLE)
                .build();
        activoSerieRepository.save(activo);
    }
}

    @Override
@Transactional
public InventarioResponseDTO registrarSalida(SalidaInventarioRequestDTO dto) {
    InventarioEntity producto = inventarioRepository.findByIdConCategoria(dto.getArticuloId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + dto.getArticuloId()));

    BigDecimal stockAnterior = producto.getStockActual() != null ? producto.getStockActual() : BigDecimal.ZERO;
    BigDecimal nuevoStock;

    // LÓGICA SEGÚN TIPO DE PRODUCTO
    if (Boolean.TRUE.equals(producto.getRequiereSku())) {
        // Para activos con SKU (Laptops, Proyectores): 
        // El stock actual es la cantidad real de series que permanecen DISPONIBLES
        long seriesDisponibles = activoSerieRepository.countByProductoIdAndEstado(
                producto.getId(), ActivoSerieEntity.EstadoActivo.DISPONIBLE);
        nuevoStock = BigDecimal.valueOf(seriesDisponibles);
    } else {
        // Para insumos generales sin SKU: Se realiza la resta convencional
        if (stockAnterior.compareTo(dto.getCantidad()) < 0) {
            throw new IllegalArgumentException("Stock insuficiente. Stock disponible: " + stockAnterior 
                    + ", Cantidad solicitada: " + dto.getCantidad());
        }
        nuevoStock = stockAnterior.subtract(dto.getCantidad());
    }

    // Actualizar el stock final consolidado
    producto.setStockActual(nuevoStock);
    inventarioRepository.save(producto);

    // Registrar el movimiento de salida en el Kardex
    HistorialInventarioEntity historial = HistorialInventarioEntity.builder()
            .articulo(producto)
            .skuAsignado(dto.getSku())
            .tipo(HistorialInventarioEntity.TipoMovimiento.SALIDA)
            .cantidad(dto.getCantidad())
            .costoUnitario(producto.getCostoPromedio())
            .subtotal(dto.getCantidad().multiply(producto.getCostoPromedio()))
            .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now())
            .proveedorCliente(dto.getCliente())
            .area(dto.getAreaSolicita())
            .stockAnterior(stockAnterior)
            .stockNuevo(nuevoStock)
            .build();

    historialRepository.save(historial);

    return mapToResponseDTO(producto);
}

    private InventarioResponseDTO mapToResponseDTO(InventarioEntity entity) {
        return InventarioResponseDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descripcion(entity.getDescripcion())
                .unidad(entity.getUnidad())
                .stockActual(entity.getStockActual())
                .costoPromedio(entity.getCostoPromedio())
                .stockMinimo(entity.getStockMinimo())
                .version(entity.getVersion())
                .categoriaId(entity.getCategoria() != null ? entity.getCategoria().getId() : null)
                .categoriaNombre(entity.getCategoria() != null ? entity.getCategoria().getNombre() : "SIN CATEGORÍA")
                .activo(entity.getActivo())
                .requiereSku(entity.getRequiereSku())             // <--- NUEVO
                .ubicacionFisica(entity.getUbicacionFisica())     // <--- NUEVO
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaInventarioResponseDTO> obtenerKardexPorProducto(Integer articuloId, LocalDate fechaInicio, LocalDate fechaFin) {
        return historialRepository.obtenerKardexPorProductoYRangoFechas(articuloId, fechaInicio, fechaFin)
            .stream()
            .map(this::mapToHistorialDTO)
            .collect(Collectors.toList());
    }

    private HistoriaInventarioResponseDTO mapToHistorialDTO(HistorialInventarioEntity entity) {
        return HistoriaInventarioResponseDTO.builder()
                .id(entity.getId())
                .articuloId(entity.getArticulo().getId())
                .articuloCodigo(entity.getArticulo().getCodigo())
                .articuloDescripcion(entity.getArticulo().getDescripcion())
                .skuAsignado(entity.getSkuAsignado()) // <--- NUEVO
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
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenInventarioDTO obtenerResumenKPI() {
        Long totalProductos = inventarioRepository.countByActivoTrue();
        BigDecimal valorTotal = inventarioRepository.calcularValorTotalInventario();
        Long stockBajo = inventarioRepository.contarProductosConStockBajo();

        return ResumenInventarioDTO.builder()
                .totalProductos(totalProductos)
                .valorTotalInventario(valorTotal)
                .cantidadStockBajo(stockBajo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponseDTO> obtenerProductosStockBajo() {
        return inventarioRepository.findProductosConStockBajo()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // <--- NUEVO MÉTODO PARA CONSULTAR LAS SERIES/SKUS DE UN PRODUCTO (LAPTOPS/PROYECTORES)
    @Override
    @Transactional(readOnly = true)
    public List<ActivoSerieDTO> obtenerSkusPorProducto(Integer productoId) {
        return activoSerieRepository.findByProductoId(productoId).stream()
                .map(activo -> ActivoSerieDTO.builder()
                        .id(activo.getId())
                        .sku(activo.getSku())
                        .estado(activo.getEstado().name())
                        .build())
                .collect(Collectors.toList());
    }
}