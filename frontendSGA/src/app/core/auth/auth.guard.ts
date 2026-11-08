import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { Rol } from '../../shared/models';
import { SesionService } from './sesion.service';

/** Solo deja entrar si hay sesión iniciada; si no, envía al login. */
export const autenticadoGuard: CanMatchFn = () => {
  const sesion = inject(SesionService);
  return sesion.autenticado() || inject(Router).createUrlTree(['/login']);
};

/** Solo deja entrar a los roles indicados. */
export const rolGuard =
  (...roles: Rol[]): CanMatchFn =>
  () => {
    const sesion = inject(SesionService);
    const router = inject(Router);
    if (!sesion.autenticado()) {
      return router.createUrlTree(['/login']);
    }
    return sesion.tieneRol(...roles) || router.createUrlTree(['/inicio']);
  };
