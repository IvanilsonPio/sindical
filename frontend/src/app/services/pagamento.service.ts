import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Pagamento, PagamentoRequest, FiltroPagamento } from '../models/pagamento.model';
import { PagedResponse } from '../models/common.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PagamentoService {
  private apiUrl = `${environment.apiUrl}/pagamentos`;

  constructor(private http: HttpClient) {}

  listarPagamentos(filtros: FiltroPagamento): Observable<PagedResponse<Pagamento>> {
    let params = new HttpParams();
    
    if (filtros.socioId) params = params.set('socioId', filtros.socioId.toString());
    if (filtros.mes) params = params.set('mes', filtros.mes.toString());
    if (filtros.ano) params = params.set('ano', filtros.ano.toString());
    if (filtros.status) params = params.set('status', filtros.status);
    if (filtros.page !== undefined) params = params.set('page', filtros.page.toString());
    if (filtros.size !== undefined) params = params.set('size', filtros.size.toString());

    return this.http.get<PagedResponse<Pagamento>>(this.apiUrl, { params });
  }

  registrarPagamento(pagamento: PagamentoRequest): Observable<Pagamento> {
    return this.http.post<Pagamento>(this.apiUrl, pagamento);
  }

  cancelarPagamento(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  gerarRecibo(pagamentoId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${pagamentoId}/recibo`, {
      responseType: 'blob'
    });
  }

  gerarReciboSegundaVia(pagamentoId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${pagamentoId}/recibo/segunda-via`, {
      responseType: 'blob'
    });
  }

  listarRecibosPorSocio(socioId: number): Observable<Pagamento[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '100');
    
    return this.http.get<PagedResponse<Pagamento>>(`${this.apiUrl}/socio/${socioId}`, { params })
      .pipe(
        map(response => response.content)
      );
  }
}
