export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  nome: string;
}

export interface Usuario {
  id: number;
  username: string;
  nome: string;
  status: string;
}
