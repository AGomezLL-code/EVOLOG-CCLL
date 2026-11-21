import { Injectable,inject } from '@angular/core';

import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportesService {
  private http = inject(HttpClient);
  
  // URL directa hacia tu Backend Spring Boot
  private apiUrl = 'http://localhost:8080/api/v1/reportes';

  descargarInventarioExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/inventario/excel`, {
      responseType: 'blob'
    });
  }

  // 2. Descargar Ficha de Requerimiento en PDF
  descargarRequerimientoPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/requerimiento/${id}/pdf`, {
      responseType: 'blob'
    });
  }

  // 3. Descargar Kardex Excel (con filtros opcionales de fecha)
  descargarKardexExcel(fechaInicio?: string, fechaFin?: string): Observable<Blob> {
    let params = new HttpParams();
    if (fechaInicio) params = params.set('fechaInicio', fechaInicio);
    if (fechaFin) params = params.set('fechaFin', fechaFin);

    return this.http.get(`${this.apiUrl}/kardex/excel`, {
      params,
      responseType: 'blob'
    });
  }
}
