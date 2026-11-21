import { Component, OnInit, signal, computed, Inject, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';

// PrimeNG - Módulos compatibles v18+
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { CheckboxModule } from 'primeng/checkbox';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

// Servicios y Modelos
import { CategoriaService } from '../../services/categoria.service';
import { InventarioService } from '../../services/inventario.service';
import {
  EntradaInventarioRequestDTO,
  InventarioResponseDTO,
  SalidaInventarioRequestDTO,
  HistoriaInventarioResponseDTO,
  ResumenInventarioDTO,
  ActivoSerieDTO
} from '../../../../core/models/inventario.model';
import { CategoriaResponseDTO } from '../../../../core/models/categoria.model';
import { ReportesService } from '../../../reportes/reportes.service';
import { AuthService } from '../../../../core/auth/auth.service'; // Verifica que la ruta coincida con tu proyecto

@Component({
  selector: 'app-inventario-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    CheckboxModule,
    TagModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './inventario-list.html',
  styleUrl: './inventario-list.css',
})
export class InventarioList implements OnInit {

  private reportesService = inject(ReportesService);
  private authService = inject(AuthService);

  productos = signal<InventarioResponseDTO[]>([]);
  categorias = signal<CategoriaResponseDTO[]>([]);
  loading = signal<boolean>(false);

  // Buscador global por descripción (compartido en todas las vistas/pestañas)
  busquedaDescripcion = signal<string>('');

  // Lista filtrada reactiva por descripción o código
  productosFiltrados = computed(() => {
    const termino = this.busquedaDescripcion().trim().toLowerCase();
    const lista = this.productos();
    if (!termino) return lista;

    return lista.filter(p =>
      (p.descripcion || '').toLowerCase().includes(termino) ||
      (p.codigo || '').toLowerCase().includes(termino)
    );
  });

  busquedaSku = signal<string>('');

  skusFiltrados = computed(() => {
    const q = this.busquedaSku().trim().toLowerCase();
    const lista = this.listaSkusActivos();
    if (!q) return lista;
    return lista.filter(s =>
      (s.sku || '').toLowerCase().includes(q) ||
      (s.estado || '').toLowerCase().includes(q)
    );
  });

  // Detecta si el usuario es Cliente Interno o Área Comercial (modo solo vista)
  get esSoloConsulta(): boolean {
    const rol = this.authService.getSesion()?.rol;
    return rol === 'CLIENTE_INTERNO' || rol === 'AREA_COMERCIAL';
  }

  historialKardex = signal<HistoriaInventarioResponseDTO[]>([]);
  loadingKardex = signal<boolean>(false);
  kardexDialog = signal<boolean>(false);

  productoDialog = signal<boolean>(false);
  movimientoDialog = signal<boolean>(false);
  tipoMovimiento = signal<'ENTRADA' | 'SALIDA'>('ENTRADA');

  // Variables para controlar el modal emergente de Series / SKUs de Activos
  displayModalSkus = signal<boolean>(false);
  loadingSkus = signal<boolean>(false);
  listaSkusActivos = signal<ActivoSerieDTO[]>([]);
  productoSeleccionadoSku?: InventarioResponseDTO;

  resumenKPI = signal<ResumenInventarioDTO>({ totalProductos: 0, valorTotalInventario: 0, cantidadStockBajo: 0 });

  productoForm!: FormGroup;
  movimientoForm!: FormGroup;
  productoSeleccionado?: InventarioResponseDTO;

  // Variables para controlar el modal emergente de Nueva Categoría
  categoriaDialog: boolean = false;
  nuevaCategoriaNombre: string = '';

  constructor(
    private inventarioService: InventarioService,
    private categoriaService: CategoriaService,
    private fb: FormBuilder,
    private messageService: MessageService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.initForms();
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.cargarDatos();
    }
  }

  exportarExcel(): void {
    this.reportesService.descargarInventarioExcel().subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Inventario_CCLL_${new Date().toISOString().split('T')[0]}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error al exportar inventario a Excel:', err)
    });
  }

  initForms(): void {
    this.productoForm = this.fb.group({
      codigo: ['', [Validators.required, Validators.maxLength(50)]],
      descripcion: ['', [Validators.required, Validators.maxLength(200)]],
      unidad: ['UND', [Validators.required]],
      stockMinimo: [0, [Validators.required, Validators.min(0)]],
      categoriaId: [null, [Validators.required]],
      requiereSku: [false],
      ubicacionFisica: ['', [Validators.maxLength(100)]]
    });

    this.movimientoForm = this.fb.group({
      articuloId: [null, [Validators.required]],
      sku: [''],
      cantidad: [1, [Validators.required, Validators.min(0.01)]],
      costoUnitario: [0, [Validators.min(0)]],
      fecha: [new Date().toISOString().substring(0, 10), [Validators.required]],
      proveedorCliente: ['', [Validators.required, Validators.maxLength(100)]],
      area: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  cargarDatos(): void {
    this.loading.set(true);

    this.inventarioService.listarTodos().subscribe({
      next: (data: any) => {
        const lista = Array.isArray(data) ? data : (data.content || data.data || []);
        this.productos.set([...lista]);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar inventario:', err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el inventario' });
        this.loading.set(false);
      }
    });

    // Solo Logística consulta el resumen KPI valorizado (evita 403 para Cliente Interno / Comercial)
    if (!this.esSoloConsulta) {
      this.inventarioService.obtenerResumenKPI().subscribe({
        next: (kpi) => this.resumenKPI.set(kpi)
      });
    }

    this.cargarCategorias();
  }

  cargarCategorias(): void {
    this.categoriaService.listarTodas().subscribe({
      next: (data: any) => {
        const listaCat = Array.isArray(data) ? data : (data.content || data.data || []);
        this.categorias.set([...listaCat]);
      },
      error: (err) => console.error('Error al cargar categorías:', err)
    });
  }

  // --- MÉTODOS PARA VER LAS SERIES / SKUs INDIVIDUALES ---

  verSkusProducto(producto: InventarioResponseDTO): void {
    if (this.esSoloConsulta) return;
    this.productoSeleccionadoSku = producto;
    this.loadingSkus.set(true);
    this.displayModalSkus.set(true);

    this.inventarioService.obtenerSkusPorProducto(producto.id).subscribe({
      next: (skus) => {
        this.listaSkusActivos.set(skus);
        this.loadingSkus.set(false);
      },
      error: (err) => {
        console.error('Error al obtener SKUs:', err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron obtener las series del producto' });
        this.loadingSkus.set(false);
      }
    });
  }

  // --- MÉTODOS PARA EL MODAL DE NUEVA CATEGORÍA ---

  abrirModalNuevaCategoria(): void {
    this.nuevaCategoriaNombre = '';
    this.categoriaDialog = true;
  }

  guardarNuevaCategoria(): void {
    if (!this.nuevaCategoriaNombre.trim()) {
      this.messageService.add({ severity: 'warn', summary: 'Advertencia', detail: 'Ingrese el nombre de la categoría' });
      return;
    }

    const nombreUpper = this.nuevaCategoriaNombre.trim().toUpperCase();

    this.categoriaService.crear({ nombre: nombreUpper }).subscribe({
      next: (categoriaCreada: any) => {
        this.messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Categoría registrada correctamente' });
        this.cargarCategorias();

        if (categoriaCreada && categoriaCreada.id) {
          this.productoForm.patchValue({ categoriaId: categoriaCreada.id });
        }

        this.categoriaDialog = false;
        this.nuevaCategoriaNombre = '';
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Error al guardar categoría' });
      }
    });
  }

  // --- MÉTODOS DE PRODUCTO Y MOVIMIENTOS ---

  abrirNuevoProducto(): void {
    this.productoForm.reset({
      unidad: 'UND',
      stockMinimo: 0,
      requiereSku: false,
      ubicacionFisica: ''
    });
    this.productoDialog.set(true);
  }

  guardarProducto(): void {
    if (this.productoForm.invalid) return;

    this.inventarioService.crear(this.productoForm.value).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Producto registrado correctamente' });
        this.productoDialog.set(false);
        this.cargarDatos();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Error al guardar' });
      }
    });
  }

  abrirMovimiento(producto: InventarioResponseDTO, tipo: 'ENTRADA' | 'SALIDA'): void {
    this.productoSeleccionado = producto;
    this.tipoMovimiento.set(tipo);
    this.movimientoForm.reset({
      articuloId: producto.id,
      sku: '',
      cantidad: 1,
      costoUnitario: producto.costoPromedio,
      fecha: new Date().toISOString().substring(0, 10),
      proveedorCliente: '',
      area: ''
    });
    this.movimientoDialog.set(true);
  }

  guardarMovimiento(): void {
    if (this.movimientoForm.invalid) return;

    const formValues = this.movimientoForm.value;

    if (this.tipoMovimiento() === 'ENTRADA') {
      const payload: EntradaInventarioRequestDTO = {
        articuloId: formValues.articuloId,
        sku: formValues.sku || undefined,
        cantidad: formValues.cantidad,
        costoUnitario: formValues.costoUnitario,
        fecha: formValues.fecha,
        proveedor: formValues.proveedorCliente,
        areaDestino: formValues.area
      };

      this.inventarioService.registrarEntrada(payload).subscribe({
        next: () => this.handleSuccessMovimiento('Entrada'),
        error: (err) => this.handleErrorMovimiento(err)
      });
    } else {
      const payload: SalidaInventarioRequestDTO = {
        articuloId: formValues.articuloId,
        sku: formValues.sku || undefined,
        cantidad: formValues.cantidad,
        fecha: formValues.fecha,
        cliente: formValues.proveedorCliente,
        areaSolicita: formValues.area
      };

      this.inventarioService.registrarSalida(payload).subscribe({
        next: () => this.handleSuccessMovimiento('Salida'),
        error: (err) => this.handleErrorMovimiento(err)
      });
    }
  }

  private handleSuccessMovimiento(tipo: string): void {
    this.messageService.add({ severity: 'success', summary: 'Éxito', detail: `Movimiento de ${tipo} registrado correctamente` });
    this.movimientoDialog.set(false);
    this.cargarDatos();
  }

  private handleErrorMovimiento(err: any): void {
    this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Error al registrar movimiento' });
  }

  verKardex(producto: InventarioResponseDTO): void {
    if (this.esSoloConsulta) return;
    this.productoSeleccionado = producto;
    this.loadingKardex.set(true);
    this.kardexDialog.set(true);

    this.inventarioService.obtenerKardex(producto.id).subscribe({
      next: (data) => {
        this.historialKardex.set(data);
        this.loadingKardex.set(false);
      },
      error: (err) => {
        console.error('Error al obtener Kardex:', err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el historial del Kardex' });
        this.loadingKardex.set(false);
      }
    });
  }
}