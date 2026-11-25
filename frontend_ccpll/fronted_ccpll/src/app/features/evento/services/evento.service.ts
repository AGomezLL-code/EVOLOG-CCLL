import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Ambiente,
  CrearAmbienteRequest,
  ReservaEvento,
  SolicitudReservaRequest
} from '../../../core/models/evento.model';

@Injectable({
  providedIn: 'root'
})
export class EventoService {
  private http = inject(HttpClient);
  private readonly API_URL = 'https://evolog-ccll.onrender.com/api/v1/eventos';

  listarAmbientes(): Observable<Ambiente[]> {
    return this.http.get<Ambiente[]>(`${this.API_URL}/ambientes`);
  }

  crearAmbiente(payload: CrearAmbienteRequest): Observable<Ambiente> {
    return this.http.post<Ambiente>(`${this.API_URL}/ambientes`, payload);
  }

  listarReservasPorFecha(fecha: string): Observable<ReservaEvento[]> {
    return this.http.get<ReservaEvento[]>(`${this.API_URL}/reservas?fecha=${fecha}`);
  }

  solicitarReserva(payload: SolicitudReservaRequest): Observable<ReservaEvento> {
    return this.http.post<ReservaEvento>(`${this.API_URL}/reservas`, payload);
  }

  cambiarEstadoReserva(id: number, estado: 'APROBADO' | 'RECHAZADO'): Observable<ReservaEvento> {
    return this.http.patch<ReservaEvento>(`${this.API_URL}/reservas/${id}/estado`, { estado });
  }

  eliminarReserva(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.API_URL}/reservas/${id}`);
  }

  editarReserva(id: number, payload: SolicitudReservaRequest): Observable<ReservaEvento> {
  return this.http.put<ReservaEvento>(`${this.API_URL}/reservas/${id}`, payload);
}
}