import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';

import { InventarioResponseDTO, InventarioRequestDTO, EntradaInventarioRequestDTO, SalidaInventarioRequestDTO,HistoriaInventarioResponseDTO, ResumenInventarioDTO, ActivoSerieDTO  } from '../../../core/models/inventario.model';


@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/v1/inventario';

  listarTodos(): Observable<InventarioResponseDTO[]> {
    return this.http.get<InventarioResponseDTO[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<InventarioResponseDTO> {
    return this.http.get<InventarioResponseDTO>(`${this.apiUrl}/${id}`);
  }

  crear(dto: InventarioRequestDTO): Observable<InventarioResponseDTO> {
    return this.http.post<InventarioResponseDTO>(this.apiUrl, dto);
  }

  registrarEntrada(dto: EntradaInventarioRequestDTO): Observable<InventarioResponseDTO> {
    return this.http.post<InventarioResponseDTO>(`${this.apiUrl}/movimiento/entrada`, dto);
  }

  registrarSalida(dto: SalidaInventarioRequestDTO): Observable<InventarioResponseDTO> {
    return this.http.post<InventarioResponseDTO>(`${this.apiUrl}/movimiento/salida`, dto);
  }

  obtenerKardex(articuloId: number, fechaInicio?: string, fechaFin?: string): Observable<HistoriaInventarioResponseDTO[]> {
  let params = new HttpParams();
  if (fechaInicio) params = params.set('fechaInicio', fechaInicio);
  if (fechaFin) params = params.set('fechaFin', fechaFin);

  return this.http.get<HistoriaInventarioResponseDTO[]>(`${this.apiUrl}/${articuloId}/kardex`, { params });
}

obtenerResumenKPI(): Observable<ResumenInventarioDTO> {
  return this.http.get<ResumenInventarioDTO>(`${this.apiUrl}/resumen`);
}

obtenerAlertasStockBajo(): Observable<InventarioResponseDTO[]> {
  return this.http.get<InventarioResponseDTO[]>(`${this.apiUrl}/alertas`);
}

obtenerSkusPorProducto(productoId: number): Observable<ActivoSerieDTO[]> {
    return this.http.get<ActivoSerieDTO[]>(`${this.apiUrl}/${productoId}/skus`);
  }
}