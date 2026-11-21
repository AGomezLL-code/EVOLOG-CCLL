import { Component, inject, OnInit, PLATFORM_ID, ChangeDetectorRef,effect } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { PerfilService } from '../../../features/perfil/services/perfil.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.css']
})
export class LayoutComponent implements OnInit {
  authService = inject(AuthService);
  private perfilService = inject(PerfilService);
  private confirmationService = inject(ConfirmationService);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);

  // Signal reactivo conectado directamente con PerfilService
  fotoPerfil = this.perfilService.fotoPerfilSignal;

  constructor() {
    // Fuerza la actualización visual del header cada vez que cambie la foto en Mi Perfil
    effect(() => {
      this.fotoPerfil();
      this.cdr.detectChanges();
    });
  }
  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.perfilService.obtenerPerfil().subscribe({
        next: () => this.cdr.detectChanges(),
        error: () => {}
      });
    }
  }

  get usuario() {
    return this.authService.getSesion();
  }

  get iniciales(): string {
    const nombre = this.usuario?.nombreCompleto || 'US';
    return nombre
      .split(' ')
      .slice(0, 2)
      .map(n => n[0])
      .join('')
      .toUpperCase();
  }

  get rolLegible(): string {
    const mapa: Record<string, string> = {
      JEFE_LOGISTICA: 'Jefe de Logística',
      ASISTENTE: 'Asistente de Almacén',
      AUXILIAR: 'Auxiliar de Inventario',
      CLIENTE_INTERNO: 'Cliente Interno',
      AREA_COMERCIAL: 'Área Comercial'
    };
    return this.usuario?.rol ? (mapa[this.usuario.rol] || this.usuario.rol) : '';
  }

  esPersonalLogistica(): boolean {
    return this.authService.tieneRol(['JEFE_LOGISTICA', 'ASISTENTE', 'AUXILIAR']);
  }

  puedeVerInventario(): boolean {
    return this.authService.tieneRol([
      'JEFE_LOGISTICA',
      'ASISTENTE',
      'AUXILIAR',
      'CLIENTE_INTERNO',
      'AREA_COMERCIAL'
    ]);
  }

  esJefe(): boolean {
    return this.authService.tieneRol(['JEFE_LOGISTICA']);
  }

  puedeGestionarEventos(): boolean {
    const rol = this.usuario?.rol;
    return rol === 'JEFE_LOGISTICA' || rol === 'AREA_COMERCIAL';
  }

  cerrarSesion(): void {
    this.confirmationService.confirm({
      header: 'Confirmar Cierre de Sesión',
      message: '¿Estás seguro de que deseas cerrar sesión? Tendrás que volver a autenticarte para ingresar al sistema.',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, cerrar sesión',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-secondary p-button-outlined',
      accept: () => {
        this.perfilService.fotoPerfilSignal.set(null);
        this.authService.logout();
      }
    });
  }
}