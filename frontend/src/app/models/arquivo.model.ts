/**
 * Interface para entidade Arquivo
 */
export interface Arquivo {
  id: number;
  socioId: number;
  socioNome: string;
  nomeOriginal: string;
  nomeArquivo: string;
  tipoConteudo: string;
  tamanho: number;
  tamanhoFormatado: string;
  criadoEm: string;
}

/**
 * DTO para resposta de Arquivo
 */
export interface ArquivoResponse {
  id: number;
  socioId: number;
  socioNome: string;
  nomeOriginal: string;
  nomeArquivo: string;
  tipoConteudo: string;
  tamanho: number;
  tamanhoFormatado: string;
  criadoEm: string;
}
