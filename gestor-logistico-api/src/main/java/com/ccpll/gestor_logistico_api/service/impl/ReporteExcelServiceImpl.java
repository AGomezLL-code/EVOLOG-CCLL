package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.entity.inventario.HistorialInventarioEntity;
import com.ccpll.gestor_logistico_api.entity.inventario.InventarioEntity;
import com.ccpll.gestor_logistico_api.entity.requerimientos.RequerimientoEntity;
import com.ccpll.gestor_logistico_api.repository.HistorialInventarioRepository;
import com.ccpll.gestor_logistico_api.repository.InventarioRepository;
import com.ccpll.gestor_logistico_api.repository.RequerimientoRepository;
import com.ccpll.gestor_logistico_api.service.ReporteExcelService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteExcelServiceImpl implements ReporteExcelService {

    private final InventarioRepository inventarioRepository;
    private final RequerimientoRepository requerimientoRepository;
    private final HistorialInventarioRepository historialInventarioRepository;

    // ==========================================
    // 1. REPORTE DE INVENTARIO EN EXCEL (CON SEMÁFORO DE STOCK)
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generarReporteInventarioExcel() throws IOException {
        String[] columnas = {
            "Código", "SKU / N° Serie", "Descripción", "Ubicación Física",
            "Categoría", "Unidad", "Stock Actual", "Stock Mínimo", "Costo Promedio (S/)", "Estado Stock"
        };

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventario Valorado");

            // Estilo de Encabezados
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(new XSSFColor(new Color(30, 58, 138), null));
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            aplicarBordes(headerCellStyle);

            // Estilos Semáforo de Stock (Verde, Amarillo, Rojo)
            XSSFCellStyle estiloVerde = crearEstiloSemaforo(workbook, new Color(220, 252, 231), new Color(22, 101, 52));
            XSSFCellStyle estiloAmarillo = crearEstiloSemaforo(workbook, new Color(254, 249, 195), new Color(133, 77, 14));
            XSSFCellStyle estiloRojo = crearEstiloSemaforo(workbook, new Color(254, 226, 226), new Color(153, 27, 27));

            // Fila 0: Leyenda visual de estados
            Row leyendaRow = sheet.createRow(0);
            leyendaRow.setHeightInPoints(20);
            Cell lblLeyenda = leyendaRow.createCell(0);
            lblLeyenda.setCellValue("Leyenda:");
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle lblStyle = workbook.createCellStyle();
            lblStyle.setFont(boldFont);
            lblLeyenda.setCellStyle(lblStyle);

            Cell cVerde = leyendaRow.createCell(1);
            cVerde.setCellValue("Stock Normal (> 125% mín.)");
            cVerde.setCellStyle(estiloVerde);

            Cell cAmarillo = leyendaRow.createCell(2);
            cAmarillo.setCellValue("Alerta: Cerca al mínimo");
            cAmarillo.setCellStyle(estiloAmarillo);

            Cell cRojo = leyendaRow.createCell(3);
            cRojo.setCellValue("Crítico: En stock mínimo o menor");
            cRojo.setCellStyle(estiloRojo);

            // Fila 2: Encabezados de tabla
            Row headerRow = sheet.createRow(2);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Llenar datos desde BD a partir de la fila 3
            List<InventarioEntity> inventario = inventarioRepository.findAll();
            int rowIdx = 3;

            for (InventarioEntity item : inventario) {
                Row row = sheet.createRow(rowIdx++);

                // 0. Código
                row.createCell(0).setCellValue(item.getCodigo() != null ? item.getCodigo() : "");

                // 1. SKU / N° Serie
                String skuTxt = (Boolean.TRUE.equals(item.getRequiereSku())) ? "APLICA SKU" : "-";
                row.createCell(1).setCellValue(skuTxt);

                // 2. Descripción
                row.createCell(2).setCellValue(item.getDescripcion() != null ? item.getDescripcion() : "");

                // 3. Ubicación Física
                String ubicacion = item.getUbicacionFisica() != null && !item.getUbicacionFisica().isBlank()
                        ? item.getUbicacionFisica() : "Almacén General";
                row.createCell(3).setCellValue(ubicacion);

                // 4. Categoría
                row.createCell(4).setCellValue(item.getCategoria() != null ? item.getCategoria().getNombre() : "Sin Categoría");

                // 5. Unidad
                row.createCell(5).setCellValue(item.getUnidad() != null ? item.getUnidad() : "");

                // 6. Stock Actual
                BigDecimal stockActual = item.getStockActual() != null ? item.getStockActual() : BigDecimal.ZERO;
                row.createCell(6).setCellValue(stockActual.doubleValue());

                // 7. Stock Mínimo
                BigDecimal stockMinimo = item.getStockMinimo() != null ? item.getStockMinimo() : BigDecimal.ZERO;
                row.createCell(7).setCellValue(stockMinimo.doubleValue());

                // 8. Costo Promedio
                BigDecimal costoPromedio = item.getCostoPromedio() != null ? item.getCostoPromedio() : BigDecimal.ZERO;
                row.createCell(8).setCellValue(costoPromedio.doubleValue());

                // 9. Evaluación de Estado Semáforo
                double actualVal = stockActual.doubleValue();
                double minVal = stockMinimo.doubleValue();
                double umbralAdvertencia = minVal > 0 ? minVal * 1.25 : 2.0;

                String estado;
                XSSFCellStyle estiloFila;

                if (actualVal <= minVal) {
                    estado = "CRÍTICO (MÍNIMO)";
                    estiloFila = estiloRojo;
                } else if (actualVal <= umbralAdvertencia) {
                    estado = "POR LLEGAR AL MÍNIMO";
                    estiloFila = estiloAmarillo;
                } else {
                    estado = "STOCK NORMAL";
                    estiloFila = estiloVerde;
                }

                row.createCell(9).setCellValue(estado);

                // Pintar toda la fila según su estado de stock
                for (int c = 0; c < columnas.length; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) {
                        cell.setCellStyle(estiloFila);
                    }
                }
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private XSSFCellStyle crearEstiloSemaforo(XSSFWorkbook workbook, Color fondo, Color texto) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(fondo, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = workbook.createFont();
        font.setColor(new XSSFColor(texto, null));
        font.setBold(true);
        style.setFont(font);
        aplicarBordes(style);
        return style;
    }

    private void aplicarBordes(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    // ==========================================
    // 2. FICHA DE REQUERIMIENTO EN PDF
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generarFichaRequerimientoPdf(Integer requerimientoId) {
        RequerimientoEntity req = requerimientoRepository.findById(requerimientoId)
                .orElseThrow(() -> new RuntimeException("Requerimiento no encontrado con ID: " + requerimientoId));

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(30, 58, 138));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font skuFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(29, 78, 216));
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            Paragraph header = new Paragraph("CÁMARA DE COMERCIO Y PRODUCCIÓN DE LA LIBERTAD", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph(
                    "SISTEMA DE GESTIÓN LOGÍSTICA (EVOLOG-CCLL)\nFICHA DE REQUERIMIENTO DE MATERIALES Y ACTIVOS", subTitleFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(15);
            document.add(subHeader);

            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[] { 20, 30, 20, 30 });

            agregarCeldaInfo(infoTable, "Código Req.:", labelFont);
            agregarCeldaInfo(infoTable, req.getCodigo() != null ? req.getCodigo() : "N/A", valueFont);
            agregarCeldaInfo(infoTable, "Estado:", labelFont);
            agregarCeldaInfo(infoTable, req.getEstado() != null ? req.getEstado().name() : "PENDIENTE", valueFont);

            agregarCeldaInfo(infoTable, "Área Solic.:", labelFont);
            agregarCeldaInfo(infoTable, req.getAreaSolicitante() != null ? req.getAreaSolicitante() : "N/A", valueFont);
            agregarCeldaInfo(infoTable, "Solicitante:", labelFont);
            agregarCeldaInfo(infoTable, req.getSolicitante() != null ? req.getSolicitante() : "N/A", valueFont);

            agregarCeldaInfo(infoTable, "Fecha Solicitud:", labelFont);
            String fechaStr = req.getFechaSolicitud() != null ?
                    req.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            agregarCeldaInfo(infoTable, fechaStr, valueFont);
            agregarCeldaInfo(infoTable, "", labelFont);
            agregarCeldaInfo(infoTable, "", valueFont);

            infoTable.setSpacingAfter(15);
            document.add(infoTable);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 12, 18, 30, 14, 8, 8, 10 });

            String[] headers = { "Código", "SKU / Serie", "Descripción de Artículo", "Ubicación", "Cant.Sol.", "Cant.Atd.", "Unidad" };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(new Color(30, 58, 138));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            if (req.getDetalles() != null && !req.getDetalles().isEmpty()) {
                req.getDetalles().forEach(det -> {
                    table.addCell(new Phrase(det.getArticulo() != null ? det.getArticulo().getCodigo() : "-", valueFont));

                    String skuVal = det.getSkuAsignado() != null && !det.getSkuAsignado().isBlank()
                            ? det.getSkuAsignado() : "-";
                    Font fSku = !skuVal.equals("-") ? skuFont : valueFont;
                    table.addCell(new Phrase(skuVal, fSku));

                    table.addCell(new Phrase(det.getArticulo() != null ? det.getArticulo().getDescripcion() : "-", valueFont));

                    String ubicacionVal = (det.getArticulo() != null && det.getArticulo().getUbicacionFisica() != null
                            && !det.getArticulo().getUbicacionFisica().isBlank())
                            ? det.getArticulo().getUbicacionFisica() : "Almacén General";
                    table.addCell(new Phrase(ubicacionVal, valueFont));

                    BigDecimal cantidadSol = det.getCantidadSolicitada() != null ? det.getCantidadSolicitada() : BigDecimal.ZERO;
                    PdfPCell cantSolCell = new PdfPCell(new Phrase(cantidadSol.toString(), valueFont));
                    cantSolCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cantSolCell);

                    BigDecimal cantidadAtd = det.getCantidadAtendida() != null ? det.getCantidadAtendida() : BigDecimal.ZERO;
                    PdfPCell cantAtdCell = new PdfPCell(new Phrase(cantidadAtd.toString(), valueFont));
                    cantAtdCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cantAtdCell);

                    String unidad = det.getUnidadMedida() != null ? det.getUnidadMedida()
                            : (det.getArticulo() != null && det.getArticulo().getUnidad() != null
                                    ? det.getArticulo().getUnidad() : "UND");
                    table.addCell(new Phrase(unidad, valueFont));
                });
            }

            table.setSpacingAfter(40);
            document.add(table);

            PdfPTable firmaTable = new PdfPTable(2);
            firmaTable.setWidthPercentage(80);
            firmaTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cellFirma1 = new PdfPCell(
                    new Phrase("___________________________\nEntregado por (Almacén / Logística)", labelFont));
            cellFirma1.setBorder(Rectangle.NO_BORDER);
            cellFirma1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cellFirma2 = new PdfPCell(
                    new Phrase("___________________________\nRecibido Conforme (Solicitante)", labelFont));
            cellFirma2.setBorder(Rectangle.NO_BORDER);
            cellFirma2.setHorizontalAlignment(Element.ALIGN_CENTER);

            firmaTable.addCell(cellFirma1);
            firmaTable.addCell(cellFirma2);
            document.add(firmaTable);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // 3. REPORTE KARDEX EN EXCEL (CON SKU Y UBICACIÓN FÍSICA)
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generarReporteKardexExcel(LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
        String[] columnas = {
            "Fecha", "Código Art.", "SKU / N° Serie", "Descripción", "Ubicación Física", "Tipo Movimiento",
            "Cantidad", "Stock Anterior", "Stock Nuevo", "Costo Unit. (S/)",
            "Subtotal (S/)", "Prov. / Cliente", "Área", "Usuario Registra"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Historial Kardex");

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerCellStyle);
            }

            List<HistorialInventarioEntity> movimientos = (fechaInicio != null && fechaFin != null)
                    ? historialInventarioRepository.findByFechaBetweenOrderByIdDesc(fechaInicio, fechaFin)
                    : historialInventarioRepository.findAll();

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (HistorialInventarioEntity mov : movimientos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(mov.getFecha() != null ? mov.getFecha().format(formatter) : "");
                row.createCell(1).setCellValue(mov.getArticulo() != null ? mov.getArticulo().getCodigo() : "");

                String skuMov = mov.getSkuAsignado() != null && !mov.getSkuAsignado().isBlank()
                        ? mov.getSkuAsignado() : "-";
                row.createCell(2).setCellValue(skuMov);

                row.createCell(3).setCellValue(mov.getArticulo() != null ? mov.getArticulo().getDescripcion() : "");

                String ubicacionMov = (mov.getArticulo() != null && mov.getArticulo().getUbicacionFisica() != null
                        && !mov.getArticulo().getUbicacionFisica().isBlank())
                        ? mov.getArticulo().getUbicacionFisica() : "Almacén General";
                row.createCell(4).setCellValue(ubicacionMov);

                row.createCell(5).setCellValue(mov.getTipo() != null ? mov.getTipo().name() : "");

                BigDecimal cantidad = mov.getCantidad() != null ? mov.getCantidad() : BigDecimal.ZERO;
                row.createCell(6).setCellValue(cantidad.doubleValue());

                BigDecimal stockAnterior = mov.getStockAnterior() != null ? mov.getStockAnterior() : BigDecimal.ZERO;
                BigDecimal stockNuevo = mov.getStockNuevo() != null ? mov.getStockNuevo() : BigDecimal.ZERO;
                row.createCell(7).setCellValue(stockAnterior.doubleValue());
                row.createCell(8).setCellValue(stockNuevo.doubleValue());

                BigDecimal costoUnit = mov.getCostoUnitario() != null ? mov.getCostoUnitario() : BigDecimal.ZERO;
                BigDecimal subtotal = mov.getSubtotal() != null ? mov.getSubtotal() : BigDecimal.ZERO;
                row.createCell(9).setCellValue(costoUnit.doubleValue());
                row.createCell(10).setCellValue(subtotal.doubleValue());

                row.createCell(11).setCellValue(mov.getProveedorCliente() != null ? mov.getProveedorCliente() : "");
                row.createCell(12).setCellValue(mov.getArea() != null ? mov.getArea() : "");
                row.createCell(13).setCellValue(mov.getUsuarioRegistra() != null ? mov.getUsuarioRegistra() : "");
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void agregarCeldaInfo(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        table.addCell(cell);
    }
}