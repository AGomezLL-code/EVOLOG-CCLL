import { Component, OnInit, inject, ChangeDetectorRef, PLATFORM_ID, NgZone } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';
import { PerfilService } from '../services/perfil.service';
import { AuthService } from '../../../core/auth/auth.service';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { UsuarioPerfilResponse } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ToastModule,
    CardModule,
    InputTextModule,
    ButtonModule,
    DividerModule
  ],
  providers: [MessageService],
  templateUrl: './perfil.component.html'
})
export class PerfilComponent implements OnInit {
  private fb          = inject(FormBuilder);
  private perfilSvc   = inject(PerfilService);
  private authSvc     = inject(AuthService);
  private msgSvc      = inject(MessageService);
  private cdr         = inject(ChangeDetectorRef);
  private platformId  = inject(PLATFORM_ID);
  private ngZone = inject(NgZone);


  perfil: UsuarioPerfilResponse | null = null;
  cargando   = false;
  guardando  = false;
  cambiandoPwd = false;
  subiendoFoto = false;


  

  formPerfil: FormGroup = this.fb.group({
    nombreCompleto: ['', [Validators.required, Validators.maxLength(100)]],
    area:           ['', [Validators.required, Validators.maxLength(80)]]
  });

  formPassword: FormGroup = this.fb.group({
    passwordActual: ['', [Validators.required]],
    passwordNuevo:  [
      '',
      [
        Validators.required,
        Validators.minLength(11), // Mayor de 10 caracteres (mínimo 11)
        Validators.maxLength(60),
        (ctrl: AbstractControl) => this.validarPasswordRobusta(ctrl)
      ]
    ]
  });

  get iniciales(): string {
    const nombre = this.perfil?.nombreCompleto || this.authSvc.getSesion()?.nombreCompleto || 'US';
    return nombre.split(' ').slice(0, 2).map((n: string) => n[0]).join('').toUpperCase();
  }

  get rolLegible(): string {
    const mapa: Record<string, string> = {
      JEFE_LOGISTICA:  'Jefe de Logística',
      ASISTENTE:       'Asistente de Almacén',
      AUXILIAR:        'Auxiliar de Inventario',
      CLIENTE_INTERNO: 'Cliente Interno',
      AREA_COMERCIAL:  'Área Comercial'
    };
    return this.perfil?.rol ? (mapa[this.perfil.rol] || this.perfil.rol) : '';
  }

  // Checklist reactivo para mostrar en el HTML mientras el usuario escribe
  get requisitosPassword() {
    const val: string = this.formPassword.get('passwordNuevo')?.value || '';
    return {
      longitudMayor10: val.length > 10,
      tieneEspecial: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?¡¿°¬~`]/.test(val),
      tieneMayusculaYNumero: /[A-Z]/.test(val) && /[0-9]/.test(val),
      sinDatosPersonales: !this.contieneDatosPersonalesOCargo(val)
    };
  }

  private obtenerPalabrasProhibidas(): string[] {
    const sesion = this.authSvc.getSesion();
    const fuentes = [
      this.perfil?.nombreCompleto || sesion?.nombreCompleto || '',
      this.perfil?.username || sesion?.username || '',
      this.perfil?.area || sesion?.area || '',
      this.perfil?.rol || sesion?.rol || '',
      this.rolLegible,
      'jefe', 'logistica', 'asistente', 'almacen', 'auxiliar', 'inventario', 'comercial', 'cliente', 'interno', 'ccll', 'evolog'
    ];

    const palabras = new Set<string>();
    for (const texto of fuentes) {
      const normalizado = this.normalizarTexto(texto);
      // Divide por espacios, puntos, guiones o guiones bajos
      normalizado.split(/[\s._-]+/).forEach(token => {
        if (token.length >= 3) {
          palabras.add(token);
        }
      });
    }
    return Array.from(palabras);
  }

  private normalizarTexto(texto: string): string {
    return texto
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, ''); // Quita tildes (ej. Logística -> logistica)
  }

  private contieneDatosPersonalesOCargo(password: string): boolean {
    if (!password) return false;
    const pwdNorm = this.normalizarTexto(password);
    return this.obtenerPalabrasProhibidas().some(palabra => pwdNorm.includes(palabra));
  }

  private validarPasswordRobusta(control: AbstractControl): ValidationErrors | null {
    const val: string = control.value || '';
    if (!val) return null;

    const errores: ValidationErrors = {};

    if (val.length <= 10) {
      errores['longitudInsuficiente'] = true;
    }
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?¡¿°¬~`]/.test(val)) {
      errores['sinCaracterEspecial'] = true;
    }
    if (!/[A-Z]/.test(val) || !/[0-9]/.test(val)) {
      errores['sinMayusculaONumero'] = true;
    }
    if (this.contieneDatosPersonalesOCargo(val)) {
      errores['contieneDatosPersonales'] = true;
    }

    return Object.keys(errores).length > 0 ? errores : null;
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.cargarPerfil();
    }
  }

  cargarPerfil(): void {
    this.cargando = true;
    this.cdr.detectChanges();

    this.perfilSvc.obtenerPerfil().subscribe({
      next: (data) => {
        this.perfil = data;
        this.formPerfil.patchValue({
          nombreCompleto: data.nombreCompleto,
          area:           data.area
        });
        // Re-evaluar contraseña si el usuario ya había escrito algo
        this.formPassword.get('passwordNuevo')?.updateValueAndValidity();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.cargando = false;
        this.cdr.detectChanges();
        this.msgSvc.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.message || 'No se pudo cargar el perfil.'
        });
      }
    });
  }

  onGuardarPerfil(): void {
    if (this.formPerfil.invalid) { this.formPerfil.markAllAsTouched(); return; }
    this.guardando = true;
    this.cdr.detectChanges();

    this.perfilSvc.actualizarPerfil(this.formPerfil.value).subscribe({
      next: (data) => {
        this.perfil = data;
        this.guardando = false;
        this.cdr.detectChanges();
        this.msgSvc.add({
          severity: 'success',
          summary: 'Perfil actualizado',
          detail: 'Tus datos han sido guardados correctamente.'
        });
      },
      error: (err) => {
        this.guardando = false;
        this.cdr.detectChanges();
        this.msgSvc.add({
          severity: 'error',
          summary: 'Error al guardar',
          detail: err.error?.message || 'No se pudieron guardar los cambios.'
        });
      }
    });
  }

  onCambiarPassword(): void {
    if (this.formPassword.invalid) { this.formPassword.markAllAsTouched(); return; }
    this.cambiandoPwd = true;
    this.cdr.detectChanges();

    this.perfilSvc.cambiarPassword(this.formPassword.value).subscribe({
      next: (resp) => {
        this.cambiandoPwd = false;
        this.formPassword.reset();
        this.cdr.detectChanges();
        this.msgSvc.add({
          severity: 'success',
          summary: 'Contraseña actualizada',
          detail: resp.message || 'Tu contraseña ha sido cambiada exitosamente.'
        });
      },
      error: (err) => {
        this.cambiandoPwd = false;
        this.cdr.detectChanges();
        this.msgSvc.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.message || 'No se pudo cambiar la contraseña.'
        });
      }
    });
  }

  onSeleccionarFoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    if (!file.type.startsWith('image/')) {
      this.msgSvc.add({ severity: 'warn', summary: 'Formato inválido', detail: 'Selecciona una imagen (JPG, PNG o WEBP).' });
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const size = 200;
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d')!;

        const minSide = Math.min(img.width, img.height);
        const sx = (img.width - minSide) / 2;
        const sy = (img.height - minSide) / 2;
        ctx.drawImage(img, sx, sy, minSide, minSide, 0, 0, size, size);

        const base64Comprimido = canvas.toDataURL('image/jpeg', 0.85);
        this.ngZone.run(() => {
          this.guardarFoto(base64Comprimido);
        });
      };
      img.src = reader.result as string;
    };
    reader.readAsDataURL(file);
    input.value = ''; // Permite volver a seleccionar el mismo archivo si se desea
  }

guardarFoto(base64: string | null): void {
  this.subiendoFoto = true;
  this.cdr.detectChanges();

  this.perfilSvc.actualizarFoto(base64).subscribe({
    next: (data) => {
      this.perfil = data;
      this.subiendoFoto = false;
      this.cdr.detectChanges();
      this.msgSvc.add({
        severity: 'success',
        summary: 'Foto actualizada',
        detail: base64 ? 'Tu foto de perfil se actualizó correctamente.' : 'Se eliminó la foto de perfil.'
      });
    },
    error: () => {
      this.subiendoFoto = false;
      this.cdr.detectChanges();
      this.msgSvc.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar la foto.' });
    }
  });
}

  campoInvalido(form: FormGroup, campo: string): boolean {
    const ctrl = form.get(campo);
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }
}
