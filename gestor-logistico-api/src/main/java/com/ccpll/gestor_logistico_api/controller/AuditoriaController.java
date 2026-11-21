package com.ccpll.gestor_logistico_api.controller;

import com.ccpll.gestor_logistico_api.entity.seguridad.AuditoriaEntity;
import com.ccpll.gestor_logistico_api.service.AuditoriaExcelService;
import com.ccpll.gestor_logistico_api.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('JEFE_LOGISTICA')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final AuditoriaExcelService auditoriaExcelService;

    @GetMapping
    public ResponseEntity<List<AuditoriaEntity>> listarTodo() {
        return ResponseEntity.ok(auditoriaService.listarTodo());
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel() throws IOException {
        byte[] excelBytes = auditoriaExcelService.generarReporteAuditoriaMultiHoja();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Auditoria_EVOLOG_CCLL.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}