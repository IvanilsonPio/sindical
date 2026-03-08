# Como Corrigir o Erro do Banco de Dados

## Problema
A migração V4 do Flyway falhou porque estava tentando criar um índice em uma coluna inexistente (`data_alteracao` em vez de `data_operacao`).

## Solução

### Opção 1: Resetar o banco de dados (Recomendado para desenvolvimento)

```bash
# 1. Parar a aplicação se estiver rodando (Ctrl+C)

# 2. Resetar o banco de dados
./reset-db.sh

# 3. Iniciar a aplicação novamente
./mvnw spring-boot:run
```

### Opção 2: Resetar manualmente via psql

```bash
# 1. Conectar ao banco
psql -h localhost -U postgres -d sindicato_rural_dev

# 2. Executar no psql:
DROP TABLE IF EXISTS arquivos CASCADE;
DROP TABLE IF EXISTS pagamentos CASCADE;
DROP TABLE IF EXISTS socio_history CASCADE;
DROP TABLE IF EXISTS socios CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP FUNCTION IF EXISTS update_socio_timestamp() CASCADE;

# 3. Sair do psql
\q

# 4. Iniciar a aplicação
./mvnw spring-boot:run
```

### Opção 3: Usar Docker Compose (se estiver usando Docker)

```bash
# 1. Parar todos os containers
docker-compose down

# 2. Remover o volume do banco de dados
docker volume rm sindical_postgres_data

# 3. Iniciar novamente
docker-compose up
```

## O que foi corrigido

O arquivo `src/main/resources/db/migration/V4__add_performance_indexes.sql` foi corrigido para:
- Remover a tentativa de criar índice em coluna inexistente `data_alteracao`
- Os índices da tabela `socio_history` já foram criados na migração V2
- Comentários foram adicionados para evitar duplicação

## Próximos passos

Após resetar o banco e iniciar a aplicação:
1. A aplicação criará todas as tabelas automaticamente via Flyway
2. Você precisará criar um usuário admin inicial
3. O sistema estará pronto para uso

## Criar usuário admin

```bash
# Após a aplicação iniciar com sucesso
java GeneratePassword.java
```

Ou use o script de inicialização:
```bash
./start.sh
```
