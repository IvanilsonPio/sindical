-- Add caminho_recibo column to pagamentos table for permanent receipt storage
-- Requirement 4.4: Permanent storage of receipts

ALTER TABLE pagamentos 
ADD COLUMN caminho_recibo VARCHAR(500);

COMMENT ON COLUMN pagamentos.caminho_recibo IS 'Relative path to the stored receipt PDF file';
