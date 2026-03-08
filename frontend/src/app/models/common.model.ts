/**
 * Interfaces comuns utilizadas em todo o sistema
 */

/**
 * Interface para resposta paginada do backend
 */
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Interface para mensagem de erro
 */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

/**
 * Interface para resposta de sucesso genérica
 */
export interface SuccessResponse {
  message: string;
  success: boolean;
}
