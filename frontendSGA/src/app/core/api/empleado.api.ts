import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ContratarEmpleadoRequest, Empleado } from '../../shared/models';

/** GestionEmpleadoUseCase del backend. */
@Injectable({ providedIn: 'root' })
export class EmpleadoApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/empleados`;

  listar(): Observable<Empleado[]> {
    return this.http.get<Empleado[]>(this.url);
  }

  contratar(empleado: ContratarEmpleadoRequest): Observable<Empleado> {
    return this.http.post<Empleado>(this.url, empleado);
  }

  actualizar(
    documento: string,
    datos: Pick<Empleado, 'telefono' | 'correo'>,
  ): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.url}/${documento}`, datos);
  }

  desvincular(documento: string): Observable<Empleado> {
    return this.http.patch<Empleado>(`${this.url}/${documento}/desvincular`, {});
  }
}
