import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Huesped, LoginRequest, RegistroHuespedRequest, Sesion } from '../../shared/models';

/** AutenticacionUseCase del backend. */
@Injectable({ providedIn: 'root' })
export class AutenticacionApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/auth`;

  login(body: LoginRequest): Observable<Sesion> {
    return this.http.post<Sesion>(`${this.url}/login`, body);
  }

  registrarHuesped(body: RegistroHuespedRequest): Observable<Huesped> {
    return this.http.post<Huesped>(`${this.url}/registro`, body);
  }

  cambiarContrasena(actual: string, nueva: string): Observable<void> {
    return this.http.put<void>(`${this.url}/contrasena`, { actual, nueva });
  }
}
