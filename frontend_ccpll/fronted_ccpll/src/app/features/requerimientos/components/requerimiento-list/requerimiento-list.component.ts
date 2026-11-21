import { Component, OnInit, inject, ChangeDetectorRef, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { TextareaModule } from 'primeng/textarea';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ReportesService } from '../../../reportes/reportes.service';

import { RequerimientoService } from '../../services/requerimiento.service';
import { 
  RequerimientoResponse, 
  DetalleRequerimientoResponse, 
  EstadoRequerimiento, 
  DevolucionActivoRequestDTO 
} from '../../../../core/models/requerimiento.model';
import { InventarioService } from '../../../inventario/services/inventario.service';
import { AuthService } from '../../../../core/auth/auth.service';


@Component({
  selector: 'app-requerimiento-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    SelectModule,
    InputTextModule,
    InputNumberModule,
    TextareaModule,
    TagModule,
    ToastModule,
    ConfirmDialogModule,
    TooltipModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './requerimiento-list.component.html',
  styleUrl: './requerimiento-list.css',
})
export class RequerimientoListComponent implements OnInit {
  private reqService = inject(RequerimientoService);
  private invService = inject(InventarioService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private cdr = inject(ChangeDetectorRef);
  private reportesService = inject(ReportesService);
  private platformId = inject(PLATFORM_ID);

  requerimientos: RequerimientoResponse[] = [];
  articulosDisponibles: any[] = [];

  // Modales
  displayDialogCrear: boolean = false;
  displayDialogDetalle: boolean = false;
  displayDialogDevolucion: boolean = false;
  
  requerimientoSeleccionado: RequerimientoResponse | null = null;
  detalleParaDevolucion: DetalleRequerimientoResponse | null = null;

  // Formularios Reactivos
  formReq!: FormGroup;
  formDevolucion!: FormGroup;

  // --- HELPERS RBAC PARA EL HTML ---
  puedeAprobarORechazar(): boolean {
    return this.authService.tieneRol(['JEFE_LOGISTICA']);
  }

  puedeDespachar(): boolean {
    return this.authService.tieneRol(['JEFE_LOGISTICA', 'ASISTENTE']);
  }

  puedeProcesarDevolucion(): boolean {
    return this.authService.tieneRol(['JEFE_LOGISTICA', 'ASISTENTE', 'AUXILIAR']);
  }

  loading: boolean = false;

  ngOnInit(): void {
    this.initForm();
    this.initFormDevolucion();

    if (isPlatformBrowser(this.platformId)) {
      this.cargarRequerimientos();
      this.cargarArticulos();
    }
  }

  cargarRequerimientos(): void {
    this.loading = true;
    this.reqService.listarTodos().subscribe({
      next: (data) => {
        this.requerimientos = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar la lista de requerimientos.'
        });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarArticulos(): void {
    this.invService.listarTodos().subscribe({
      next: (articulos) => {
        this.articulosDisponibles = articulos;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar artículos:', err);
      }
    });
  }

  initForm(): void {
    this.formReq = this.fb.group({
      areaSolicitante: ['', Validators.required],
      solicitante: ['', Validators.required],
      observacion: [''],
      detalles: this.fb.array([])
    });
    this.agregarFilaDetalle();
  }

  initFormDevolucion(): void {
    this.formDevolucion = this.fb.group({
      usuarioRecibe: ['ALMACÉN CENTRAL', Validators.required],
      observaciones: ['']
    });
  }

  get detallesFormArray(): FormArray {
    return this.formReq.get('detalles') as FormArray;
  }

  agregarFilaDetalle(): void {
    const fila = this.fb.group({
      articuloId: [null, Validators.required],
      cantidadSolicitada: [1, [Validators.required, Validators.min(0.01)]],
      caracteristicas: [''],
      sustento: ['']
    });
    this.detallesFormArray.push(fila);
  }

  eliminarFilaDetalle(index: number): void {
    if (this.detallesFormArray.length > 1) {
      this.detallesFormArray.removeAt(index);
    } else {
      this.messageService.add({ severity: 'warn', summary: 'Atención', detail: 'Debe ingresar al menos un producto en la solicitud.' });
    }
  }

  abrirNuevoModal(): void {
    const usuario = this.authService.getSesion();

    this.formReq.reset();
    this.detallesFormArray.clear();
    this.agregarFilaDetalle();

    // Autocompletar y bloquear Área y Solicitante con el usuario logueado
    if (usuario) {
      this.formReq.patchValue({
        areaSolicitante: usuario.area,
        solicitante: usuario.nombreCompleto
      });
      this.formReq.get('areaSolicitante')?.disable();
      this.formReq.get('solicitante')?.disable();
    }

    this.displayDialogCrear = true;
    setTimeout(() => this.cdr.detectChanges(), 0);
  }

  guardarRequerimiento(): void {
    if (this.formReq.invalid) {
      this.formReq.markAllAsTouched();
      return;
    }

    // getRawValue() incluye los campos deshabilitados (areaSolicitante y solicitante)
    const payload = this.formReq.getRawValue();

    this.reqService.crear(payload).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Requerimiento registrado correctamente.' });
        this.displayDialogCrear = false;
        this.cargarRequerimientos();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Error al guardar el requerimiento.' });
        setTimeout(() => this.cdr.detectChanges(), 0);
      }
    });
  }

  verDetalle(req: RequerimientoResponse): void {
    this.requerimientoSeleccionado = req;
    this.displayDialogDetalle = true;
    setTimeout(() => this.cdr.detectChanges(), 0);
  }

  cambiarEstado(req: RequerimientoResponse, nuevoEstado: EstadoRequerimiento): void {
    const mensajeConfirmacion = nuevoEstado === 'DESPACHADO' 
      ? `¿Confirmas despachar el requerimiento ${req.codigo}? Esto asignará los SKUs correspondientes y descontará el stock.`
      : `¿Estás seguro de cambiar el estado a ${nuevoEstado}?`;

    this.confirmationService.confirm({
      message: mensajeConfirmacion,
      header: 'Confirmar Acción',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, procesar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.reqService.cambiarEstado(req.id, nuevoEstado).subscribe({
          next: () => {
            this.messageService.add({ 
              severity: 'success', 
              summary: 'Estado Actualizado', 
              detail: `El requerimiento ${req.codigo} pasó a ${nuevoEstado}.` 
            });
            this.cargarRequerimientos();
            if (this.displayDialogDetalle) {
              this.displayDialogDetalle = false;
            }
          },
          error: (err) => {
            const mensajeError = err.error?.message || 'No se pudo procesar la operación.';
            this.messageService.add({ 
              severity: 'warn', 
              summary: 'Atención', 
              detail: mensajeError,
              life: 5000 
            });
            setTimeout(() => this.cdr.detectChanges(), 0);
          }
        });
      }
    });
  }

  // --- LÓGICA DE DEVOLUCIÓN DE EQUIPOS EN PRÉSTAMO (ÍTEM POR ÍTEM EN CUALQUIER ORDEN) ---
  abrirModalDevolucion(req: RequerimientoResponse, detalle: DetalleRequerimientoResponse): void {
    const usuario = this.authService.getSesion();
    this.requerimientoSeleccionado = req;
    this.detalleParaDevolucion = detalle;
    this.formDevolucion.reset({
      usuarioRecibe: usuario ? usuario.nombreCompleto.toUpperCase() : 'ALMACÉN CENTRAL',
      observaciones: ''
    });
    this.displayDialogDevolucion = true;
    setTimeout(() => this.cdr.detectChanges(), 0);
  }

  procesarDevolucion(): void {
    if (this.formDevolucion.invalid || !this.requerimientoSeleccionado || !this.detalleParaDevolucion) {
      this.formDevolucion.markAllAsTouched();
      return;
    }

    const dto: DevolucionActivoRequestDTO = {
      requerimientoId: this.requerimientoSeleccionado.id,
      requerimientoDetalleId: this.detalleParaDevolucion.id,
      sku: this.detalleParaDevolucion.skuAsignado,
      usuarioRecibe: this.formDevolucion.value.usuarioRecibe,
      observaciones: this.formDevolucion.value.observaciones
    };

    const nombreItemDevuelto = this.detalleParaDevolucion.articuloDescripcion;

    this.reqService.registrarDevolucion(dto).subscribe({
      next: (reqActualizado) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Ítem Devuelto',
          detail: `${nombreItemDevuelto} retornado al inventario como DISPONIBLE.`
        });
        this.displayDialogDevolucion = false;
        this.detalleParaDevolucion = null;
        // Actualiza el modal de detalle sin cerrarlo para poder devolver los demás ítems
        this.requerimientoSeleccionado = reqActualizado;
        this.cargarRequerimientos();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Error al procesar la devolución.' });
        setTimeout(() => this.cdr.detectChanges(), 0);
      }
    });
  }

  getSeverity(estado: EstadoRequerimiento): "success" | "info" | "warn" | "danger" | "secondary" | "contrast" | undefined {
    switch (estado) {
      case 'PENDIENTE': return 'warn';
      case 'APROBADO': return 'info';
      case 'DESPACHADO': return 'success';
      case 'ENTREGADO': return 'success';
      case 'DEVUELTO': return 'secondary';
      case 'RECHAZADO': return 'danger';
      case 'CANCELADO': return 'contrast';
      default: return 'secondary';
    }
  }

  exportarFichaPdf(id: number): void {
    this.reportesService.descargarRequerimientoPdf(id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Ficha_Requerimiento_REQ-${id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error al generar la ficha PDF:', err)
    });
  }
}