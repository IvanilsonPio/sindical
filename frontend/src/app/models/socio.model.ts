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
  status: string;
  criadoEm: string;
  atualizadoEm: string;
}

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

export interface FiltroSocio {
  termo?: string;
  status?: string;
  page?: number;
  size?: number;
}
