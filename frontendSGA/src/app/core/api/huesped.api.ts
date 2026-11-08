import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Huesped } from '../../shared/models';

/** GestionHuespedUseCase del backend. */
@Injectable({ providedIn: 'root' })
export class HuespedApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/huespedes`;

  miPerfil(): Observable<Huesped> {
    return this.http.get<Huesped>(`${this.url}/yo`);
  }

  actualizarPerfil(datos: Pick<Huesped, 'telefono' | 'correo' | 'foto'>): Observable<Huesped> {
    return this.http.put<Huesped>(`${this.url}/yo`, datos);
  }

  buscar(documento: string): Observable<Huesped> {
    return this.http.get<Huesped>(`${this.url}/${documento}`);
  }
}
