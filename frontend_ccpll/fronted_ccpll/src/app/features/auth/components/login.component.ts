import { ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { CheckboxModule } from 'primeng/checkbox';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/auth/auth.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    SelectModule,
    CheckboxModule,
    ButtonModule
  ],
  template: `
    <div class="min-h-screen w-full flex flex-col lg:flex-row font-sans">
      
      <!-- PANEL IZQUIERDO: IDENTIDAD INSTITUCIONAL (GUINDA CCLL) -->
      <div class="lg:w-1/2 relative overflow-hidden flex flex-col justify-between p-8 sm:p-12 lg:p-16 text-white"
           style="background: linear-gradient(145deg, #320b14 0%, #4a121f 60%, #3a0d17 100%);">
        
        <!-- Círculos decorativos sutiles de fondo -->
        <div class="pointer-events-none absolute -right-24 -top-24 w-[460px] h-[460px] rounded-full border-[45px] border-white/[0.03]"></div>
        <div class="pointer-events-none absolute right-12 top-36 w-[320px] h-[320px] rounded-full border-[30px] border-white/[0.02]"></div>

        <!-- Cabecera Logo -->
        <div class="relative z-10 flex items-center gap-4">
          <div class="bg-white rounded-xl px-4 py-2.5 shadow-md flex items-center justify-center">
            <!-- Si tienes el logo en assets, reemplaza este bloque por <img src="assets/logo-ccll.png" class="h-10" /> -->
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-full bg-[#4a121f] text-[#d4af37] font-extrabold text-xs flex items-center justify-center">
                CCLL
              </div>
              <div class="leading-tight">
                <div class="text-[10px] font-extrabold text-[#4a121f] tracking-tight">CÁMARA DE COMERCIO</div>
                <div class="text-[8px] font-semibold text-slate-500">DE LA LIBERTAD</div>
              </div>
            </div>
          </div>

          <div>
            <div class="font-bold text-lg tracking-wide text-white">EVOLOG-CCLL</div>
            <div class="text-[11px] uppercase tracking-wider text-rose-200/70 font-medium">
              Sistema de Gestión Logística<br />Institucional
            </div>
          </div>
        </div>

        <!-- Contenido Central Hero -->
        <div class="relative z-10 my-12 lg:my-0 max-w-lg">
          <div class="inline-flex items-center gap-2 text-xs font-semibold tracking-wide mb-5" style="color: #e2b857;">
            <i class="pi pi-verified text-sm"></i>
            <span>Gestión institucional centralizada</span>
          </div>

          <h1 class="text-4xl sm:text-5xl font-bold leading-[1.15] tracking-tight text-white mb-6">
            Logística eficiente para una institución que avanza.
          </h1>

          <p class="text-rose-100/80 text-base leading-relaxed">
            Gestiona solicitudes, inventarios y aprobaciones en una plataforma segura al servicio de la Cámara de Comercio y Producción de La Libertad.
          </p>
        </div>

        <!-- Pilares Inferiores y Pie de Página -->
        <div class="relative z-10">
          <div class="border-t border-white/15 pt-6 pb-8 grid grid-cols-3 gap-4">
            <div>
              <div class="font-bold text-white text-sm">Control</div>
              <div class="text-xs text-rose-200/65 mt-0.5">Visibilidad del inventario</div>
            </div>
            <div>
              <div class="font-bold text-white text-sm">Agilidad</div>
              <div class="text-xs text-rose-200/65 mt-0.5">Solicitudes centralizadas</div>
            </div>
            <div>
              <div class="font-bold text-white text-sm">Trazabilidad</div>
              <div class="text-xs text-rose-200/65 mt-0.5">Seguimiento operativo</div>
            </div>
          </div>

          <div class="flex items-center gap-2 text-xs text-rose-200/60">
            <i class="pi pi-building"></i>
            <span>Cámara de Comercio y Producción de La Libertad · Trujillo, Perú</span>
          </div>
        </div>
      </div>

      <!-- PANEL DERECHO: FORMULARIO DE ACCESO -->
      <div class="lg:w-1/2 bg-white flex items-center justify-center p-8 sm:p-12 lg:p-16">
        <div class="w-full max-w-md">
          
          <div class="text-[11px] font-bold uppercase tracking-[0.12em] mb-2" style="color: #7c2d3e;">
            ACCESO INSTITUCIONAL
          </div>
          <h2 class="text-3xl font-bold text-slate-900 mb-2">Bienvenido</h2>
          <p class="text-slate-500 text-sm mb-8">
            Ingresa tus credenciales para acceder al sistema logístico institucional.
          </p>

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="space-y-5">
            
            <!-- Usuario / Correo Institucional -->
            <div>
              <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                Usuario o correo institucional
              </label>
              <div class="relative">
                <i class="pi pi-envelope text-slate-400 text-sm mb-1 block"></i>
                <input
                  type="text"
                  pInputText
                  formControlName="username"
                  placeholder="jefe.logistica"
                  class="w-full !rounded-lg !border-slate-300 !py-2.5 !px-3.5 text-sm text-slate-800 focus:!border-[#4a121f]" />
              </div>
            </div>

            <!-- Contraseña con botón mostrar/ocultar -->
            <div>
              <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                Contraseña
              </label>
              <i class="pi pi-lock text-slate-400 text-sm mb-1 block"></i>
              <div class="relative flex items-center">
                <input
                  [type]="mostrarPassword ? 'text' : 'password'"
                  pInputText
                  formControlName="password"
                  placeholder="••••••••••"
                  class="w-full !rounded-lg !border-slate-300 !py-2.5 !pl-3.5 !pr-10 text-sm text-slate-800 focus:!border-[#4a121f]" />
                <button
                  type="button"
                  (click)="mostrarPassword = !mostrarPassword"
                  class="absolute right-3 text-slate-400 hover:text-slate-600 focus:outline-none">
                  <i [class]="mostrarPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
                </button>
              </div>
            </div>

            <!-- Recordar sesión y Olvidaste contraseña -->
            <div class="flex items-center justify-between text-xs pt-1">
              <label class="flex items-center gap-2 cursor-pointer text-slate-600 select-none">
                <input
                  type="checkbox"
                  formControlName="recordar"
                  class="w-4 h-4 rounded border-slate-300 accent-[#4a121f] cursor-pointer" />
                <span>Recordar sesión</span>
              </label>

              <a href="javascript:void(0)" (click)="mostrarAyudaClave()" class="font-semibold hover:underline" style="color: #7c2d3e;">
                ¿Olvidaste tu contraseña?
              </a>
            </div>


            <!-- Mensaje de error si falla autenticación -->
            <div *ngIf="errorMsg" class="p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
              <i class="pi pi-exclamation-circle"></i>
              <span>{{ errorMsg }}</span>
            </div>

            <!-- Botón Principal Continuar (Guinda CCLL) -->
            <button
              type="submit"
              [disabled]="loginForm.invalid || cargando"
              class="w-full py-3 px-4 rounded-lg text-white font-semibold text-sm flex items-center justify-center gap-2 transition-all shadow-sm hover:opacity-95 disabled:opacity-50 cursor-pointer"
              style="background-color: #4a121f;">
              <span>{{ cargando ? 'Validando acceso...' : 'Continuar' }}</span>
              <i *ngIf="!cargando" class="pi pi-angle-right text-xs"></i>
              <i *ngIf="cargando" class="pi pi-spin pi-spinner text-xs"></i>
            </button>

            <!-- Banner inferior de seguridad JWT -->
            <div class="p-3 rounded-lg border text-xs flex items-center gap-2.5"
                 style="background-color: #fdfaf2; border-color: #f3e5c0; color: #85641a;">
              <i class="pi pi-shield text-sm" style="color: #c59b27;"></i>
              <span>Acceso protegido mediante autenticación JWT y control por roles.</span>
            </div>

          </form>
        </div>
      </div>

    </div>
  `


})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);

  cargando = false;
  mostrarPassword = false;
  errorMsg = '';

  perfiles = [
    { label: 'Jefe de Logística', username: 'jefe.logistica' },
    { label: 'Asistente de Almacén', username: 'asistente.almacen' },
    { label: 'Cliente Interno (Solicitante)', username: 'abel.gomez' },
    { label: 'Área Comercial (Eventos)', username: 'comercial.eventos' }
  ];

  loginForm = this.fb.group({
    username: ['jefe.logistica', Validators.required],
    password: ['123456', Validators.required],
    recordar: [true],
    perfilSeleccionado: ['jefe.logistica']
  });

  ngOnInit(): void {
    // Si el usuario tiene sesión activa y retrocede con "Atrás" hasta /login, lo devuelve a su panel
    if (isPlatformBrowser(this.platformId) && this.authService.estaAutenticado()) {
      const rol = this.authService.getSesion()?.rol;
      this.redirigirPorRol(rol);
    }
  }

  private redirigirPorRol(rol?: string): void {
    if (rol === 'CLIENTE_INTERNO') {
      this.router.navigate(['/requerimientos'], { replaceUrl: true });
    } else if (rol === 'AREA_COMERCIAL') {
      this.router.navigate(['/eventos'], { replaceUrl: true });
    } else {
      this.router.navigate(['/dashboard'], { replaceUrl: true });
    }
  }

  onCambioPerfil(username: string): void {
    if (username) {
      this.loginForm.patchValue({ username });
    }
  }

  mostrarAyudaClave(): void {
    this.errorMsg = 'Comuníquese con el Jefe de Logística o Soporte TI para restablecer su clave.';
  }

  onSubmit(): void {
    if (this.loginForm.invalid || this.cargando) return;
    this.cargando = true;
    this.errorMsg = '';

    const { username, password } = this.loginForm.getRawValue();

    this.authService.login({
      username: username!,
      password: password!
    }).pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (sesion) => {
        this.redirigirPorRol(sesion.rol);
      },
      error: (err) => {
        if (err.status === 0) {
          this.errorMsg = 'No se pudo conectar con el servidor. Verifique que el backend esté activo.';
        } else {
          this.errorMsg = err.error?.message || 'Usuario o contraseña incorrectos. Verifique sus credenciales.';
        }
        this.cdr.detectChanges();
      }
    });
  }
}