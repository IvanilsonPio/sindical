import { StatusUsuario } from './enums';

/**
 * DTO para requisição de login
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * DTO para resposta de autenticação
 */
export interface AuthResponse {
  token: string;
  refreshToken: string;
  username: string;
  nome: string;
  success: boolean;
}

/**
 * Interface para entidade Usuario
 */
export interface Usuario {
  id: number;
  username: string;
  nome: string;
  status: StatusUsuario;
  criadoEm: string;
  atualizadoEm: string;
}
