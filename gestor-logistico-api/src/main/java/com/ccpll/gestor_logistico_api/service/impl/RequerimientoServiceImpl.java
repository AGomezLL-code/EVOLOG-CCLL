package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.dto.DevolucionActivoRequestDTO;
import com.ccpll.gestor_logistico_api.dto.RequerimientoDetalleRequestDTO;
import com.ccpll.gestor_logistico_api.dto.RequerimientoRequestDTO;
import com.ccpll.gestor_logistico_api.dto.RequerimientoResponseDTO;
import com.ccpll.gestor_logistico_api.dto.SalidaInventarioRequestDTO;
import com.ccpll.gestor_logistico_api.entity.inventario.ActivoSerieEntity;
import com.ccpll.gestor_logistico_api.entity.inventario.HistorialInventarioEntity;
import com.ccpll.gestor_logistico_api.entity.inventario.InventarioEntity;
import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoDetalleEntity;
import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity;
import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity.EstadoRequerimiento;
import com.ccpll.gestor_logistico_api.repository.ActivoSerieRepository;
import com.ccpll.gestor_logistico_api.repository.HistorialInventarioRepository;
import com.ccpll.gestor_logistico_api.repository.InventarioRepository;
import com.ccpll.gestor_logistico_api.repository.RequerimientoDetalleRepository;
import com.ccpll.gestor_logistico_api.repository.RequerimientoRepository;
import com.ccpll.gestor_logistico_api.service.AuditoriaService;
import com.ccpll.gestor_logistico_api.service.InventarioService;
import com.ccpll.gestor_logistico_api.service.RequerimientoService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequerimientoServiceImpl implements RequerimientoService {

    private final RequerimientoRepository requerimientoRepository;
    private final RequerimientoDetalleRepository detalleRepository;
    private final InventarioRepository inventarioRepository;
    private final ActivoSerieRepository activoSerieRepository;
    private final HistorialInventarioRepository historialRepository;
    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;
    
    @Override
    @Transactional
    public RequerimientoResponseDTO crearRequerimiento(RequerimientoRequestDTO dto) {
        String codigoGenerado = generarCodigoRequerimiento();

        RequerimientoEntity requerimiento = RequerimientoEntity.builder()
                .codigo(codigoGenerado)
                .areaSolicitante(dto.getAreaSolicitante())
                .solicitante(dto.getSolicitante())
                .observacion(dto.getObservacion())
                .estado(EstadoRequerimiento.PENDIENTE)
                .fechaSolicitud(LocalDate.now())
                .build();

        for (RequerimientoDetalleRequestDTO detDto : dto.getDetalles()) {
            InventarioEntity articulo = inventarioRepository.findById(detDto.getArticuloId())
                    .orElseThrow(() -> new RuntimeException("Artículo no encontrado con ID: " + detDto.getArticuloId()));

            RequerimientoDetalleEntity detalle = RequerimientoDetalleEntity.builder()
                    .articulo(articulo)
                    .skuAsignado(detDto.getSkuAsignado())
                    .cantidadSolicitada(detDto.getCantidadSolicitada())
                    .productoServicio(articulo.getDescripcion() != null ? articulo.getDescripcion() : "BIEN/SERVICIO")
                    .unidadMedida(articulo.getUnidad() != null ? articulo.getUnidad() : "UND")
                    .caracteristicas(detDto.getCaracteristicas())
                    .sustento(detDto.getSustento())
                    .build();

            requerimiento.agregarDetalle(detalle);
        }

        RequerimientoEntity guardado = requerimientoRepository.save(requerimiento);
        auditoriaService.registrar(
                "CREAR_REQUERIMIENTO",
                "Requerimiento",
                guardado.getId(),
                "Solicitud " + guardado.getCodigo() + " creada por " + guardado.getSolicitante() + " (Área: " + guardado.getAreaSolicitante() + ")"
        );
        return mapToDTO(guardado);

      
    }

    @Override
@Transactional(readOnly = true)
public List<RequerimientoResponseDTO> listarTodos() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

    boolean esClienteInterno = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE_INTERNO"));

    // Si es CLIENTE_INTERNO, solo devuelve los requerimientos que él mismo creó
    if (esClienteInterno) {
        return requerimientoRepository.findByCreadoPorConDetalles(username).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Si es JEFE_LOGISTICA, ASISTENTE o AUXILIAR, devuelve todos los requerimientos
    return requerimientoRepository.findAllConDetalles().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
}

    @Override
    @Transactional(readOnly = true)
    public RequerimientoResponseDTO obtenerPorId(Integer id) {
        RequerimientoEntity req = requerimientoRepository.findByIdConDetalles(id)
                .orElseThrow(() -> new RuntimeException("Requerimiento no encontrado con ID: " + id));
        return mapToDTO(req);
    }

    @Override
@Transactional
public RequerimientoResponseDTO cambiarEstado(Integer id, EstadoRequerimiento nuevoEstado, String observaciones) {
    RequerimientoEntity requerimiento = requerimientoRepository.findByIdConDetalles(id)
            .orElseThrow(() -> new RuntimeException("Requerimiento no encontrado con ID: " + id));

    if (requerimiento.getEstado() == EstadoRequerimiento.DESPACHADO || requerimiento.getEstado() == EstadoRequerimiento.ENTREGADO) {
        throw new IllegalStateException("El requerimiento ya fue despachado/entregado y no puede cambiar de estado.");
    }

    if (nuevoEstado == EstadoRequerimiento.DESPACHADO || nuevoEstado == EstadoRequerimiento.ENTREGADO) {
        boolean tieneEquiposPrestamo = false;

        for (RequerimientoDetalleEntity det : requerimiento.getDetalles()) {
            det.setCantidadAtendida(det.getCantidadSolicitada());
            InventarioEntity producto = det.getArticulo();

            // LÓGICA SI EL ARTÍCULO REQUIERE TRAZABILIDAD POR SKU
            if (Boolean.TRUE.equals(producto.getRequiereSku())) {
                tieneEquiposPrestamo = true;
                int cantidadRequerida = det.getCantidadSolicitada().intValue();

                List<String> skusAsignadosList = new ArrayList<>();

                // Asignar serie por serie garantizando persistencia inmediata
                for (int i = 0; i < cantidadRequerida; i++) {
                    List<ActivoSerieEntity> disponibles = activoSerieRepository
                            .findByProductoIdAndEstado(producto.getId(), ActivoSerieEntity.EstadoActivo.DISPONIBLE);

                    if (disponibles.isEmpty()) {
                        throw new IllegalStateException(String.format(
                            "Stock de series insuficiente para '%s'. Requeridas: %d, Disponibles: %d.",
                            producto.getDescripcion(), cantidadRequerida, skusAsignadosList.size()
                        ));
                    }

                    ActivoSerieEntity activo = disponibles.get(0);
                    activo.setEstado(ActivoSerieEntity.EstadoActivo.PRESTADO);
                    activoSerieRepository.saveAndFlush(activo); // Persistir cambio en BD inmediatamente
                    skusAsignadosList.add(activo.getSku());
                }

                det.setSkuAsignado(String.join(", ", skusAsignadosList));
                detalleRepository.save(det);

                // =========================================================================
                // AQUÍ VA LA SINCRONIZACIÓN DEL STOCK EN INVENTARIO
                // =========================================================================
                long stockDisponibleReal = activoSerieRepository.countByProductoIdAndEstado(
                        producto.getId(), ActivoSerieEntity.EstadoActivo.DISPONIBLE);
                producto.setStockActual(BigDecimal.valueOf(stockDisponibleReal));
                inventarioRepository.save(producto);
            }

            // Registrar la salida en Inventario / Kardex
            SalidaInventarioRequestDTO salidaDTO = SalidaInventarioRequestDTO.builder()
                    .articuloId(producto.getId())
                    .sku(det.getSkuAsignado())
                    .cantidad(det.getCantidadSolicitada())
                    .fecha(LocalDate.now())
                    .cliente("Atención Requerimiento " + requerimiento.getCodigo() + " - " + requerimiento.getSolicitante())
                    .areaSolicita(requerimiento.getAreaSolicitante())
                    .build();

            inventarioService.registrarSalida(salidaDTO);
        }

        requerimiento.setFechaDespacho(LocalDateTime.now());
        nuevoEstado = tieneEquiposPrestamo ? EstadoRequerimiento.ENTREGADO : EstadoRequerimiento.DESPACHADO;
    }

    requerimiento.setEstado(nuevoEstado);
    if (observaciones != null && !observaciones.isBlank()) {
        requerimiento.setObservacion(observaciones);
    }

    RequerimientoEntity actualizado = requerimientoRepository.save(requerimiento);

    auditoriaService.registrar(
    nuevoEstado.name(),
    "Requerimiento",
    requerimiento.getId(),
    "Requerimiento " + requerimiento.getCodigo() + " cambió a estado " + nuevoEstado.name()
);
    return mapToDTO(actualizado);
}
    // REGISTRO DE DEVOLUCIÓN DE EQUIPOS/LAPTOPS EN PRÉSTAMO
@Override
    @Transactional
    public RequerimientoResponseDTO registrarDevolucionActivo(DevolucionActivoRequestDTO dto) {
        RequerimientoDetalleEntity detalle = detalleRepository.findById(dto.getRequerimientoDetalleId())
                .orElseThrow(() -> new RuntimeException("Detalle de requerimiento no encontrado"));

        if (Boolean.TRUE.equals(detalle.getDevuelto())) {
            throw new IllegalStateException("Este ítem ya fue devuelto anteriormente.");
        }

        InventarioEntity producto = detalle.getArticulo();
        String skuDevuelto = (dto.getSku() != null && !dto.getSku().isBlank()) ? dto.getSku() : detalle.getSkuAsignado();

        if (skuDevuelto == null || skuDevuelto.isBlank() || skuDevuelto.equals("-")) {
            throw new IllegalArgumentException("No se ha especificado ningún SKU válido para la devolución.");
        }

        // 1. Separar la lista de SKUs de este detalle y pasarlos a DISPONIBLE
        List<String> skusList = Arrays.stream(skuDevuelto.split(","))
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        for (String skuSingle : skusList) {
            ActivoSerieEntity activoSerie = activoSerieRepository.findBySku(skuSingle)
                    .orElseThrow(() -> new RuntimeException("Serie/SKU no encontrado: " + skuSingle));
            activoSerie.setEstado(ActivoSerieEntity.EstadoActivo.DISPONIBLE);
            activoSerieRepository.save(activoSerie);
        }

        // 2. Recalcular stock actual de forma exacta según series disponibles en BD
        BigDecimal stockAnt = producto.getStockActual();
        if (Boolean.TRUE.equals(producto.getRequiereSku())) {
            long seriesDisponiblesCount = activoSerieRepository.countByProductoIdAndEstado(
                    producto.getId(), ActivoSerieEntity.EstadoActivo.DISPONIBLE);
            producto.setStockActual(BigDecimal.valueOf(seriesDisponiblesCount));
        } else {
            producto.setStockActual(stockAnt.add(detalle.getCantidadSolicitada()));
        }
        inventarioRepository.save(producto);

        // 3. Registrar movimiento DEVOLUCIÓN en Kardex
        HistorialInventarioEntity historial = HistorialInventarioEntity.builder()
                .articulo(producto)
                .skuAsignado(skuDevuelto)
                .tipo(HistorialInventarioEntity.TipoMovimiento.DEVOLUCION)
                .cantidad(BigDecimal.valueOf(skusList.size()))
                .costoUnitario(producto.getCostoPromedio())
                .subtotal(BigDecimal.valueOf(skusList.size()).multiply(producto.getCostoPromedio()))
                .fecha(LocalDate.now())
                .proveedorCliente(detalle.getRequerimiento().getSolicitante())
                .area(detalle.getRequerimiento().getAreaSolicitante())
                .stockAnterior(stockAnt)
                .stockNuevo(producto.getStockActual())
                .usuarioRegistra(dto.getUsuarioRecibe())
                .build();
        historialRepository.save(historial);

        // 4. Marcar SOLO este detalle como devuelto
        detalle.setDevuelto(true);
        detalleRepository.saveAndFlush(detalle);

        // 5. Verificar si TODOS los ítems con SKU de este requerimiento ya fueron devueltos
        RequerimientoEntity req = detalle.getRequerimiento();
        boolean todosDevueltos = req.getDetalles().stream()
                .filter(d -> d.getSkuAsignado() != null && !d.getSkuAsignado().isBlank() && !d.getSkuAsignado().equals("-"))
                .allMatch(d -> Boolean.TRUE.equals(d.getDevuelto()));

        if (todosDevueltos) {
            req.setEstado(EstadoRequerimiento.DEVUELTO);
        }

        auditoriaService.registrar(
                "DEVOLUCION_ACTIVO",
                "Requerimiento",
                dto.getRequerimientoId(),
                "Retorno de " + producto.getDescripcion() + " (" + skusList.size() + " und) recibido por " + dto.getUsuarioRecibe()
        );

        return mapToDTO(requerimientoRepository.save(req));
    }

    private String generarCodigoRequerimiento() {
        long count = requerimientoRepository.count() + 1;
        String anio = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        return String.format("REQ-%s-%04d", anio, count);
    }

    private RequerimientoResponseDTO mapToDTO(RequerimientoEntity entity) {
        if (entity == null) {
            return null;
        }

        List<RequerimientoResponseDTO.DetalleResponseDTO> detallesDTO = (entity.getDetalles() == null)
            ? Collections.emptyList()
            : entity.getDetalles().stream()
                    .map(this::mapDetalleToDTO)
                    .collect(Collectors.toList());

        Integer reqId = (entity.getId() != null) ? entity.getId().intValue() : null;

        return RequerimientoResponseDTO.builder()
                .id(reqId)
                .codigo(entity.getCodigo())
                .areaSolicitante(entity.getAreaSolicitante())
                .solicitante(entity.getSolicitante())
                .observacion(entity.getObservacion())
                .estado(entity.getEstado())
                .fechaSolicitud(entity.getFechaSolicitud())
                .fechaDespacho(entity.getFechaDespacho())
                .usuarioCreacion(entity.getCreadoPor())
                .fechaCreacion(entity.getFechaCreacion())
                .detalles(detallesDTO)
                .build();
    }

   private RequerimientoResponseDTO.DetalleResponseDTO mapDetalleToDTO(RequerimientoDetalleEntity d) {
        Integer detalleId = (d.getId() != null) ? d.getId().intValue() : null;
        Integer articuloId = (d.getArticulo() != null && d.getArticulo().getId() != null)
                ? d.getArticulo().getId().intValue()
                : null;

        String articuloCodigo = (d.getArticulo() != null) ? d.getArticulo().getCodigo() : null;
        String articuloDescripcion = (d.getArticulo() != null) ? d.getArticulo().getDescripcion() : null;
        BigDecimal stockActual = (d.getArticulo() != null) ? d.getArticulo().getStockActual() : BigDecimal.ZERO;

        return RequerimientoResponseDTO.DetalleResponseDTO.builder()
                .id(detalleId)
                .articuloId(articuloId)
                .articuloCodigo(articuloCodigo)
                .articuloDescripcion(articuloDescripcion)
                .skuAsignado(d.getSkuAsignado() != null ? d.getSkuAsignado() : "-")
                .cantidadSolicitada(d.getCantidadSolicitada())
                .cantidadAtendida(d.getCantidadAtendida())
                .devuelto(Boolean.TRUE.equals(d.getDevuelto()))
                .stockDisponible(stockActual)
                .caracteristicas(d.getCaracteristicas())
                .sustento(d.getSustento())
                .build();
    }

}