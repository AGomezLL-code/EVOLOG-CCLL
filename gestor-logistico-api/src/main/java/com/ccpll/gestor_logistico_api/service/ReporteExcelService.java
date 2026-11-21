package com.ccpll.gestor_logistico_api.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;


public interface ReporteExcelService {

    /**
     * Genera un reporte en Excel (.xlsx) del inventario valorizado.
     *
     * @return ByteArrayInputStream con el flujo de datos del archivo Excel.
     * @throws IOException Si ocurre un error durante la generación del archivo.
     */
    ByteArrayInputStream generarReporteInventarioExcel() throws IOException;

    ByteArrayInputStream generarFichaRequerimientoPdf(Integer requerimientoId);

ByteArrayInputStream generarReporteKardexExcel(LocalDate fechaInicio, LocalDate fechaFin) throws IOException;
}
