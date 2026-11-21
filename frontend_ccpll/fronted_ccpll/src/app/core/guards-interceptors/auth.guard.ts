import { CanActivateFn, Router } from '@angular/router';
import { inject,PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

import { AuthService } from '../auth/auth.service';


export const authGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // En SSR (servidor), dejar pasar la renderización inicial para que el navegador lea localStorage al hidratar
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (!authService.estaAutenticado()) {
    router.navigate(['/login']);
    return false;
  }

  const rolesPermitidos = route.data?.['roles'] as string[] | undefined;
  const sesion = authService.getSesion();

  if (rolesPermitidos && sesion && !rolesPermitidos.includes(sesion.rol)) {
    if (sesion.rol === 'AREA_COMERCIAL') {
      router.navigate(['/eventos']);
    } else if (sesion.rol === 'CLIENTE_INTERNO') {
      router.navigate(['/requerimientos']);
    } else {
      router.navigate(['/dashboard']);
    }
    return false;
  }

  return true;
};