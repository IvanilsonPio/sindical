/**
 * Error response model matching backend ErrorResponse DTO
 */
export interface ErrorResponse {
  errorCode: string;
  message: string;
  timestamp: string;
  path?: string;
  status?: number;
  fieldErrors?: FieldError[];
  details?: Record<string, any>;
}

/**
 * Field-level validation error
 */
export interface FieldError {
  field: string;
  message: string;
  rejectedValue?: any;
}

/**
 * Standardized error codes matching backend ErrorCode enum
 */
export enum ErrorCode {
  // Authentication errors
  INVALID_CREDENTIALS = 'AUTH001',
  SESSION_EXPIRED = 'AUTH002',
  ACCESS_DENIED = 'AUTH003',
  
  // Validation errors
  REQUIRED_FIELD_MISSING = 'VAL001',
  INVALID_FORMAT = 'VAL002',
  DUPLICATE_ENTRY = 'VAL003',
  
  // Business logic errors
  SOCIO_NOT_FOUND = 'BUS001',
  PAYMENT_ALREADY_EXISTS = 'BUS002',
  FILE_TOO_LARGE = 'BUS003',
  
  // System errors
  DATABASE_ERROR = 'SYS001',
  FILE_SYSTEM_ERROR = 'SYS002',
  INTERNAL_ERROR = 'SYS003'
}

/**
 * User-friendly error messages in Portuguese
 */
export const ERROR_MESSAGES: Record<string, string> = {
  [ErrorCode.INVALID_CREDENTIALS]: 'Credenciais inválidas. Verifique seu usuário e senha.',
  [ErrorCode.SESSION_EXPIRED]: 'Sua sessão expirou. Por favor, faça login novamente.',
  [ErrorCode.ACCESS_DENIED]: 'Você não tem permissão para acessar este recurso.',
  [ErrorCode.REQUIRED_FIELD_MISSING]: 'Campos obrigatórios não foram preenchidos.',
  [ErrorCode.INVALID_FORMAT]: 'Formato de dados inválido.',
  [ErrorCode.DUPLICATE_ENTRY]: 'Este registro já existe no sistema.',
  [ErrorCode.SOCIO_NOT_FOUND]: 'Sócio não encontrado.',
  [ErrorCode.PAYMENT_ALREADY_EXISTS]: 'Já existe um pagamento para este período.',
  [ErrorCode.FILE_TOO_LARGE]: 'O arquivo excede o tamanho máximo permitido.',
  [ErrorCode.DATABASE_ERROR]: 'Erro ao acessar o banco de dados.',
  [ErrorCode.FILE_SYSTEM_ERROR]: 'Erro ao processar arquivo.',
  [ErrorCode.INTERNAL_ERROR]: 'Erro interno do servidor. Tente novamente mais tarde.'
};
