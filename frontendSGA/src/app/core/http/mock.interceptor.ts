import { HttpInterceptorFn, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/** Mientras no hay backend: GET /api/xxx → /mocks/xxx.json (carpeta public/mocks). */
export const mockInterceptor: HttpInterceptorFn = (req, next) => {
  if (!environment.usarMocks || req.method !== 'GET' || !req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  const ruta = req.url.substring(environment.apiUrl.length);   // '/apartamentos'
    return next(req.clone({ url: `/mocks${ruta}.json`, params: new HttpParams() }));
};