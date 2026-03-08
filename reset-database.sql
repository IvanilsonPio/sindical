-- Script para resetar o banco de dados
-- Execute com: psql -U postgres -d sindicato_rural_dev -f reset-database.sql

-- Remover todas as tabelas
DROP TABLE IF EXISTS arquivos CASCADE;
DROP TABLE IF EXISTS pagamentos CASCADE;
DROP TABLE IF EXISTS socio_history CASCADE;
DROP TABLE IF EXISTS socios CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Remover funções
DROP FUNCTION IF EXISTS update_socio_timestamp() CASCADE;

-- Mensagem de sucesso
SELECT 'Database reset completed successfully!' as status;
