import { Component, OnInit, inject, ChangeDetectorRef, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';

// Módulos PrimeNG
import { TableModule, Table } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { MessageService } from 'primeng/api';


import { ReportesService } from '../../../reportes/reportes.service';
import { HistorialInventarioService } from '../../services/historial-inventario';
import { HistoriaInventarioResponseDTO } from '../../../../core/models/historial.model';
import { OpcionOrden } from '../../../../core/models/orden.model';


@Component({
  selector: 'app-historial-inventario',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    SelectModule,
    InputTextModule,
    TagModule,
    ToastModule,
    TooltipModule,
    IconFieldModule,
    InputIconModule
  ],
  providers: [MessageService],
  templateUrl: './historial-inventario.html',
  styleUrl: './historial-inventario.css'
})
export class HistorialInventarioComponent implements OnInit {
  private historialService = inject(HistorialInventarioService);
  private reportesService = inject(ReportesService);
  private messageService = inject(MessageService);
  private cdr = inject(ChangeDetectorRef);
  private platformId = inject(PLATFORM_ID);
  

  movimientos: HistoriaInventarioResponseDTO[] = [];
  loading: boolean = true;

  // Combobox de Ordenamiento
  opcionesOrden: OpcionOrden[] = [
    { label: 'Fecha (Más recientes)', field: 'fecha', order: -1 },
    { label: 'Fecha (Más antiguas)', field: 'fecha', order: 1 },
    { label: 'Producto (A-Z)', field: 'articuloDescripcion', order: 1 },
    { label: 'Código Producto', field: 'articuloCodigo', order: 1 },
    { label: 'Proveedor / Área', field: 'proveedorCliente', order: 1 }
  ];

  criterioOrdenSeleccionado: OpcionOrden = this.opcionesOrden[0];

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        this.cargarMovimientos();
      }, 0);
    }
  }

  cargarMovimientos(): void {
    this.loading = true;
    this.historialService.listarTodos().subscribe({
      next: (data) => {
        this.movimientos = data;
        this.loading = false;
        setTimeout(() => this.cdr.detectChanges(), 0);
      },
      error: () => {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Error', 
          detail: 'No se pudo cargar el historial de movimientos.' 
        });
        this.loading = false;
        setTimeout(() => this.cdr.detectChanges(), 0);
      }
    });
  }

  aplicarOrdenamiento(table: Table): void {
    if (this.criterioOrdenSeleccionado) {
      table.sortField = this.criterioOrdenSeleccionado.field;
      table.sortOrder = this.criterioOrdenSeleccionado.order;
      table.sortSingle();
    }
  }

  exportarKardexExcel(): void {
    this.reportesService.descargarKardexExcel().subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Kardex_Movimientos_CCLL_${new Date().toISOString().split('T')[0]}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.messageService.add({ 
          severity: 'error', 
          summary: 'Error', 
          detail: 'Error al exportar el Kardex a Excel.' 
        });
      }
    });
  }

  getSeverity(tipo: 'ENTRADA' | 'SALIDA'): 'success' | 'warn' {
    return tipo === 'ENTRADA' ? 'success' : 'warn';
  }
}
