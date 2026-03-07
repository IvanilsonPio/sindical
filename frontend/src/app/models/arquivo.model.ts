export interface Arquivo {
  id: number;
  socioId: number;
  nomeOriginal: string;
  nomeArquivo: string;
  tipoConteudo: string;
  tamanho: number;
  criadoEm: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
