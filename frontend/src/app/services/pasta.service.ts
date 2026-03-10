import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Pasta {
  id: number;
  nome: string;
  descricao?: string;
  caminhoCompleto: string;
  pastaPaiId?: number;
  quantidadeSubpastas: number;
  quantidadeArquivos: number;
  criadoEm: string;
  atualizadoEm: string;
}

export interface PastaRequest {
  nome: string;
  descricao?: string;
  pastaPaiId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class PastaService {
  private apiUrl = `${environment.apiUrl}/pastas`;

  constructor(private http: HttpClient) {}

  criarPasta(request: PastaRequest): Observable<Pasta> {
    return this.http.post<Pasta>(this.apiUrl, request);
  }

  buscarPasta(id: number): Observable<Pasta> {
    return this.http.get<Pasta>(`${this.apiUrl}/${id}`);
  }

  listarPastasRaiz(): Observable<Pasta[]> {
    return this.http.get<Pasta[]>(`${this.apiUrl}/raiz`);
  }

  listarSubpastas(id: number): Observable<Pasta[]> {
    return this.http.get<Pasta[]>(`${this.apiUrl}/${id}/subpastas`);
  }

  atualizarPasta(id: number, request: PastaRequest): Observable<Pasta> {
    return this.http.put<Pasta>(`${this.apiUrl}/${id}`, request);
  }

  excluirPasta(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
