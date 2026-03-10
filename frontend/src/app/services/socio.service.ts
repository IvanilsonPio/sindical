import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Socio, SocioRequest, FiltroSocio, SocioDetalhadoResponse, HistoricoAlteracaoResponse } from '../models/socio.model';
import { PagedResponse } from '../models/common.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SocioService {
  private apiUrl = `${environment.apiUrl}/socios`;

  constructor(private http: HttpClient) {}

  listarSocios(filtros: FiltroSocio): Observable<PagedResponse<Socio>> {
    let params = new HttpParams();

    if (filtros.termo) params = params.set('search', filtros.termo);
    if (filtros.status) params = params.set('status', filtros.status);
    if (filtros.page !== undefined) params = params.set('page', filtros.page.toString());
    if (filtros.size !== undefined) params = params.set('size', filtros.size.toString());

    return this.http.get<PagedResponse<Socio>>(this.apiUrl, { params });
  }

  buscarSocio(id: number): Observable<Socio> {
    return this.http.get<Socio>(`${this.apiUrl}/${id}`);
  }

  getSocioDetalhado(id: number): Observable<SocioDetalhadoResponse> {
    return this.http.get<SocioDetalhadoResponse>(`${this.apiUrl}/${id}/detalhes`);
  }

  criarSocio(socio: SocioRequest): Observable<Socio> {
    return this.http.post<Socio>(this.apiUrl, socio);
  }

  atualizarSocio(id: number, socio: SocioRequest): Observable<Socio> {
    return this.http.put<Socio>(`${this.apiUrl}/${id}`, socio);
  }

  /**
   * Update socio with comprehensive error handling
   * Handles validation errors (400), not found (404), and conflict errors (409)
   */
  updateSocio(id: number, socio: SocioRequest): Observable<Socio> {
    return this.http.put<Socio>(`${this.apiUrl}/${id}`, socio).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle specific error cases
        if (error.status === 400) {
          // Validation error - return error with validation details
          return throwError(() => ({
            status: 400,
            message: 'Erro de validação. Verifique os dados fornecidos.',
            error: error.error
          }));
        } else if (error.status === 404) {
          // Not found error
          return throwError(() => ({
            status: 404,
            message: 'Sócio não encontrado.',
            error: error.error
          }));
        } else if (error.status === 409) {
          // Conflict error (duplicate CPF or matricula)
          const errorMessage = error.error?.message || 'Conflito de dados. CPF ou matrícula já cadastrados.';
          return throwError(() => ({
            status: 409,
            message: errorMessage,
            error: error.error
          }));
        } else {
          // Other errors
          return throwError(() => ({
            status: error.status,
            message: 'Erro ao atualizar sócio. Tente novamente.',
            error: error.error
          }));
        }
      })
    );
  }

  excluirSocio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get history of changes for a socio
   * @param id Socio ID
   * @returns Observable of HistoricoAlteracaoResponse array
   */
  getHistoricoAlteracoes(id: number): Observable<HistoricoAlteracaoResponse[]> {
    return this.http.get<HistoricoAlteracaoResponse[]>(`${this.apiUrl}/${id}/historico`);
  }
}
