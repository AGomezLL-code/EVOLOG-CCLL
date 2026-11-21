import { Injectable,inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistoriaInventarioResponseDTO } from '../../../core/models/historial.model';


@Injectable({
  providedIn: 'root'
})
export class HistorialInventarioService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/v1/historial-inventario';

  listarTodos(): Observable<HistoriaInventarioResponseDTO[]> {
    return this.http.get<HistoriaInventarioResponseDTO[]>(this.apiUrl);
  }
}
