-- Criação da tabela de pastas
CREATE TABLE pastas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(500),
    pasta_pai_id BIGINT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pasta_pai FOREIGN KEY (pasta_pai_id) REFERENCES pastas(id) ON DELETE CASCADE
);

-- Criação da tabela de arquivos gerais
CREATE TABLE arquivos_gerais (
    id BIGSERIAL PRIMARY KEY,
    pasta_id BIGINT,
    nome_original VARCHAR(255) NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_conteudo VARCHAR(100) NOT NULL,
    tamanho BIGINT NOT NULL,
    caminho_arquivo VARCHAR(500) NOT NULL,
    descricao VARCHAR(500),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_arquivo_pasta FOREIGN KEY (pasta_id) REFERENCES pastas(id) ON DELETE CASCADE
);

-- Índices para melhorar performance
CREATE INDEX idx_pastas_pasta_pai ON pastas(pasta_pai_id);
CREATE INDEX idx_pastas_nome ON pastas(nome);
CREATE INDEX idx_arquivos_gerais_pasta ON arquivos_gerais(pasta_id);
CREATE INDEX idx_arquivos_gerais_nome ON arquivos_gerais(nome_original);

-- Comentários nas tabelas
COMMENT ON TABLE pastas IS 'Armazena pastas para organização de arquivos gerais';
COMMENT ON TABLE arquivos_gerais IS 'Armazena arquivos gerais não vinculados a sócios específicos';
