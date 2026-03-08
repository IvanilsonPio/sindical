import { StatusSocio } from './enums';

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
  telefone?: string;
  email?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
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
  telefone?: string;
  email?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
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
  telefone?: string;
  email?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
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
