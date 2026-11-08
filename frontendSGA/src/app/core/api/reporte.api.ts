import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  IngresosPeriodo,
  NovedadesApartamento,
  OcupacionApartamento,
  Reserva,
  ReservasPorCanal,
} from '../../shared/models';

/** ReporteUseCase del backend. */
@Injectable({ providedIn: 'root' })
export class ReporteApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/reportes`;

  private rango(desde: string, hasta: string): HttpParams {
    return new HttpParams().set('desde', desde).set('hasta', hasta);
  }

  ocupacion(desde: string, hasta: string): Observable<OcupacionApartamento[]> {
    return this.http.get<OcupacionApartamento[]>(`${this.url}/ocupacion`, {
      params: this.rango(desde, hasta),
    });
  }

  ingresos(desde: string, hasta: string): Observable<IngresosPeriodo> {
    return this.http.get<IngresosPeriodo>(`${this.url}/ingresos`, {
      params: this.rango(desde, hasta),
    });
  }

  reservasPorCanal(): Observable<ReservasPorCanal> {
    return this.http.get<ReservasPorCanal>(`${this.url}/reservas-por-canal`);
  }

  saldosPendientes(): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(`${this.url}/saldos-pendientes`);
  }

  novedades(): Observable<NovedadesApartamento[]> {
    return this.http.get<NovedadesApartamento[]>(`${this.url}/novedades`);
  }
}
