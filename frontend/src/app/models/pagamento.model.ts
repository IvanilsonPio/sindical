export interface Pagamento {
  id: number;
  socioId: number;
  socioNome?: string;
  valor: number;
  mes: number;
  ano: number;
  dataPagamento: string;
  numeroRecibo: string;
  observacoes?: string;
  status: string;
  criadoEm: string;
}

export interface PagamentoRequest {
  socioId: number;
  valor: number;
  mes: number;
  ano: number;
  dataPagamento: string;
  observacoes?: string;
}

export interface FiltroPagamento {
  socioId?: number;
  mes?: number;
  ano?: number;
  status?: string;
  page?: number;
  size?: number;
}
