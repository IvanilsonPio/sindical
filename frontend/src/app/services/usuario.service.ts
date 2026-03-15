import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UsuarioAdmin {
  id: number;
  username: string;
  nome: string;
  status: string;
  role: string;
  criadoEm: string;
  atualizadoEm: string;
}

export interface UsuarioRequest {
  username: string;
  nome: string;
  password?: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  listar(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(this.apiUrl);
  }

  criar(request: UsuarioRequest): Observable<UsuarioAdmin> {
    return this.http.post<UsuarioAdmin>(this.apiUrl, request);
  }

  atualizar(id: number, request: UsuarioRequest): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.apiUrl}/${id}`, request);
  }

  alterarStatus(id: number, status: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/status`, { status });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
