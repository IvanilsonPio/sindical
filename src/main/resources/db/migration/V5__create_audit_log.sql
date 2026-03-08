-- Tabela de auditoria para rastreamento de operações
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entidade VARCHAR(50) NOT NULL,
    entidade_id BIGINT NOT NULL,
    operacao VARCHAR(20) NOT NULL,
    usuario VARCHAR(100),
    dados_anteriores TEXT,
    dados_novos TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance de consultas de auditoria
CREATE INDEX idx_audit_log_entidade ON audit_log(entidade);
CREATE INDEX idx_audit_log_entidade_id ON audit_log(entidade_id);
CREATE INDEX idx_audit_log_operacao ON audit_log(operacao);
CREATE INDEX idx_audit_log_usuario ON audit_log(usuario);
CREATE INDEX idx_audit_log_criado_em ON audit_log(criado_em DESC);
CREATE INDEX idx_audit_log_entidade_entidade_id ON audit_log(entidade, entidade_id);

-- Comentários para documentação
COMMENT ON TABLE audit_log IS 'Registro de auditoria de todas as operações CRUD no sistema';
COMMENT ON COLUMN audit_log.entidade IS 'Nome da entidade afetada (Usuario, Socio, Pagamento, Arquivo)';
COMMENT ON COLUMN audit_log.entidade_id IS 'ID do registro afetado';
COMMENT ON COLUMN audit_log.operacao IS 'Tipo de operação (CREATE, UPDATE, DELETE)';
COMMENT ON COLUMN audit_log.usuario IS 'Usuário que executou a operação';
COMMENT ON COLUMN audit_log.dados_anteriores IS 'Estado anterior do registro (JSON)';
COMMENT ON COLUMN audit_log.dados_novos IS 'Novo estado do registro (JSON)';
