import { StatusPagamento } from './enums';

/**
 * Interface para entidade Pagamento
 */
export interface Pagamento {
  id: number;
  socioId: number;
  socioNome: string;
  socioCpf: string;
  valor: number;
  mes: number;
  ano: number;
  dataPagamento: string;
  numeroRecibo: string;
  caminhoRecibo?: string;
  observacoes?: string;
  status: StatusPagamento;
  criadoEm: string;
  atualizadoEm: string;
}

/**
 * DTO para resposta de Pagamento
 */
export interface PagamentoResponse {
  id: number;
  socioId: number;
  socioNome: string;
  socioCpf: string;
  valor: number;
  mes: number;
  ano: number;
  dataPagamento: string;
  numeroRecibo: string;
  caminhoRecibo?: string;
  observacoes?: string;
  status: StatusPagamento;
  criadoEm: string;
  atualizadoEm: string;
}

/**
 * DTO para requisição de criação de Pagamento
 */
export interface PagamentoRequest {
  socioId: number;
  valor: number;
  mes: number;
  ano: number;
  dataPagamento: string;
  observacoes?: string;
}

/**
 * Interface para filtros de busca de Pagamento
 */
export interface FiltroPagamento {
  socioId?: number;
  mes?: number;
  ano?: number;
  status?: StatusPagamento;
  page?: number;
  size?: number;
}
