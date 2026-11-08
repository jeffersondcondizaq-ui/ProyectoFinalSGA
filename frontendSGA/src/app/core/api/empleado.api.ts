import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Empleado } from '../../shared/models';

/** GestionEmpleadoUseCase del backend. */
@Injectable({ providedIn: 'root' })
export class EmpleadoApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/empleados`;

  listar(): Observable<Empleado[]> {
    return this.http.get<Empleado[]>(this.url);
  }

  contratar(empleado: Empleado & { contrasena: string }): Observable<Empleado> {
    return this.http.post<Empleado>(this.url, empleado);
  }

  actualizar(documento: string, telefono: string, correo: string): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.url}/${documento}`, { telefono, correo });
  }

  desvincular(documento: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${documento}/desvincular`, {});
  }
}
