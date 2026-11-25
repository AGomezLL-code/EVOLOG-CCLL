import { Component, OnInit, inject, ChangeDetectorRef, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ChartModule } from 'primeng/chart';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { DashboardService } from '../../services/dashboard.service';
import { 
  DashboardKpiDTO, 
  ValorCategoriaDTO, 
  MovimientoRecienteDTO 
} from '../../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ChartModule, ButtonModule, TableModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private cdr = inject(ChangeDetectorRef);
  private platformId = inject(PLATFORM_ID);

  porcentajeAtencion: number = 0;

  kpis: DashboardKpiDTO = {
    totalArticulos: 0,
    valorTotalInventario: 0,
    articulosBajoStock: 0
  };

  movimientosRecientes: any[] = [];

  barData: any = { labels: [], datasets: [] };
  barOptions: any = {};

  categoriaChartData: any = { labels: [], datasets: [] };
  categoriaChartOptions: any = {};

  doughnutSolicitudesData: any = { labels: [], datasets: [] };
  doughnutSolicitudesOptions: any = {};

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.cargarKpis();
      this.cargarGraficoMovimientosReales();
      this.cargarValorPorCategoria();
      this.cargarMovimientosRecientes();
      this.cargarResumenSolicitudes();
    }
  }

  cargarKpis(): void {
    this.dashboardService.getKpis().subscribe({
      next: (data: any) => {
        queueMicrotask(() => {
          if (data) {
            this.kpis = {
              valorTotalInventario: data.inventarioValorizado ?? data.valorTotalInventario ?? 0,
              totalArticulos: data.unidadesDisponibles ?? data.totalArticulos ?? 0,
              articulosBajoStock: data.productosPorReponer ?? data.articulosBajoStock ?? 0
            };
          }
          this.cdr.markForCheck();
        });
      },
      error: (err) => console.error('Error al cargar KPIs:', err)
    });
  }

  cargarGraficoMovimientosReales(): void {
    this.dashboardService.getMovimientosMensuales().subscribe({
      next: (data: any[]) => {
        queueMicrotask(() => {
          const meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
          const labels = data.map((item: any) => {
            const rawMes = item.mes ?? item[0];
            const m = Number(rawMes);
            return !isNaN(m) && m >= 1 && m <= 12 ? meses[m - 1] : (rawMes || 'Actual');
          });
          const entradas = data.map((item: any) => item.entradas ?? item[1] ?? 0);
          const salidas = data.map((item: any) => item.salidas ?? item[2] ?? 0);

          this.barData = {
            labels: labels,
            datasets: [
              {
                label: 'Entradas',
                backgroundColor: '#1e3a8a',
                borderRadius: 4,
                data: entradas
              },
              {
                label: 'Salidas',
                backgroundColor: '#93c5fd',
                borderRadius: 4,
                data: salidas
              }
            ]
          };

          this.barOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'top',
                align: 'end',
                labels: { color: '#64748b', usePointStyle: true, boxWidth: 8 }
              }
            },
            scales: {
              x: { grid: { display: false }, ticks: { color: '#64748b' } },
              y: { grid: { color: '#f1f5f9' }, ticks: { color: '#64748b', precision: 0 } }
            }
          };

          this.cdr.markForCheck();
        });
      },
      error: (err) => console.error('Error al cargar movimientos mensuales reales:', err)
    });
  }

  cargarResumenSolicitudes(): void {
    this.dashboardService.getSolicitudesResumen().subscribe({
      next: (res: any) => {
        queueMicrotask(() => {
          const aprobadas = res?.aprobadas || 0;
          const pendientes = res?.pendientes || 0;
          const enRevision = res?.enRevision || 0;
          const rechazadas = res?.rechazadas || 0;

          const total = aprobadas + pendientes + enRevision + rechazadas;
          const procesadas = aprobadas + rechazadas;
          this.porcentajeAtencion = total > 0 ? Math.round((procesadas / total) * 100) : 0;

          this.doughnutSolicitudesData = {
            labels: ['Despachadas / Aprobadas', 'Pendientes', 'En Revisión', 'Rechazadas'],
            datasets: [
              {
                data: [aprobadas, pendientes, enRevision, rechazadas],
                backgroundColor: ['#22c55e', '#f59e0b', '#64748b', '#ef4444'],
                hoverBackgroundColor: ['#16a34a', '#d97706', '#475569', '#dc2626']
              }
            ]
          };

          this.doughnutSolicitudesOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'bottom',
                labels: {
                  color: '#334155',
                  usePointStyle: true,
                  padding: 15
                }
              }
            }
          };

          this.cdr.markForCheck();
        });
      },
      error: (err) => console.error('Error cargando el resumen de solicitudes:', err)
    });
  }

  cargarValorPorCategoria(): void {
    this.dashboardService.getValorPorCategoria().subscribe({
      next: (data: any[]) => {
        queueMicrotask(() => {
          this.categoriaChartData = {
            labels: data.map((item: any) => item.categoria ?? item.categoriaNombre ?? item.nombre ?? item[0] ?? 'Sin Categoría'),
            datasets: [
              {
                data: data.map((item: any) => Number(item.valor_total ?? item.valorTotal ?? item.total ?? item[1] ?? 0)),
                backgroundColor: ['#1e3a8a', '#2563eb', '#3b82f6', '#60a5fa', '#93c5fd'],
                hoverBackgroundColor: ['#172554', '#1d4ed8', '#2563eb', '#3b82f6', '#60a5fa']
              }
            ]
          };

          this.categoriaChartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'bottom',
                labels: { color: '#475569', font: { weight: '500', size: 11 } }
              }
            }
          };

          this.cdr.markForCheck();
        });
      },
      error: (err) => console.error('Error cargando valor por categoría:', err)
    });
  }

  cargarMovimientosRecientes(): void {
    this.dashboardService.getMovimientosRecientes().subscribe({
      next: (data: any[]) => {
        queueMicrotask(() => {
          this.movimientosRecientes = (data || []).map((item: any) => {
            if (Array.isArray(item)) {
              return {
                fecha: item[0],
                codigo: item[1],
                descripcion: item[2],
                tipo: item[3],
                cantidad: item[4],
                costoUnitario: item[5],
                subtotal: item[6],
                area: item[7]
              };
            }
            return {
              fecha: item.fecha ?? item.fechaMovimiento ?? item.fechaCreacion,
              codigo: item.codigo ?? item.articuloCodigo ?? item.articulo?.codigo ?? '-',
              descripcion: item.descripcion ?? item.articuloDescripcion ?? item.articulo?.descripcion ?? '-',
              tipo: item.tipo ?? item.tipoMovimiento ?? 'ENTRADA',
              cantidad: item.cantidad ?? 0,
              costoUnitario: item.costo_unitario ?? item.costoUnitario ?? 0,
              subtotal: item.subtotal ?? 0,
              area: item.area ?? item.areaDestino ?? item.proveedorCliente ?? 'ALMACÉN CENTRAL'
            };
          });
          this.cdr.markForCheck();
        });
      },
      error: (err) => console.error('Error cargando últimos movimientos:', err)
    });
  }
}