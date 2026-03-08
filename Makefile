.PHONY: help build up down logs clean dev-up dev-down test backend-build frontend-build

# Default target
help:
	@echo "Sistema Sindicato Rural - Comandos Disponíveis"
	@echo ""
	@echo "Desenvolvimento:"
	@echo "  make dev-up          - Inicia PostgreSQL para desenvolvimento"
	@echo "  make dev-down        - Para PostgreSQL de desenvolvimento"
	@echo "  make backend-run     - Executa backend localmente"
	@echo "  make frontend-run    - Executa frontend localmente"
	@echo ""
	@echo "Produção:"
	@echo "  make build           - Build de todas as imagens Docker"
	@echo "  make up              - Inicia todos os serviços em produção"
	@echo "  make down            - Para todos os serviços"
	@echo "  make restart         - Reinicia todos os serviços"
	@echo "  make logs            - Mostra logs de todos os serviços"
	@echo ""
	@echo "Build Manual:"
	@echo "  make backend-build   - Build do backend (JAR)"
	@echo "  make frontend-build  - Build do frontend (produção)"
	@echo ""
	@echo "Testes:"
	@echo "  make test            - Executa todos os testes"
	@echo "  make test-backend    - Executa testes do backend"
	@echo "  make test-frontend   - Executa testes do frontend"
	@echo ""
	@echo "Manutenção:"
	@echo "  make clean           - Remove containers, volumes e imagens"
	@echo "  make backup-db       - Faz backup do banco de dados"
	@echo "  make restore-db      - Restaura backup do banco de dados"

# Desenvolvimento
dev-up:
	docker-compose -f docker-compose.dev.yml up -d
	@echo "PostgreSQL iniciado em localhost:5432"

dev-down:
	docker-compose -f docker-compose.dev.yml down

backend-run:
	mvn spring-boot:run

frontend-run:
	cd frontend && npm start

# Produção
build:
	docker-compose build

up:
	docker-compose up -d
	@echo "Aplicação iniciada!"
	@echo "Frontend: http://localhost"
	@echo "Backend: http://localhost:8080"

down:
	docker-compose down

restart:
	docker-compose restart

logs:
	docker-compose logs -f

# Build Manual
backend-build:
	mvn clean package -DskipTests
	@echo "JAR criado em target/"

frontend-build:
	cd frontend && npm install && npm run build -- --configuration production
	@echo "Build criado em frontend/dist/"

# Testes
test: test-backend test-frontend

test-backend:
	mvn test

test-frontend:
	cd frontend && npm test -- --watch=false --browsers=ChromeHeadless

# Manutenção
clean:
	docker-compose down -v
	docker system prune -f
	@echo "Limpeza concluída"

backup-db:
	@mkdir -p backups
	docker-compose exec -T postgres pg_dump -U sindicato_user sindicato_rural > backups/backup-$$(date +%Y%m%d-%H%M%S).sql
	@echo "Backup criado em backups/"

restore-db:
	@read -p "Digite o nome do arquivo de backup: " backup_file; \
	docker-compose exec -T postgres psql -U sindicato_user sindicato_rural < $$backup_file
	@echo "Backup restaurado"

# Status
status:
	docker-compose ps

# Health checks
health:
	@echo "Verificando saúde dos serviços..."
	@curl -f http://localhost:8080/actuator/health || echo "Backend: ERRO"
	@curl -f http://localhost/ > /dev/null 2>&1 && echo "Frontend: OK" || echo "Frontend: ERRO"
