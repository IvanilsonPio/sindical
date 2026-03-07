-- Sistema de Gerenciamento do Sindicato Rural
-- Migração inicial do banco de dados
-- Versão: 1.0
-- Data: 2024

-- ============================================
-- Tabela de usuários administrativos
-- ============================================
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Tabela de sócios (trabalhadores rurais)
-- ============================================
CREATE TABLE socios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    rg VARCHAR(20),
    data_nascimento DATE,
    telefone VARCHAR(20),
    email VARCHAR(100),
    endereco TEXT,
    cidade VARCHAR(50),
    estado VARCHAR(2),
    cep VARCHAR(10),
    profissao VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Tabela de pagamentos mensais
-- ============================================
CREATE TABLE pagamentos (
    id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL REFERENCES socios(id),
    valor DECIMAL(10,2) NOT NULL,
    mes INTEGER NOT NULL CHECK (mes BETWEEN 1 AND 12),
    ano INTEGER NOT NULL CHECK (ano >= 2020),
    data_pagamento DATE NOT NULL,
    numero_recibo VARCHAR(20) UNIQUE NOT NULL,
    observacoes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PAGO',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(socio_id, mes, ano)
);

-- ============================================
-- Tabela de arquivos associados aos sócios
-- ============================================
CREATE TABLE arquivos (
    id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL REFERENCES socios(id),
    nome_original VARCHAR(255) NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_conteudo VARCHAR(100) NOT NULL,
    tamanho BIGINT NOT NULL,
    caminho_arquivo VARCHAR(500) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Índices para otimização de performance
-- ============================================

-- Índices para busca rápida de sócios por CPF
CREATE INDEX idx_socios_cpf ON socios(cpf);

-- Índices para busca rápida de sócios por matrícula
CREATE INDEX idx_socios_matricula ON socios(matricula);

-- Índices para consultas de pagamentos por sócio e período
CREATE INDEX idx_pagamentos_socio_periodo ON pagamentos(socio_id, ano, mes);

-- Índices para listagem de arquivos por sócio
CREATE INDEX idx_arquivos_socio ON arquivos(socio_id);

-- ============================================
-- Comentários nas tabelas e colunas
-- ============================================

COMMENT ON TABLE usuarios IS 'Usuários administrativos do sistema com credenciais de acesso';
COMMENT ON TABLE socios IS 'Trabalhadores rurais associados ao sindicato';
COMMENT ON TABLE pagamentos IS 'Registro de pagamentos mensais dos sócios';
COMMENT ON TABLE arquivos IS 'Documentos digitais associados aos sócios';

COMMENT ON COLUMN usuarios.username IS 'Nome de usuário único para login';
COMMENT ON COLUMN usuarios.password IS 'Senha criptografada com BCrypt';
COMMENT ON COLUMN usuarios.status IS 'Status do usuário: ATIVO, INATIVO, BLOQUEADO';

COMMENT ON COLUMN socios.cpf IS 'CPF único do sócio (formato: 000.000.000-00)';
COMMENT ON COLUMN socios.matricula IS 'Número de matrícula único do sócio no sindicato';
COMMENT ON COLUMN socios.status IS 'Status do sócio: ATIVO, INATIVO, SUSPENSO';

COMMENT ON COLUMN pagamentos.numero_recibo IS 'Número único do recibo gerado automaticamente';
COMMENT ON COLUMN pagamentos.mes IS 'Mês do pagamento (1-12)';
COMMENT ON COLUMN pagamentos.ano IS 'Ano do pagamento (>= 2020)';
COMMENT ON COLUMN pagamentos.status IS 'Status do pagamento: PAGO, CANCELADO, ESTORNADO';

COMMENT ON COLUMN arquivos.nome_original IS 'Nome original do arquivo enviado pelo usuário';
COMMENT ON COLUMN arquivos.nome_arquivo IS 'Nome único do arquivo no sistema de armazenamento';
COMMENT ON COLUMN arquivos.caminho_arquivo IS 'Caminho completo do arquivo no sistema de arquivos';
