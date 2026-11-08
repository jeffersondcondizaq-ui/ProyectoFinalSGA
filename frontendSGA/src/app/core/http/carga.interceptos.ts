import { HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { CargaService } from './carga.service';

/** Marca una petición para que NO muestre el indicador de carga (por ejemplo, el health). */
export const SIN_INDICADOR = new HttpContextToken<boolean>(() => false);

/** Muestra el indicador de carga mientras haya peticiones al API en curso. */
export const cargaInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(SIN_INDICADOR)) {
    return next(req);
  }
  const carga = inject(CargaService);
  carga.iniciar();
  return next(req).pipe(finalize(() => carga.terminar()));
};