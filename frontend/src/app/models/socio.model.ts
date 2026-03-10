import { StatusSocio } from './enums';
import { PagamentoResponse } from './pagamento.model';
import { ArquivoResponse } from './arquivo.model';

/**
 * Interface para entidade Socio
 */
export interface Socio {
  id: number;
  nome: string;
  cpf: string;
  matricula: string;
  rg?: string;
  dataNascimento?: string;
  estadoCivil?: string;
  
  // Endereço
  cep?: string;
  endereco?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  
  // Contato
  telefone?: string;
  celular?: string;
  email?: string;
  
  profissao?: string;
  status: StatusSocio;
  criadoEm: string;
  atualizadoEm: string;
}

/**
 * DTO para resposta de Socio
 */
export interface SocioResponse {
  id: number;
  nome: string;
  cpf: string;
  matricula: string;
  rg?: string;
  dataNascimento?: string;
  estadoCivil?: string;
  
  // Endereço
  cep?: string;
  endereco?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  
  // Contato
  telefone?: string;
  celular?: string;
  email?: string;
  
  profissao?: string;
  status: StatusSocio;
  criadoEm: string;
  atualizadoEm: string;
}

/**
 * DTO para requisição de criação/atualização de Socio
 */
export interface SocioRequest {
  nome: string;
  cpf: string;
  matricula: string;
  rg?: string;
  dataNascimento?: string;
  estadoCivil?: string;
  
  // Endereço
  cep?: string;
  endereco?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  
  // Contato
  telefone?: string;
  celular?: string;
  email?: string;
  
  profissao?: string;
}

/**
 * Interface para filtros de busca de Socio
 */
export interface FiltroSocio {
  termo?: string;
  status?: StatusSocio;
  page?: number;
  size?: number;
}

/**
 * Interface para campo alterado no histórico
 */
export interface CampoAlterado {
  nomeCampo: string;
  valorAnterior: string;
  valorNovo: string;
}

/**
 * Interface para histórico de alterações
 */
export interface HistoricoAlteracaoResponse {
  id: number;
  socioId: number;
  usuario: string;
  dataHora: string;
  operacao: string; // CREATE, UPDATE, DELETE
  camposAlterados: { [key: string]: CampoAlterado };
}

/**
 * DTO para resposta detalhada de Socio com relacionamentos
 */
export interface SocioDetalhadoResponse {
  id: number;
  nome: string;
  cpf: string;
  matricula: string;
  rg?: string;
  dataNascimento?: string;
  estadoCivil?: string;
  
  // Endereço
  cep?: string;
  endereco?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  
  // Contato
  telefone?: string;
  celular?: string;
  email?: string;
  
  // Status e metadados
  status: StatusSocio;
  criadoEm: string;
  atualizadoEm: string;
  
  // Relacionamentos
  pagamentos: PagamentoResponse[];
  arquivos: ArquivoResponse[];
  historico?: HistoricoAlteracaoResponse[];
}
