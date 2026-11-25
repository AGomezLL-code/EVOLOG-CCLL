import { Injectable,inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  DashboardKpiDTO, 
  ValorCategoriaDTO, 
  MovimientoRecienteDTO 
} from '../../../core/models/dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = 'https://evolog-ccll.onrender.com/api/v1/dashboard';

  // 1. KPIs Globales
  getKpis(): Observable<DashboardKpiDTO> {
    return this.http.get<DashboardKpiDTO>(`${this.apiUrl}/kpis`);
  }

  // 2. Resumen de Solicitudes
  getSolicitudesResumen(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/solicitudes-resumen`);
  }

  // 3. Distribución del Valor por Categoría (Para el Gráfico)
  getValorPorCategoria(): Observable<ValorCategoriaDTO[]> {
    return this.http.get<ValorCategoriaDTO[]>(`${this.apiUrl}/valor-categoria`);
  }

  // 4. Últimos Movimientos del Kardex (Para la Tabla)
  getMovimientosRecientes(): Observable<MovimientoRecienteDTO[]> {
    return this.http.get<MovimientoRecienteDTO[]>(`${this.apiUrl}/movimientos-recientes`);
  }

  getMovimientosMensuales(): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/movimientos-mensuales`);
}

}
