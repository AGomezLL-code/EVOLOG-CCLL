import { Routes } from '@angular/router';
import { authGuard } from './core/guards-interceptors/auth.guard';

export const routes: Routes = [
  // Ruta pública (fuera del Layout lateral)
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/components/login.component').then(m => m.LoginComponent)
  },
  // Rutas protegidas dentro del Layout principal
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/components/layout/layout.component').then(m => m.LayoutComponent),
    children: [
      {
        path: '',
        redirectTo: 'requerimientos',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        canActivate: [authGuard],
        data: { roles: ['JEFE_LOGISTICA', 'ASISTENTE', 'AUXILIAR'] },
        loadComponent: () =>
          import('./features/dashboard/components/dashboard/dashboard.component').then(
            m => m.DashboardComponent
          )
      },
      {
        path: 'requerimientos',
        loadComponent: () =>
          import('./features/requerimientos/components/requerimiento-list/requerimiento-list.component').then(
            m => m.RequerimientoListComponent
          )
      },
      {
        path: 'eventos',
        canActivate: [authGuard],
        data: { roles: ['JEFE_LOGISTICA', 'AREA_COMERCIAL'] },
        loadComponent: () =>
          import('./features/evento/components/evento.component').then(
            m => m.EventosComponent
          )
      },
      {
        path: 'inventario',
        loadComponent: () => import('./features/inventario/components/inventario-list/inventario-list.component').then(m => m.InventarioList),
        canActivate: [authGuard],
        data: { roles: ['JEFE_LOGISTICA', 'ASISTENTE', 'AUXILIAR', 'CLIENTE_INTERNO', 'AREA_COMERCIAL'] }
      },
      {
        path: 'movimientos',
        canActivate: [authGuard],
        data: { roles: ['JEFE_LOGISTICA', 'ASISTENTE', 'AUXILIAR'] },
        loadComponent: () =>
          import('./features/historial/components/historial-inventario/historial-inventario').then(
            m => m.HistorialInventarioComponent
          )
      },
      {
        path: 'auditoria',
        canActivate: [authGuard],
        data: { roles: ['JEFE_LOGISTICA'] },
        loadComponent: () =>
          import('./features/auditoria/components/auditoria.component').then(
            m => m.AuditoriaComponent
          )
      },
      {
        path: 'mi-perfil',
        loadComponent: () =>
          import('./features/perfil/components/perfil.component').then(
            m => m.PerfilComponent
          )
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];