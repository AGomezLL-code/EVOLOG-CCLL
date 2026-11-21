import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegistroAuditoria } from '../../../core/models/auditoria.model';

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/v1/auditoria';

  listarTodo(): Observable<RegistroAuditoria[]> {
    return this.http.get<RegistroAuditoria[]>(this.API_URL);
  }

  exportarExcelMultiHoja(): Observable<Blob> {
    return this.http.get(`${this.API_URL}/exportar-excel`, { responseType: 'blob' });
  }
}