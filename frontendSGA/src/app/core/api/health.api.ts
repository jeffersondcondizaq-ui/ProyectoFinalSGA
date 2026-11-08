import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SIN_INDICADOR } from '../http/carga.interceptos';

export interface HealthResponse {
  status: string;
  components?: Record<string, { status: string }>;
}

@Injectable({ providedIn: 'root' })
export class HealthApi {
  private readonly http = inject(HttpClient);

  check(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${environment.apiUrl}/health`, {
      context: new HttpContext().set(SIN_INDICADOR, true),
    });
  }
}
