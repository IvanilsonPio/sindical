#!/bin/bash

# Script de deploy rápido para Railway
# Este script ajuda a configurar e fazer deploy no Railway

set -e

echo "🚂 Railway Deploy Helper - Sistema Sindicato Rural"
echo "=================================================="
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar se Railway CLI está instalado
if ! command -v railway &> /dev/null; then
    echo -e "${YELLOW}Railway CLI não encontrado. Instalando...${NC}"
    npm i -g @railway/cli
fi

# Menu principal
echo "Escolha uma opção:"
echo "1) Fazer login no Railway"
echo "2) Criar novo projeto"
echo "3) Conectar a projeto existente"
echo "4) Adicionar PostgreSQL"
echo "5) Configurar variáveis de ambiente"
echo "6) Deploy do backend"
echo "7) Ver logs"
echo "8) Criar usuário admin"
echo "9) Abrir dashboard do Railway"
echo "0) Sair"
echo ""
read -p "Opção: " option

case $option in
    1)
        echo -e "${GREEN}Fazendo login no Railway...${NC}"
        railway login
        ;;
    2)
        echo -e "${GREEN}Criando novo projeto...${NC}"
        railway init
        ;;
    3)
        echo -e "${GREEN}Conectando a projeto existente...${NC}"
        railway link
        ;;
    4)
        echo -e "${GREEN}Adicionando PostgreSQL...${NC}"
        echo "Execute no dashboard do Railway:"
        echo "1. Clique em '+ New'"
        echo "2. Selecione 'Database' → 'PostgreSQL'"
        echo ""
        read -p "Pressione Enter quando terminar..."
        ;;
    5)
        echo -e "${GREEN}Configurando variáveis de ambiente...${NC}"
        echo ""
        echo "Gerando JWT_SECRET..."
        JWT_SECRET=$(openssl rand -base64 32)
        echo -e "${YELLOW}JWT_SECRET gerado: ${JWT_SECRET}${NC}"
        echo ""
        echo "Adicione estas variáveis no Railway:"
        echo ""
        echo "SPRING_PROFILES_ACTIVE=prod"
        echo "DB_HOST=\${{Postgres.PGHOST}}"
        echo "DB_PORT=\${{Postgres.PGPORT}}"
        echo "DB_NAME=\${{Postgres.PGDATABASE}}"
        echo "DB_USERNAME=\${{Postgres.PGUSER}}"
        echo "DB_PASSWORD=\${{Postgres.PGPASSWORD}}"
        echo "JWT_SECRET=${JWT_SECRET}"
        echo "FILE_UPLOAD_DIR=/app/uploads"
        echo "PORT=8080"
        echo ""
        echo "Para frontend em domínio diferente, adicione:"
        echo "CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app"
        echo ""
        read -p "Pressione Enter quando terminar..."
        ;;
    6)
        echo -e "${GREEN}Fazendo deploy...${NC}"
        
        # Verificar se há mudanças não commitadas
        if [[ -n $(git status -s) ]]; then
            echo -e "${YELLOW}Há mudanças não commitadas. Commitando...${NC}"
            git add .
            read -p "Mensagem do commit: " commit_msg
            git commit -m "$commit_msg"
        fi
        
        # Push para o repositório
        echo "Fazendo push para o repositório..."
        git push
        
        echo -e "${GREEN}Deploy iniciado! O Railway fará o build automaticamente.${NC}"
        echo "Acompanhe o progresso no dashboard ou com: railway logs"
        ;;
    7)
        echo -e "${GREEN}Mostrando logs...${NC}"
        railway logs
        ;;
    8)
        echo -e "${GREEN}Criando usuário admin...${NC}"
        echo ""
        echo "Opção 1: Via script (recomendado)"
        echo "railway run bash create-admin.sh"
        echo ""
        echo "Opção 2: Via shell do Railway"
        echo "1. Abra o dashboard"
        echo "2. Vá em Deployments → View Logs → Shell"
        echo "3. Execute: bash create-admin.sh"
        echo ""
        read -p "Executar opção 1 agora? (s/n): " run_admin
        if [[ $run_admin == "s" || $run_admin == "S" ]]; then
            railway run bash create-admin.sh
        fi
        ;;
    9)
        echo -e "${GREEN}Abrindo dashboard...${NC}"
        railway open
        ;;
    0)
        echo "Saindo..."
        exit 0
        ;;
    *)
        echo -e "${RED}Opção inválida${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}✅ Operação concluída!${NC}"
echo ""
echo "Recursos úteis:"
echo "- Dashboard: railway open"
echo "- Logs: railway logs"
echo "- Variáveis: railway variables"
echo "- Shell: railway shell"
echo ""
