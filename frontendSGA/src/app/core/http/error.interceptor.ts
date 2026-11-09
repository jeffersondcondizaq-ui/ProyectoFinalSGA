import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SesionService } from '../auth/sesion.service';

export const SIN_AVISO_ERROR = new HttpContextToken<boolean>(() => false);

/**
 * Muestra los errores del API en un snackbar.
 * El backend responde { mensaje } desde el GlobalExceptionHandler.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const sesion = inject(SesionService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        sesion.cerrar();
        router.navigate(['/login']);
      }
      const mensaje =
        error.error?.mensaje ??
        (error.status === 0 ? 'No hay conexión con el servidor' : 'Ocurrió un error inesperado');
      if (!req.context.get(SIN_AVISO_ERROR)) {
        snackBar.open(mensaje, 'Cerrar', { duration: 5000 });
      }
      return throwError(() => error);
    }),
  );
};
