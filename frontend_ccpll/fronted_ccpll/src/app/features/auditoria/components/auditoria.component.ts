import { Component, OnInit, inject, ChangeDetectorRef, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';

import { RegistroAuditoria } from '../../../core/models/auditoria.model';
import { AuditoriaService } from '../services/auditoria.service';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule, TableModule, TagModule, InputTextModule],
  templateUrl: './auditoria.html'
})
export class AuditoriaComponent implements OnInit {
  private auditoriaService = inject(AuditoriaService);
  private cdr = inject(ChangeDetectorRef);
  private platformId = inject(PLATFORM_ID);

  registros: RegistroAuditoria[] = [];
  entidadFiltro = 'TODOS';
  textoBusqueda = '';
  descargando = false;

  tabsEntidad = [
    { etiqueta: 'Todos', valor: 'TODOS' },
    { etiqueta: 'Accesos / Logins', valor: 'Usuario' },
    { etiqueta: 'Requerimientos', valor: 'Requerimiento' },
    { etiqueta: 'Inventario', valor: 'Inventario' },
    { etiqueta: 'Eventos / Ambientes', valor: 'ReservaEvento' }
  ];

  get registrosFiltrados(): RegistroAuditoria[] {
    return this.registros.filter(r => {
      const cumpleEntidad = this.entidadFiltro === 'TODOS' || r.entidad.toLowerCase().includes(this.entidadFiltro.toLowerCase());
      const q = this.textoBusqueda.toLowerCase().trim();
      const cumpleTexto = !q ||
        r.usuario.toLowerCase().includes(q) ||
        r.accion.toLowerCase().includes(q) ||
        r.detalle.toLowerCase().includes(q);
      return cumpleEntidad && cumpleTexto;
    });
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.cargarAuditoria();
    }
  }

  cargarAuditoria(): void {
    this.auditoriaService.listarTodo().subscribe({
      next: (data) => {
        this.registros = data || [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar auditoría:', err)
    });
  }

  descargarExcel(): void {
    this.descargando = true;
    this.cdr.detectChanges();

    this.auditoriaService.exportarExcelMultiHoja().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Auditoria_EVOLOG_CCLL_${new Date().toISOString().slice(0, 10)}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.descargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        alert('No se pudo exportar el reporte Excel de auditoría.');
        this.descargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  obtenerSeveridad(accion: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    if (accion.includes('APROB') || accion.includes('LOGIN') || accion.includes('DEVOLUCION')) return 'success';
    if (accion.includes('RECHAZ')) return 'danger';
    if (accion.includes('CREAR') || accion.includes('SOLICITAR')) return 'info';
    return 'warn';
  }
}