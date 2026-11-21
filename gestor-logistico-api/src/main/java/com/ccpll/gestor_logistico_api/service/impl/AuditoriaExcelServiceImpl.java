package com.ccpll.gestor_logistico_api.service.impl;

import com.ccpll.gestor_logistico_api.entity.seguridad.AuditoriaEntity;
import com.ccpll.gestor_logistico_api.repository.AuditoriaRepository;
import com.ccpll.gestor_logistico_api.service.AuditoriaExcelService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaExcelServiceImpl implements AuditoriaExcelService {

    private final AuditoriaRepository auditoriaRepository;

    @Override
    public byte[] generarReporteAuditoriaMultiHoja() throws IOException {
        List<AuditoriaEntity> todos = auditoriaRepository.findAllByOrderByFechaHoraDesc();
        List<AuditoriaEntity> logins = todos.stream().filter(a -> "Usuario".equalsIgnoreCase(a.getEntidad())).toList();
        List<AuditoriaEntity> requerimientos = todos.stream().filter(a -> "Requerimiento".equalsIgnoreCase(a.getEntidad())).toList();
        List<AuditoriaEntity> inventario = todos.stream().filter(a -> "Inventario".equalsIgnoreCase(a.getEntidad())).toList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = crearEstiloCabecera(workbook);

            crearHoja(workbook, "Accesos y Logins", logins, headerStyle);
            crearHoja(workbook, "Requerimientos y Aprob.", requerimientos, headerStyle);
            crearHoja(workbook, "Movimientos Inventario", inventario, headerStyle);
            crearHoja(workbook, "Bitácora Completa", todos, headerStyle);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void crearHoja(Workbook wb, String nombreHoja, List<AuditoriaEntity> registros, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet(nombreHoja);
        String[] columnas = {"ID", "Fecha y Hora", "Usuario", "Acción", "Módulo / Entidad", "ID Ref.", "Detalle de Operación", "IP Origen"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        int rowIdx = 1;
        for (AuditoriaEntity log : registros) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(log.getId());
            row.createCell(1).setCellValue(log.getFechaHora() != null ? log.getFechaHora().format(fmt) : "");
            row.createCell(2).setCellValue(log.getUsuario());
            row.createCell(3).setCellValue(log.getAccion());
            row.createCell(4).setCellValue(log.getEntidad());
            row.createCell(5).setCellValue(log.getEntidadId() != null ? log.getEntidadId().toString() : "-");
            row.createCell(6).setCellValue(log.getDetalle());
            row.createCell(7).setCellValue(log.getIpOrigen() != null ? log.getIpOrigen() : "");
        }

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle crearEstiloCabecera(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}