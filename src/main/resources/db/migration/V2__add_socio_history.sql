-- Sistema de Gerenciamento do Sindicato Rural
-- Migração para adicionar tabela de histórico de alterações de sócios
-- Versão: 2.0
-- Data: 2024

-- ============================================
-- Tabela de histórico de alterações de sócios
-- ============================================
CREATE TABLE socio_history (
    id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL REFERENCES socios(id) ON DELETE RESTRICT,
    tipo_operacao VARCHAR(20) NOT NULL,
    dados_anteriores TEXT,
    dados_novos TEXT,
    usuario VARCHAR(100),
    data_operacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Índices para otimização de performance
-- ============================================

-- Índice para busca rápida de histórico por sócio
CREATE INDEX idx_socio_history_socio_id ON socio_history(socio_id);

-- Índice para ordenação por data de operação
CREATE INDEX idx_socio_history_data_operacao ON socio_history(data_operacao DESC);

-- Índice para busca por tipo de operação
CREATE INDEX idx_socio_history_tipo_operacao ON socio_history(tipo_operacao);

-- ============================================
-- Comentários na tabela e colunas
-- ============================================

COMMENT ON TABLE socio_history IS 'Registro de auditoria de todas as alterações realizadas em fichas de sócios';
COMMENT ON COLUMN socio_history.socio_id IS 'ID do sócio que foi alterado';
COMMENT ON COLUMN socio_history.tipo_operacao IS 'Tipo de operação: CREACAO, ATUALIZACAO, EXCLUSAO';
COMMENT ON COLUMN socio_history.dados_anteriores IS 'Dados do sócio antes da alteração (formato JSON)';
COMMENT ON COLUMN socio_history.dados_novos IS 'Dados do sócio após a alteração (formato JSON)';
COMMENT ON COLUMN socio_history.usuario IS 'Nome do usuário que realizou a alteração';
COMMENT ON COLUMN socio_history.data_operacao IS 'Data e hora da operação';

-- ============================================
-- Gatilho para atualização automática de atualizado_em
-- ============================================
CREATE OR REPLACE FUNCTION update_socio_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_socio_timestamp
    BEFORE UPDATE ON socios
    FOR EACH ROW
    EXECUTE FUNCTION update_socio_timestamp();