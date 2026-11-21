import { Injectable, inject,signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  UsuarioPerfilResponse,
  ActualizarPerfilRequest,
  CambiarPasswordRequest
} from '../../../core/models/usuario.model';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/v1/usuario';

  fotoPerfilSignal = signal<string | null>(null);

  obtenerPerfil(): Observable<UsuarioPerfilResponse> {
    return this.http.get<UsuarioPerfilResponse>(`${this.apiUrl}/perfil`).pipe(
      tap(p => this.fotoPerfilSignal.set(p.fotoUrl || null))
    );
  }
  actualizarPerfil(dto: ActualizarPerfilRequest): Observable<UsuarioPerfilResponse> {
    return this.http.put<UsuarioPerfilResponse>(`${this.apiUrl}/perfil`, dto);
  }

  actualizarFoto(fotoUrl: string | null): Observable<UsuarioPerfilResponse> {
    return this.http.put<UsuarioPerfilResponse>(`${this.apiUrl}/foto`, { fotoUrl }).pipe(
      tap(p => this.fotoPerfilSignal.set(p.fotoUrl || null))
    );
  }

  cambiarPassword(dto: CambiarPasswordRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/password`, dto);
  }
}
