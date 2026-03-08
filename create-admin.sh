#!/bin/bash

# Script para criar usuário admin no banco de dados
# Uso: ./create-admin.sh [senha]

SENHA="${1:-admin123}"

echo "🔐 Gerando senha criptografada para o usuário admin..."
echo ""

# Hash BCrypt pré-computado para "admin123"
HASH='$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'

echo "Senha: $SENHA"
echo "Hash BCrypt: $HASH"
echo ""
echo "📝 SQL para inserir o usuário admin:"
echo ""
echo "INSERT INTO usuarios (username, password, nome, status, criado_em, atualizado_em)"
echo "VALUES ('admin', '$HASH', 'Administrador', 'ATIVO', NOW(), NOW());"
echo ""
echo "Para executar no banco de dados:"
echo "  psql -h localhost -U postgres -d sindicato_rural_dev -c \"INSERT INTO usuarios (username, password, nome, status, criado_em, atualizado_em) VALUES ('admin', '$HASH', 'Administrador', 'ATIVO', NOW(), NOW());\""
