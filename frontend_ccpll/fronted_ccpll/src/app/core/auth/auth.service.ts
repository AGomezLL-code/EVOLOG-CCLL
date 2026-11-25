import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export interface UsuarioSesion {
  token: string;
  username: string;
  nombreCompleto: string;
  area: string;
  rol: 'JEFE_LOGISTICA' | 'ASISTENTE' | 'AUXILIAR' | 'CLIENTE_INTERNO' | 'AREA_COMERCIAL';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  private readonly API_URL = 'https://evolog-ccll.onrender.com/api/v1/auth';
  private readonly STORAGE_KEY = 'evolog_sesion';

  login(credentials: { username: string; password: string }): Observable<UsuarioSesion> {
    return this.http.post<UsuarioSesion>(`${this.API_URL}/login`, credentials).pipe(
      tap((res) => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.STORAGE_KEY, JSON.stringify(res));
        }
      })
    );
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.STORAGE_KEY);
    }
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  getSesion(): UsuarioSesion | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    try {
      const raw = localStorage.getItem(this.STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      localStorage.removeItem(this.STORAGE_KEY);
      return null;
    }
  }

  getToken(): string | null {
    return this.getSesion()?.token ?? null;
  }

  estaAutenticado(): boolean {
    if (!isPlatformBrowser(this.platformId)) return true;

    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (payload.exp && Date.now() >= payload.exp * 1000) {
        this.logout();
        return false;
      }
      return true;
    } catch {
      return false;
    }
  }

  tieneRol(rolesPermitidos: string[]): boolean {
    const rolActual = this.getSesion()?.rol;
    return rolActual ? rolesPermitidos.includes(rolActual) : false;
  }
}