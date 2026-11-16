import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Apartamento,
  ApartamentoRequest,
  Bloqueo,
  EstadoApartamento,
  NovedadRequest,
} from '../../shared/models';

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

  crear(apartamento: ApartamentoRequest): Observable<Apartamento> {
    return this.http.post<Apartamento>(this.url, apartamento);
  }

  actualizar(codigo: string, datos: Omit<ApartamentoRequest, 'codigo'>): Observable<Apartamento> {
    return this.http.put<Apartamento>(`${this.url}/${codigo}`, datos);
  }

  eliminar(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${codigo}`);
  }

  registrarBloqueo(codigo: string, bloqueo: Bloqueo): Observable<Apartamento> {
    return this.http.post<Apartamento>(`${this.url}/${codigo}/bloqueos`, bloqueo);
  }

  cambiarEstado(
    codigo: string,
    estado: EstadoApartamento,
    motivo?: string,
  ): Observable<Apartamento> {
    return this.http.patch<Apartamento>(`${this.url}/${codigo}/estado`, { estado, motivo });
  }

  registrarNovedad(codigo: string, novedad: NovedadRequest): Observable<void> {
    return this.http.post<void>(`${this.url}/${codigo}/novedades`, novedad);
  }
}
