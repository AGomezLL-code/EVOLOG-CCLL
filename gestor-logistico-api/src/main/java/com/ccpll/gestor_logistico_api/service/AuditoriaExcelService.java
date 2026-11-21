package com.ccpll.gestor_logistico_api.service;

import java.io.IOException;

public interface AuditoriaExcelService {
    byte[] generarReporteAuditoriaMultiHoja() throws IOException;
}
