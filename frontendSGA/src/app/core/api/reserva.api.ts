import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Apartamento,
  ConflictoCanal,
  Cotizacion,
  CotizarRequest,
  CrearReservaRequest,
  Reserva,
  ReservaExternaRequest,
} from '../../shared/models';

/** ReservaUseCase del backend: disponibilidad, cotización, canales y ciclo de vida. */
@Injectable({ providedIn: 'root' })
export class ReservaApi {
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiUrl;

  disponibles(entrada: string, salida: string, personas: number): Observable<Apartamento[]> {
    const params = new HttpParams()
      .set('entrada', entrada)
      .set('salida', salida)
      .set('personas', personas);
    return this.http.get<Apartamento[]>(`${this.url}/disponibilidad`, { params });
  }

  cotizar(body: CotizarRequest): Observable<Cotizacion> {
    return this.http.post<Cotizacion>(`${this.url}/cotizaciones`, body);
  }

  crearPortal(body: CrearReservaRequest): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.url}/reservas/portal`, body);
  }

  crearDirecta(body: CrearReservaRequest): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.url}/reservas/directa`, body);
  }

  recibirExterna(body: ReservaExternaRequest): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.url}/reservas/externa`, body);
  }

  misReservas(): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(`${this.url}/reservas/mias`);
  }

  listar(): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(`${this.url}/reservas`);
  }

  buscar(codigo: string): Observable<Reserva> {
    return this.http.get<Reserva>(`${this.url}/reservas/${codigo}`);
  }

  confirmar(codigo: string): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.url}/reservas/${codigo}/confirmar`, {});
  }

  simularCancelacion(codigo: string): Observable<{ devolucion: number }> {
    return this.http.get<{ devolucion: number }>(`${this.url}/reservas/${codigo}/devolucion`);
  }

  cancelar(codigo: string): Observable<{ devolucion: number }> {
    return this.http.post<{ devolucion: number }>(`${this.url}/reservas/${codigo}/cancelar`, {});
  }

  checkIn(codigo: string): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.url}/reservas/${codigo}/check-in`, {});
  }

  checkOut(codigo: string): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.url}/reservas/${codigo}/check-out`, {});
  }

  declararNoShow(codigo: string): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.url}/reservas/${codigo}/no-show`, {});
  }

  conflictos(): Observable<ConflictoCanal[]> {
    return this.http.get<ConflictoCanal[]>(`${this.url}/conflictos`);
  }
}
