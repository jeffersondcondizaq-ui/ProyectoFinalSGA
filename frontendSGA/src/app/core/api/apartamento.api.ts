import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Apartamento, Bloqueo, EstadoApartamento, Novedad, Tarifa } from '../../shared/models';

/** GestionApartamentoUseCase del backend. */
@Injectable({ providedIn: 'root' })
export class ApartamentoApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/apartamentos`;

  listar(): Observable<Apartamento[]> {
    return this.http.get<Apartamento[]>(this.url);
  }

  buscar(codigo: string): Observable<Apartamento> {
    return this.http.get<Apartamento>(`${this.url}/${codigo}`);
  }

  crear(apartamento: Apartamento): Observable<Apartamento> {
    return this.http.post<Apartamento>(this.url, apartamento);
  }

  actualizar(codigo: string, apartamento: Partial<Apartamento>): Observable<Apartamento> {
    return this.http.put<Apartamento>(`${this.url}/${codigo}`, apartamento);
  }

  eliminar(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${codigo}`);
  }

  definirTarifas(codigo: string, tarifas: Tarifa[]): Observable<Apartamento> {
    return this.http.put<Apartamento>(`${this.url}/${codigo}/tarifas`, tarifas);
  }

  registrarBloqueo(codigo: string, bloqueo: Bloqueo): Observable<void> {
    return this.http.post<void>(`${this.url}/${codigo}/bloqueos`, bloqueo);
  }

  cambiarEstado(codigo: string, estado: EstadoApartamento, motivo?: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${codigo}/estado`, { estado, motivo });
  }

  registrarNovedad(codigo: string, novedad: Novedad): Observable<void> {
    return this.http.post<void>(`${this.url}/${codigo}/novedades`, novedad);
  }
}
