import { inject, Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoriaRequestDTO,CategoriaResponseDTO } from '../../../core/models/categoria.model';
@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/v1/categorias';

  listarTodas(): Observable<CategoriaResponseDTO[]> {
    return this.http.get<CategoriaResponseDTO[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<CategoriaResponseDTO> {
    return this.http.get<CategoriaResponseDTO>(`${this.apiUrl}/${id}`);
  }

  crear(dto: CategoriaRequestDTO): Observable<CategoriaResponseDTO> {
    return this.http.post<CategoriaResponseDTO>(this.apiUrl, dto);
  }
}
