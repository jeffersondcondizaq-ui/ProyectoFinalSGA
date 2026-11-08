import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CargoRequest, Folio, PagoRequest } from '../../shared/models';

/** FolioUseCase del backend: pagos, cargos y correcciones. */
@Injectable({ providedIn: 'root' })
export class FolioApi {
  private readonly http = inject(HttpClient);

  private url(codigoReserva: string): string {
    return `${environment.apiUrl}/reservas/${codigoReserva}/folio`;
  }

  obtener(codigoReserva: string): Observable<Folio> {
    return this.http.get<Folio>(this.url(codigoReserva));
  }

  registrarPago(codigoReserva: string, pago: PagoRequest): Observable<Folio> {
    return this.http.post<Folio>(`${this.url(codigoReserva)}/pagos`, pago);
  }

  registrarCargo(codigoReserva: string, cargo: CargoRequest): Observable<Folio> {
    return this.http.post<Folio>(`${this.url(codigoReserva)}/cargos`, cargo);
  }

  corregir(codigoReserva: string, idMovimiento: string): Observable<Folio> {
    return this.http.post<Folio>(
      `${this.url(codigoReserva)}/movimientos/${idMovimiento}/correccion`,
      {},
    );
  }
}
