import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  RequerimientoRequest, 
  RequerimientoResponse, 
  EstadoRequerimiento, 
  DevolucionActivoRequestDTO 
} from '../../../core/models/requerimiento.model';

@Injectable({
  providedIn: 'root',
})
export class RequerimientoService {

  private http = inject(HttpClient);
  private apiUrl = 'https://evolog-ccll.onrender.com/api/v1/requerimientos';

  listarTodos(): Observable<RequerimientoResponse[]> {
    return this.http.get<RequerimientoResponse[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<RequerimientoResponse> {
    return this.http.get<RequerimientoResponse>(`${this.apiUrl}/${id}`);
  }

  crear(dto: RequerimientoRequest): Observable<RequerimientoResponse> {
    return this.http.post<RequerimientoResponse>(this.apiUrl, dto);
  }

  cambiarEstado(id: number, nuevoEstado: EstadoRequerimiento, observaciones?: string): Observable<RequerimientoResponse> {
    let params: any = { nuevoEstado };
    if (observaciones) {
      params.observaciones = observaciones;
    }
    return this.http.patch<RequerimientoResponse>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  // NUEVO MÉTODO PARA REGISTRAR LA DEVOLUCIÓN DE LAPTOPS/EQUIPOS EN PRÉSTAMO
  registrarDevolucion(dto: DevolucionActivoRequestDTO): Observable<RequerimientoResponse> {
    return this.http.post<RequerimientoResponse>(`${this.apiUrl}/devolucion`, dto);
  }
}
