import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Arquivo } from '../models/arquivo.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ArquivoService {
  private apiUrl = `${environment.apiUrl}/arquivos`;

  constructor(private http: HttpClient) {}

  uploadArquivos(socioId: number, arquivos: File[]): Observable<Arquivo[]> {
    const formData = new FormData();
    arquivos.forEach(arquivo => {
      formData.append('files', arquivo);
    });

    return this.http.post<Arquivo[]>(`${this.apiUrl}/upload/${socioId}`, formData);
  }

  listarArquivos(socioId: number): Observable<Arquivo[]> {
    return this.http.get<Arquivo[]>(`${this.apiUrl}/socio/${socioId}`);
  }

  downloadArquivo(arquivoId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${arquivoId}/download`, {
      responseType: 'blob'
    });
  }

  excluirArquivo(arquivoId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${arquivoId}`);
  }
}
