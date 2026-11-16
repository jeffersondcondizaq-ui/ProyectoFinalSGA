import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpParams,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { catchError, delay, filter, map, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Apartamento,
  ApartamentoRequest,
  Bloqueo,
  CargoRequest,
  ContratarEmpleadoRequest,
  CotizarRequest,
  Empleado,
  EstadoApartamento,
  EstadoReserva,
  Folio,
  LoginRequest,
  MovimientoFolio,
  NuevoHuespedRequest,
  PagoRequest,
  RegistroHuespedRequest,
  Reserva,
  ReservaExternaRequest,
  TipoMovimiento,
} from '../../shared/models';
import { aFechaIso } from '../../shared/utils/fechas';

/**
 * Mientras no hay backend, responde con los JSON de public/mocks:
 *  - GET  /api/xxx         → /mocks/xxx.json
 *  - POST /api/auth/login  → /mocks/auth/login/<usuario>.json (no valida la contraseña)
 *  - El resto de POST/PUT/PATCH/DELETE se simulan abajo. Nada se guarda: al recargar vuelven los JSON.
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

  if (req.method === 'POST' && ruta === '/huespedes') {
    // El backend real lo guarda y le asigna usuario = documento.
    const huesped = req.body as NuevoHuespedRequest;
    return of(
      new HttpResponse({ status: 201, body: { ...huesped, usuario: huesped.documento } }),
    ).pipe(delay(400));
  }

  if (req.method === 'POST' && ruta === '/reservas/directa') {
    return next(
      req.clone({ method: 'GET', url: '/mocks/reservas/directa-creada.json', body: null }),
    );
  }

  if (req.method === 'POST' && ruta === '/reservas/externa') {
    // Simula las dos reglas del enunciado; el backend real las valida contra las reservas reales.
    const externa = req.body as ReservaExternaRequest;
    if (externa.codigoExterno.toUpperCase() === 'BK-558201') {
      return throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { mensaje: 'El código externo BK-558201 ya creó la reserva R-004' },
          }),
      );
    }
    if (externa.codigoApartamento === 'APT-101') {
      return throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: {
              mensaje: 'Choca con la reserva R-001: quedó registrada como conflicto de canal',
              conflicto: true,
            },
          }),
      );
    }
    return next(req.clone({ method: 'GET', url: '/mocks/reservas/R-004.json', body: null }));
  }

  if (req.method === 'POST' && ruta === '/reservas/portal') {
    // Siempre responde la reserva Pendiente R-003; el backend real crea una nueva con su código.
    return next(req.clone({ method: 'GET', url: '/mocks/reservas/R-003.json', body: null }));
  }

  // --- Folio: pagos, cargos y correcciones ---
  const folioPost = ruta.match(
    /^\/reservas\/([^/]+)\/folio\/(pagos|cargos|movimientos\/([^/]+)\/correccion)$/,
  );
  if (req.method === 'POST' && folioPost) {
    const [, codigo, accion, idMovimiento] = folioPost;
    return conFolio(req, next, codigo, (folio) => {
      if (accion === 'pagos') {
        const pago = req.body as PagoRequest;
        return [
          ...folio.movimientos,
          nuevoMovimiento('PAGO', pago.valor, pago.descripcion, { medio: pago.medio }),
        ];
      }
      if (accion === 'cargos') {
        const cargo = req.body as CargoRequest;
        return [
          ...folio.movimientos,
          nuevoMovimiento('CARGO', cargo.valor, cargo.descripcion, { tipoCargo: cargo.tipo }),
        ];
      }
      // Corrección: movimiento contrario al original (el original nunca se modifica).
      const original = folio.movimientos.find((m) => m.id === idMovimiento);
      if (!original) {
        return folio.movimientos;
      }
      return [
        ...folio.movimientos,
        nuevoMovimiento(original.tipo, -original.valor, `Corrección: ${original.descripcion}`, {
          esCorreccion: true,
          medio: original.medio,
          tipoCargo: original.tipoCargo,
        }),
      ];
    });
  }

  // --- Ciclo de vida: confirmar, check-in, check-out y no-show ---
  const transicion = ruta.match(/^\/reservas\/([^/]+)\/(confirmar|check-in|check-out|no-show)$/);
  if (req.method === 'POST' && transicion) {
    const [, codigo, accion] = transicion;
    return next(
      req.clone({ method: 'GET', url: `/mocks/reservas/${codigo}.json`, body: null }),
    ).pipe(
      filter((evento) => evento instanceof HttpResponse),
      map((res) => res.clone({ body: { ...(res.body as Reserva), estado: ESTADO_TRAS[accion] } })),
    );
  }

  // POST /reservas/R-001/cancelar → responde la misma simulación de /devolucion.
  const cancelar = ruta.match(/^\/reservas\/([^/]+)\/cancelar$/);
  if (req.method === 'POST' && cancelar) {
    return next(
      req.clone({
        method: 'GET',
        url: `/mocks/reservas/${cancelar[1]}/devolucion.json`,
        body: null,
      }),
    );
  }

  // --- Apartamentos (recepción y administrador) ---
  if (req.method === 'POST' && ruta === '/apartamentos') {
    const nuevo = req.body as ApartamentoRequest;
    const creado: Apartamento = { ...nuevo, estado: 'PREPARADO', activo: true, bloqueos: [] };
    return of(new HttpResponse({ status: 201, body: creado })).pipe(delay(400));
  }
  if (req.method === 'DELETE' && /^\/apartamentos\/[^/]+$/.test(ruta)) {
    return of(new HttpResponse({ status: 204 })).pipe(delay(300));
  }
  const apto = ruta.match(/^\/apartamentos\/([^/]+)\/(estado|bloqueos|novedades)$/);
  if (apto && req.method === 'PATCH' && apto[2] === 'estado') {
    const { estado, motivo } = req.body as { estado: EstadoApartamento; motivo?: string };
    return conJson<Apartamento>(req, next, `/mocks/apartamentos/${apto[1]}.json`, (a) => ({
      ...a,
      estado,
      motivoFueraServicio: motivo,
    }));
  }
  if (apto && req.method === 'POST' && apto[2] === 'bloqueos') {
    const bloqueo = req.body as Bloqueo;
    return conJson<Apartamento>(req, next, `/mocks/apartamentos/${apto[1]}.json`, (a) => ({
      ...a,
      bloqueos: [...(a.bloqueos ?? []), bloqueo],
    }));
  }
  if (apto && req.method === 'POST' && apto[2] === 'novedades') {
    return of(new HttpResponse({ status: 201 })).pipe(delay(300));
  }

  // --- Configuración: temporadas (la política usa el PUT genérico de abajo) ---
  if (req.method === 'POST' && ruta === '/temporadas') {
    return of(new HttpResponse({ status: 201, body: req.body })).pipe(delay(300));
  }

  // --- Empleados ---
  if (req.method === 'POST' && ruta === '/empleados') {
    const { contrasena, ...datos } = req.body as ContratarEmpleadoRequest;
    if (USUARIOS_MOCK.includes(datos.usuario.toLowerCase())) {
      return throwError(
        () => new HttpErrorResponse({ status: 409, error: { mensaje: 'Ese usuario ya existe' } }),
      );
    }
    const contratado: Empleado = {
      ...datos,
      rol: 'RECEPCIONISTA',
      fechaContratacion: aFechaIso(new Date()),
      activo: true,
    };
    return of(new HttpResponse({ status: 201, body: contratado })).pipe(delay(400));
  }
  const empleado = ruta.match(/^\/empleados\/([^/]+)(\/desvincular)?$/);
  if (empleado && (req.method === 'PUT' || req.method === 'PATCH')) {
    const [, documento, desvincular] = empleado;
    const cambios = desvincular ? { activo: false } : (req.body as Partial<Empleado>);
    // No hay un JSON por empleado: se toma de la lista y se le aplican los cambios.
    return conJson<Empleado[], Empleado>(req, next, '/mocks/empleados.json', (lista) => ({
      ...lista.find((e) => e.documento === documento)!,
      ...cambios,
    }));
  }

  // --- PUT genérico: lee el JSON y le mezcla lo enviado (perfil, apartamento, política...) ---
  if (req.method === 'PUT') {
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

/** Estado en que queda la reserva tras cada acción (en el backend lo decide EstadoReserva). */
const ESTADO_TRAS: Record<string, EstadoReserva> = {
  confirmar: 'CONFIRMADA',
  'check-in': 'EN_CURSO',
  'check-out': 'FINALIZADA',
  'no-show': 'NO_SHOW',
};

/** Lee un JSON de mocks y responde con lo que devuelva "cambiar" (simula el guardado). */
function conJson<T, R = T>(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  url: string,
  cambiar: (actual: T) => R,
) {
  return next(req.clone({ method: 'GET', url, body: null })).pipe(
    filter((evento) => evento instanceof HttpResponse),
    map((res) => res.clone({ body: cambiar(res.body as T) })),
  );
}

function nuevoMovimiento(
  tipo: TipoMovimiento,
  valor: number,
  descripcion: string,
  extra: Partial<MovimientoFolio> = {},
): MovimientoFolio {
  return {
    id: `M-${Date.now()}`,
    tipo,
    fecha: new Date().toISOString(),
    valor,
    descripcion,
    esCorreccion: false,
    ...extra,
  };
}

/** Lee el folio del JSON, le aplica el cambio y recalcula totales y saldo. */
function conFolio(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  codigo: string,
  cambiar: (folio: Folio) => MovimientoFolio[],
) {
  return conJson<Folio>(req, next, `/mocks/reservas/${codigo}/folio.json`, (folio) => {
    const movimientos = cambiar(folio);
    const suma = (tipo: TipoMovimiento) =>
      movimientos.filter((m) => m.tipo === tipo).reduce((total, m) => total + m.valor, 0);
    const totalCargos = suma('CARGO');
    const totalPagos = suma('PAGO');
    return { ...folio, movimientos, totalCargos, totalPagos, saldo: totalCargos - totalPagos };
  });
}
