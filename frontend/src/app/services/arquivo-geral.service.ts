import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ArquivoGeral {
  id: number;
  nomeOriginal: string;
  tipoConteudo: string;
  tamanho: number;
  tamanhoFormatado: string;
  descricao?: string;
  pastaId?: number;
  pastaNome?: string;
  criadoEm: string;
  atualizadoEm: string;
}

@Injectable({
  providedIn: 'root'
})
export class ArquivoGeralService {
  private apiUrl = `${environment.apiUrl}/arquivos-gerais`;

  constructor(private http: HttpClient) {}

  uploadArquivos(files: File[], pastaId?: number): Observable<ArquivoGeral[]> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    
    const url = pastaId ? `${this.apiUrl}?pastaId=${pastaId}` : this.apiUrl;
    return this.http.post<ArquivoGeral[]>(url, formData);
  }

  listarArquivos(pastaId?: number): Observable<ArquivoGeral[]> {
    const url = pastaId ? `${this.apiUrl}?pastaId=${pastaId}` : this.apiUrl;
    return this.http.get<ArquivoGeral[]>(url);
  }

  buscarArquivo(id: number): Observable<ArquivoGeral> {
    return this.http.get<ArquivoGeral>(`${this.apiUrl}/${id}`);
  }

  downloadArquivo(id: number, nomeOriginal: string): void {
    this.http.get(`${this.apiUrl}/${id}/download`, { 
      responseType: 'blob' 
    }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = nomeOriginal;
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }

  excluirArquivo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
