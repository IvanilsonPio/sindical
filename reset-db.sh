#!/bin/bash

# Script para resetar o banco de dados do sistema
# Uso: ./reset-db.sh

echo "🔄 Resetando banco de dados..."

# Verificar se o PostgreSQL está rodando
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "❌ PostgreSQL não está rodando em localhost:5432"
    echo "   Inicie o PostgreSQL ou use Docker:"
    echo "   docker-compose up -d postgres"
    exit 1
fi

# Executar o script de reset
PGPASSWORD=postgres psql -h localhost -U postgres -d sindicato_rural_dev -f reset-database.sql

if [ $? -eq 0 ]; then
    echo "✅ Banco de dados resetado com sucesso!"
    echo ""
    echo "Agora você pode iniciar a aplicação novamente:"
    echo "  ./mvnw spring-boot:run"
else
    echo "❌ Erro ao resetar o banco de dados"
    exit 1
fi
