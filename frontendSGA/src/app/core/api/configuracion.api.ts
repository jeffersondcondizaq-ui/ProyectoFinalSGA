import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PeriodoTemporada, PoliticaCancelacion } from '../../shared/models';

/** ConfiguracionUseCase del backend: temporadas y política de cancelación. */
@Injectable({ providedIn: 'root' })
export class ConfiguracionApi {
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiUrl;

  listarTemporadas(): Observable<PeriodoTemporada[]> {
    return this.http.get<PeriodoTemporada[]>(`${this.url}/temporadas`);
  }

  registrarTemporada(periodo: PeriodoTemporada): Observable<PeriodoTemporada> {
    return this.http.post<PeriodoTemporada>(`${this.url}/temporadas`, periodo);
  }

  obtenerPolitica(): Observable<PoliticaCancelacion> {
    return this.http.get<PoliticaCancelacion>(`${this.url}/politica-cancelacion`);
  }

  configurarPolitica(politica: PoliticaCancelacion): Observable<PoliticaCancelacion> {
    return this.http.put<PoliticaCancelacion>(`${this.url}/politica-cancelacion`, politica);
  }
}
