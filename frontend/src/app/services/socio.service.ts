import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Socio, SocioRequest, FiltroSocio } from '../models/socio.model';
import { PagedResponse } from '../models/arquivo.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SocioService {
  private apiUrl = `${environment.apiUrl}/socios`;

  constructor(private http: HttpClient) {}

  listarSocios(filtros: FiltroSocio): Observable<PagedResponse<Socio>> {
    let params = new HttpParams();
    
    if (filtros.termo) params = params.set('termo', filtros.termo);
    if (filtros.status) params = params.set('status', filtros.status);
    if (filtros.page !== undefined) params = params.set('page', filtros.page.toString());
    if (filtros.size !== undefined) params = params.set('size', filtros.size.toString());

    return this.http.get<PagedResponse<Socio>>(this.apiUrl, { params });
  }

  buscarSocio(id: number): Observable<Socio> {
    return this.http.get<Socio>(`${this.apiUrl}/${id}`);
  }

  criarSocio(socio: SocioRequest): Observable<Socio> {
    return this.http.post<Socio>(this.apiUrl, socio);
  }

  atualizarSocio(id: number, socio: SocioRequest): Observable<Socio> {
    return this.http.put<Socio>(`${this.apiUrl}/${id}`, socio);
  }

  excluirSocio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
