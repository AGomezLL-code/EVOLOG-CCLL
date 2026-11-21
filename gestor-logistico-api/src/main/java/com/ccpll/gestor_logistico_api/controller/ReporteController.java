package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.service.ReporteExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping({"/api/v1/reportes", "/api/reportes"})
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteExcelService reporteExcelService;

    @GetMapping("/inventario/excel")
    public ResponseEntity<InputStreamResource> descargarInventarioExcel() throws IOException {
        ByteArrayInputStream stream = reporteExcelService.generarReporteInventarioExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Inventario_EVOLOG_CCLL.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    // 2. Exportar Ficha de Requerimiento a PDF
    @GetMapping("/requerimiento/{id}/pdf")
    public ResponseEntity<InputStreamResource> descargarFichaRequerimientoPdf(@PathVariable Integer id) {
        ByteArrayInputStream stream = reporteExcelService.generarFichaRequerimientoPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Ficha_Requerimiento_REQ-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }

    // 3. Exportar Historial de Kardex (Entradas/Salidas) a Excel
    @GetMapping("/kardex/excel")
    public ResponseEntity<InputStreamResource> descargarKardexExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws IOException {

        ByteArrayInputStream stream = reporteExcelService.generarReporteKardexExcel(fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Kardex_Movimientos_CCLL.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }
}