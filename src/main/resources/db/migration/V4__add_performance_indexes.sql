-- Migration V4: Add performance optimization indexes
-- This migration adds additional indexes to improve query performance
-- for frequently accessed columns and search operations

-- Socios table indexes
-- Index for nome searches (case-insensitive)
CREATE INDEX IF NOT EXISTS idx_socios_nome_lower ON socios(LOWER(nome));

-- Index for status filtering
CREATE INDEX IF NOT EXISTS idx_socios_status ON socios(status);

-- Index for cidade and estado filtering
CREATE INDEX IF NOT EXISTS idx_socios_cidade ON socios(LOWER(cidade));
CREATE INDEX IF NOT EXISTS idx_socios_estado ON socios(LOWER(estado));

-- Composite index for common filter combinations
CREATE INDEX IF NOT EXISTS idx_socios_status_nome ON socios(status, LOWER(nome));

-- Pagamentos table indexes
-- Composite index for socio_id, ano, mes (optimizes period queries)
CREATE INDEX IF NOT EXISTS idx_pagamentos_socio_ano_mes ON pagamentos(socio_id, ano DESC, mes DESC);

-- Index for status filtering
CREATE INDEX IF NOT EXISTS idx_pagamentos_status ON pagamentos(status);

-- Index for data_pagamento (for date range queries)
CREATE INDEX IF NOT EXISTS idx_pagamentos_data_pagamento ON pagamentos(data_pagamento DESC);

-- Composite index for ano and mes (for period reports)
CREATE INDEX IF NOT EXISTS idx_pagamentos_ano_mes ON pagamentos(ano DESC, mes DESC);

-- Index for numero_recibo (already unique, but explicit index for lookups)
-- Already exists as unique constraint, no need to add

-- Arquivos table indexes
-- Index for socio_id (for listing files by socio)
-- Already exists from V1, but let's ensure it's optimized
CREATE INDEX IF NOT EXISTS idx_arquivos_socio_criado ON arquivos(socio_id, criado_em DESC);

-- Index for tipo_conteudo (for filtering by file type)
CREATE INDEX IF NOT EXISTS idx_arquivos_tipo_conteudo ON arquivos(tipo_conteudo);

-- Socio History table indexes (if exists)
-- Index for socio_id already exists from V2, skip to avoid duplicate
-- CREATE INDEX IF NOT EXISTS idx_socio_history_socio_id ON socio_history(socio_id);

-- Index for data_operacao for chronological queries (already exists from V2)
-- CREATE INDEX IF NOT EXISTS idx_socio_history_data_operacao ON socio_history(data_operacao DESC);

-- Add comments for documentation
COMMENT ON INDEX idx_socios_nome_lower IS 'Optimizes case-insensitive name searches';
COMMENT ON INDEX idx_socios_status IS 'Optimizes status filtering';
COMMENT ON INDEX idx_socios_status_nome IS 'Optimizes combined status and name filtering';
COMMENT ON INDEX idx_pagamentos_socio_ano_mes IS 'Optimizes payment queries by socio and period';
COMMENT ON INDEX idx_pagamentos_status IS 'Optimizes payment status filtering';
COMMENT ON INDEX idx_pagamentos_data_pagamento IS 'Optimizes date range queries';
COMMENT ON INDEX idx_pagamentos_ano_mes IS 'Optimizes period-based reports';
COMMENT ON INDEX idx_arquivos_socio_criado IS 'Optimizes file listing by socio with chronological order';
COMMENT ON INDEX idx_arquivos_tipo_conteudo IS 'Optimizes file type filtering';
