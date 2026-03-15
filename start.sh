#!/bin/bash

# Sistema Sindicato Rural - Quick Start Script

set -e

echo "=========================================="
echo "Sistema Sindicato Rural - Inicialização"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não está instalado. Por favor, instale o Docker primeiro."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro."
    exit 1
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  Arquivo .env não encontrado. Criando a partir de .env.example..."
    cp .env.example .env
    echo "✅ Arquivo .env criado. Por favor, revise as configurações antes de continuar."
    echo ""
    echo "IMPORTANTE: Altere o JWT_SECRET no arquivo .env para produção!"
    echo ""
    read -p "Pressione Enter para continuar ou Ctrl+C para cancelar..."
fi

# Ask which environment to start
echo "Escolha o ambiente:"
echo "1) Desenvolvimento (apenas PostgreSQL)"
echo "2) Produção (todos os serviços)"
read -p "Digite sua escolha (1 ou 2): " choice

case $choice in
    1)
        echo ""
        echo "🚀 Iniciando ambiente de desenvolvimento..."
        sudo docker compose -f docker-compose.dev.yml up -d
        echo ""
        echo "✅ PostgreSQL iniciado!"
        echo ""
        echo "Para iniciar o backend:"
        echo "  mvn spring-boot:run"
        echo ""
        echo "Para iniciar o frontend:"
        echo "  cd frontend && npm install && npm start"
        echo ""
        echo "Acesso:"
        echo "  Frontend: http://localhost:4200"
        echo "  Backend: http://localhost:8080"
        echo "  PostgreSQL: localhost:5432"
        ;;
    2)
        echo ""
        echo "🚀 Iniciando ambiente de produção..."
        docker-compose up -d --build
        echo ""
        echo "⏳ Aguardando serviços iniciarem..."
        sleep 10
        echo ""
        echo "✅ Aplicação iniciada!"
        echo ""
        echo "Acesso:"
        echo "  Frontend: http://localhost"
        echo "  Backend: http://localhost:8080"
        echo ""
        echo "Para ver os logs:"
        echo "  docker-compose logs -f"
        echo ""
        echo "Para parar os serviços:"
        echo "  docker-compose down"
        ;;
    *)
        echo "❌ Opção inválida"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "Inicialização concluída!"
echo "=========================================="
