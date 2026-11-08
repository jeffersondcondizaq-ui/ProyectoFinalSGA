import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpParams,
  HttpResponse,
} from '@angular/common/http';
import { catchError, delay, filter, map, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, RegistroHuespedRequest, CotizarRequest } from '../../shared/models';

/**
 * Mientras no hay backend, responde con los JSON de public/mocks:
 *  - GET  /api/xxx         → /mocks/xxx.json
 *  - POST /api/auth/login  → /mocks/auth/login/<usuario>.json (no valida la contraseña)
 */
const USUARIOS_MOCK = ['laura', 'recepcion', 'admin', 'pcardenas'];

export const mockInterceptor: HttpInterceptorFn = (req, next) => {
  if (!environment.usarMocks || !req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  const ruta = req.url.substring(environment.apiUrl.length); // '/apartamentos'

  if (req.method === 'POST' && ruta === '/auth/login') {
    const { usuario } = req.body as LoginRequest;
    const archivo = `/mocks/auth/login/${encodeURIComponent(usuario)}.json`;
    return next(req.clone({ method: 'GET', url: archivo, body: null })).pipe(
      // Si no existe el JSON de ese usuario, se responde como lo haría el backend.
      catchError(() =>
        throwError(
          () =>
            new HttpErrorResponse({
              status: 401,
              error: { mensaje: 'Usuario o contraseña incorrectos' },
            }),
        ),
      ),
    );
  }

  if (req.method === 'POST' && ruta === '/auth/registro') {
    const { contrasena, ...huesped } = req.body as RegistroHuespedRequest;
    if (USUARIOS_MOCK.includes(huesped.usuario.toLowerCase())) {
      return throwError(
        () => new HttpErrorResponse({ status: 409, error: { mensaje: 'Ese usuario ya existe' } }),
      );
    }
    // No se guarda en ningún lado: solo responde como si el backend lo hubiera creado.
    return of(new HttpResponse({ status: 201, body: huesped })).pipe(delay(500));
  }

  if (req.method === 'POST' && ruta === '/cotizaciones') {
    // Ejemplo fijo por apartamento: el cálculo real (temporadas, menores de 6) lo hace el backend.
    const { codigoApartamento } = req.body as CotizarRequest;
    return next(
      req.clone({
        method: 'GET',
        url: `/mocks/cotizaciones/${codigoApartamento}.json`,
        body: null,
      }),
    );
  }

  if (req.method === 'POST' && ruta === '/reservas/portal') {
    // Siempre responde la reserva Pendiente R-003; el backend real crea una nueva con su código.
    return next(req.clone({ method: 'GET', url: '/mocks/reservas/R-003.json', body: null }));
  }

  if (req.method === 'PUT') {
    // Lee el JSON actual y le mezcla lo que se envió, como si el backend lo hubiera guardado.
    return next(req.clone({ method: 'GET', url: `/mocks${ruta}.json`, body: null })).pipe(
      filter((evento) => evento instanceof HttpResponse),
      map((res) => res.clone({ body: { ...(res.body as object), ...(req.body as object) } })),
    );
  }

  if (req.method !== 'GET') {
    return next(req);
  }
  return next(req.clone({ url: `/mocks${ruta}.json`, params: new HttpParams() }));
};
