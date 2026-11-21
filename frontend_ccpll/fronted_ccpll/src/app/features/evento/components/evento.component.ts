import { ChangeDetectorRef, Component, OnInit, Inject, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';

import {
  Ambiente,
  CrearAmbienteRequest,
  ItemEventoRequest,
  ReservaEvento,
  SolicitudReservaRequest
} from '../../../core/models/evento.model';
import { AuthService } from '../../../core/auth/auth.service';
import { EventoService } from '../services/evento.service';
import { InventarioService } from '../../inventario/services/inventario.service';
import { InventarioResponseDTO } from '../../../core/models/inventario.model';

@Component({
  selector: 'app-eventos',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, DialogModule, ButtonModule, InputTextModule, TagModule],
  templateUrl: './evento.html'
})
export class EventosComponent implements OnInit {
  private eventoService = inject(EventoService);
  private inventarioService = inject(InventarioService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  ambientes: Ambiente[] = [];
  reservas: ReservaEvento[] = [];
  catalogoInventario: InventarioResponseDTO[] = [];

  hoyStr = this.obtenerFechaLocalISO(0);
  mananaStr = this.obtenerFechaLocalISO(1);
  fechaSeleccionada = this.mananaStr;

  horasDia = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00'];

  modalReserva = false;
  modalAmbiente = false;
  modalDetalleEvento = false;
  eventoSeleccionado: ReservaEvento | null = null;
  reservaEditandoId: number | null = null;

  guardando = false;
  errorReserva = '';
  mensajeExito = '';

  requiereRecursos = false;
  itemsEvento: ItemEventoRequest[] = [];

  reservaForm = this.fb.group({
    ambienteId: [1, Validators.required],
    nombreEvento: ['', Validators.required],
    fechaEvento: [this.mananaStr, Validators.required],
    horaInicio: ['09:00', Validators.required],
    horaFin: ['11:00', Validators.required],
    observaciones: ['']
  });

  ambienteForm = this.fb.group({
    nombre: ['', Validators.required],
    capacidad: [30, [Validators.required, Validators.min(1)]],
    ubicacion: ['', Validators.required]
  });

  get esJefeLogistica(): boolean {
    return this.authService.getSesion()?.rol === 'JEFE_LOGISTICA';
  }

  get puedeSolicitar(): boolean {
    const rol = this.authService.getSesion()?.rol;
    return rol === 'AREA_COMERCIAL' || rol === 'JEFE_LOGISTICA';
  }

  get fechaEsReservable(): boolean {
    return this.fechaSeleccionada >= this.mananaStr;
  }

  private obtenerFechaLocalISO(diasASumar: number): string {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + diasASumar);
    const yyyy = fecha.getFullYear();
    const mm = String(fecha.getMonth() + 1).padStart(2, '0');
    const dd = String(fecha.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  private mostrarNotificacion(texto: string): void {
    this.mensajeExito = texto;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.mensajeExito = '';
      this.cdr.detectChanges();
    }, 4500);
  }

 private platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.cargarAmbientes();
      this.cargarReservas();
      this.cargarCatalogoInventario();
    }
  }

  cargarAmbientes(): void {
    this.eventoService.listarAmbientes().subscribe(data => {
      this.ambientes = data;
      this.cdr.detectChanges();
    });
  }

  cargarReservas(): void {
    if (this.fechaSeleccionada < this.hoyStr) {
      this.fechaSeleccionada = this.hoyStr;
    }
    this.eventoService.listarReservasPorFecha(this.fechaSeleccionada).subscribe(data => {
      this.reservas = data.filter(r => r.estado !== 'RECHAZADO');
      if (this.eventoSeleccionado) {
        this.eventoSeleccionado = this.reservas.find(r => r.id === this.eventoSeleccionado!.id) || null;
      }
      this.cdr.detectChanges();
    });
  }

  cargarCatalogoInventario(): void {
    this.inventarioService.listarTodos().subscribe((data: InventarioResponseDTO[]) => {
      this.catalogoInventario = data.filter(i => i.activo && i.stockActual > 0);
      this.cdr.detectChanges();
    });
  }

  obtenerNombreArticulo(articuloId: number | null): string {
    if (!articuloId) return 'Artículo';
    const encontrado = this.catalogoInventario.find(p => p.id === articuloId);
    return encontrado ? `${encontrado.codigo} - ${encontrado.descripcion}` : `Artículo #${articuloId}`;
  }

  siguienteHora(hora: string): string {
    const h = parseInt(hora.split(':')[0], 10) + 1;
    return `${h.toString().padStart(2, '0')}:00`;
  }

  obtenerReservaEnBloque(ambienteId: number, horaBloque: string): ReservaEvento | undefined {
    return this.reservas.find(r =>
      r.ambiente.id === ambienteId &&
      r.horaInicio.slice(0, 5) <= horaBloque &&
      r.horaFin.slice(0, 5) > horaBloque
    );
  }

  // --- ABRIR MODAL DE DETALLE AL HACER CLIC EN UN EVENTO DEL CALENDARIO ---
  abrirDetalleEvento(res: ReservaEvento): void {
    this.eventoSeleccionado = res;
    this.modalDetalleEvento = true;
    this.cdr.detectChanges();
  }

  agregarItemEvento(): void {
    this.itemsEvento.push({ articuloId: null, cantidad: 1 });
  }

  quitarItemEvento(index: number): void {
    this.itemsEvento.splice(index, 1);
  }

  // --- ABRIR MODAL PARA NUEVA RESERVA ---
  abrirModalReserva(ambienteId?: number, hora?: string): void {
    this.reservaEditandoId = null;
    this.errorReserva = '';
    this.requiereRecursos = false;
    this.itemsEvento = [];

    const fechaInicialModal = this.fechaSeleccionada >= this.mananaStr
      ? this.fechaSeleccionada
      : this.mananaStr;

    this.reservaForm.patchValue({
      ambienteId: ambienteId ?? (this.ambientes[0]?.id || 1),
      fechaEvento: fechaInicialModal,
      horaInicio: hora ?? '09:00',
      horaFin: hora ? this.siguienteHora(hora) : '11:00',
      nombreEvento: '',
      observaciones: ''
    });
    this.modalReserva = true;
    this.cdr.detectChanges();
  }

  // --- ABRIR MODAL PARA EDITAR RESERVA PENDIENTE ---
  abrirEditarReserva(res: ReservaEvento): void {
    if (res.estado !== 'PENDIENTE') return;

    this.modalDetalleEvento = false;
    this.reservaEditandoId = res.id;
    this.errorReserva = '';

    const itemsPrevios: ItemEventoRequest[] = (res as any).items
      ? (res as any).items.map((i: any) => ({ articuloId: i.articuloId, cantidad: i.cantidad }))
      : [];

    this.requiereRecursos = itemsPrevios.length > 0;
    this.itemsEvento = itemsPrevios;

    this.reservaForm.patchValue({
      ambienteId: res.ambiente.id,
      nombreEvento: res.nombreEvento,
      fechaEvento: res.fechaEvento,
      horaInicio: res.horaInicio.slice(0, 5),
      horaFin: res.horaFin.slice(0, 5),
      observaciones: res.observaciones || ''
    });

    this.modalReserva = true;
    this.cdr.detectChanges();
  }

  guardarReserva(): void {
    if (this.reservaForm.invalid || this.guardando) return;
    const sesion = this.authService.getSesion();
    const formVal = this.reservaForm.getRawValue();

    if (formVal.fechaEvento! < this.mananaStr) {
      this.errorReserva = 'No se pueden separar eventos para hoy ni fechas pasadas. Seleccione a partir de mañana.';
      return;
    }

    const itemsValidos = this.requiereRecursos
      ? this.itemsEvento.filter(i => i.articuloId !== null && i.cantidad > 0)
      : [];

    const payload: SolicitudReservaRequest = {
      ambienteId: formVal.ambienteId!,
      nombreEvento: formVal.nombreEvento!,
      solicitante: sesion?.nombreCompleto || 'Usuario Comercial',
      areaSolicitante: sesion?.area || 'Área Comercial',
      fechaEvento: formVal.fechaEvento!,
      horaInicio: formVal.horaInicio!,
      horaFin: formVal.horaFin!,
      observaciones: formVal.observaciones || '',
      items: itemsValidos
    };

    this.guardando = true;
    this.errorReserva = '';
    this.cdr.detectChanges();

    const peticion$ = this.reservaEditandoId
      ? this.eventoService.editarReserva(this.reservaEditandoId, payload)
      : this.eventoService.solicitarReserva(payload);

    peticion$.subscribe({
      next: (res) => {
        const fueEdicion = this.reservaEditandoId !== null;
        this.guardando = false;
        this.modalReserva = false;
        this.reservaEditandoId = null;
        this.fechaSeleccionada = formVal.fechaEvento!;
        this.cargarReservas();
        this.mostrarNotificacion(
          fueEdicion
            ? `Reserva ${res.codigo} actualizada correctamente.`
            : `Solicitud ${res.codigo} registrada (pendiente de confirmación).`
        );
      },
      error: (err) => {
        this.guardando = false;
        this.errorReserva = err.error?.message || 'No se pudo procesar la reserva.';
        this.cdr.detectChanges();
      }
    });
  }

  confirmarYBloquear(id: number): void {
    this.eventoService.cambiarEstadoReserva(id, 'APROBADO').subscribe({
      next: (res) => {
        this.modalDetalleEvento = false;
        this.cargarReservas();
        this.mostrarNotificacion(`Evento ${res.codigo} confirmado, bloqueado y con recursos separados.`);
      },
      error: (err) => alert(err.error?.message || 'Error al confirmar la reserva')
    });
  }

  eliminarReserva(res: ReservaEvento): void {
    if (!confirm(`¿Está seguro de eliminar la reserva ${res.codigo} (${res.nombreEvento})? Esta acción quedará registrada en Auditoría.`)) {
      return;
    }

    this.eventoService.eliminarReserva(res.id).subscribe({
      next: () => {
        this.modalDetalleEvento = false;
        this.cargarReservas();
        this.mostrarNotificacion(`Reserva ${res.codigo} eliminada y registrada en auditoría.`);
      },
      error: (err) => alert(err.error?.message || 'No se pudo eliminar la reserva')
    });
  }

  guardarAmbiente(): void {
    if (this.ambienteForm.invalid || this.guardando) return;
    const formVal = this.ambienteForm.getRawValue();
    const payload: CrearAmbienteRequest = {
      nombre: formVal.nombre!,
      capacidad: formVal.capacidad!,
      ubicacion: formVal.ubicacion!
    };

    this.guardando = true;
    this.eventoService.crearAmbiente(payload).subscribe({
      next: () => {
        this.guardando = false;
        this.modalAmbiente = false;
        this.ambienteForm.reset({ capacidad: 30 });
        this.cargarAmbientes();
        this.mostrarNotificacion('Nuevo ambiente registrado correctamente.');
      },
      error: () => {
        this.guardando = false;
      }
    });
  }
}