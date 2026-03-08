# Guia de Deployment - Sistema Sindicato Rural

Este documento descreve como fazer o build e deployment da aplicação usando Docker e Docker Compose.

## Pré-requisitos

- Docker 20.10+
- Docker Compose 2.0+
- (Opcional) Maven 3.9+ e Node.js 20+ para desenvolvimento local

## Arquitetura de Deployment

A aplicação é composta por três serviços:

1. **PostgreSQL** - Banco de dados
2. **Backend** - API Spring Boot (porta 8080)
3. **Frontend** - Angular servido via Nginx (porta 80)

## Ambientes

### Desenvolvimento Local

Para desenvolvimento, use o `docker-compose.dev.yml` que inicia apenas o PostgreSQL:

```bash
# Iniciar PostgreSQL para desenvolvimento
docker-compose -f docker-compose.dev.yml up -d

# Backend (em outro terminal)
cd /caminho/do/projeto
mvn spring-boot:run

# Frontend (em outro terminal)
cd frontend
npm install
npm start
```

Acesse:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- PostgreSQL: localhost:5432

### Produção (Docker Compose)

Para produção, use o `docker-compose.yml` que inicia todos os serviços:

```bash
# Build e iniciar todos os serviços
docker-compose up -d --build

# Verificar status dos serviços
docker-compose ps

# Ver logs
docker-compose logs -f

# Parar todos os serviços
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados)
docker-compose down -v
```

Acesse:
- Frontend: http://localhost
- Backend API: http://localhost:8080/api
- PostgreSQL: localhost:5432

## Configuração de Produção

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto para configurações de produção:

```env
# JWT Secret (OBRIGATÓRIO em produção)
JWT_SECRET=sua-chave-secreta-super-segura-aqui-com-pelo-menos-256-bits

# Database (opcional, usa valores padrão se não definido)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=sindicato_rural
DB_USERNAME=sindicato_user
DB_PASSWORD=sindicato_pass

# File Upload Directory
FILE_UPLOAD_DIR=/app/uploads
```

### Build Manual das Imagens

#### Backend

```bash
# Build da imagem do backend
docker build -t sindicato-backend:latest .

# Executar container do backend
docker run -d \
  --name sindicato-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=postgres \
  -e DB_USERNAME=sindicato_user \
  -e DB_PASSWORD=sindicato_pass \
  -e JWT_SECRET=sua-chave-secreta \
  sindicato-backend:latest
```

#### Frontend

```bash
# Build da imagem do frontend
cd frontend
docker build -t sindicato-frontend:latest .

# Executar container do frontend
docker run -d \
  --name sindicato-frontend \
  -p 80:80 \
  sindicato-frontend:latest
```

## Build de Produção

### Angular (Frontend)

```bash
cd frontend
npm install
npm run build -- --configuration production
```

Os arquivos de produção estarão em `frontend/dist/frontend/browser/`

### Spring Boot (Backend)

```bash
mvn clean package -DskipTests
```

O JAR de produção estará em `target/sistema-sindicato-rural-0.0.1-SNAPSHOT.jar`

## Otimizações de Produção

### Angular

- **AOT Compilation**: Habilitado
- **Build Optimizer**: Habilitado
- **Minificação**: CSS e JS minificados
- **Tree Shaking**: Remove código não utilizado
- **Lazy Loading**: Módulos carregados sob demanda
- **Gzip**: Compressão habilitada no Nginx

### Spring Boot

- **Multi-stage Build**: Reduz tamanho da imagem
- **JRE Alpine**: Imagem base mínima (~150MB)
- **Container Support**: JVM otimizada para containers
- **Health Checks**: Monitoramento de saúde dos serviços
- **Connection Pool**: HikariCP otimizado para produção

### PostgreSQL

- **Persistent Volumes**: Dados persistidos em volumes Docker
- **Health Checks**: Verificação de disponibilidade
- **Connection Limits**: Configurado para produção

## Monitoramento

### Health Checks

Todos os serviços possuem health checks configurados:

- **Backend**: http://localhost:8080/actuator/health
- **Frontend**: http://localhost (Nginx)
- **PostgreSQL**: `pg_isready` command

### Logs

```bash
# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Logs do backend são salvos em volume
docker-compose exec backend ls -la /app/logs
```

## Backup e Restore

### Backup do PostgreSQL

```bash
# Backup do banco de dados
docker-compose exec postgres pg_dump -U sindicato_user sindicato_rural > backup.sql

# Ou usando docker exec
docker exec sindicato-postgres pg_dump -U sindicato_user sindicato_rural > backup.sql
```

### Restore do PostgreSQL

```bash
# Restore do banco de dados
docker-compose exec -T postgres psql -U sindicato_user sindicato_rural < backup.sql

# Ou usando docker exec
docker exec -i sindicato-postgres psql -U sindicato_user sindicato_rural < backup.sql
```

### Backup de Arquivos Uploaded

```bash
# Backup do volume de uploads
docker run --rm \
  -v sindicato_backend_uploads:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/uploads-backup.tar.gz -C /data .
```

## Troubleshooting

### Backend não conecta ao PostgreSQL

```bash
# Verificar se o PostgreSQL está rodando
docker-compose ps postgres

# Verificar logs do PostgreSQL
docker-compose logs postgres

# Verificar conectividade
docker-compose exec backend ping postgres
```

### Frontend não consegue acessar o Backend

- Verifique se o backend está rodando: `docker-compose ps backend`
- Verifique a configuração do proxy no `nginx.conf`
- Verifique os logs: `docker-compose logs frontend`

### Erro de permissão em volumes

```bash
# Ajustar permissões dos volumes
docker-compose down
sudo chown -R 1000:1000 ./uploads ./logs
docker-compose up -d
```

### Limpar tudo e recomeçar

```bash
# CUIDADO: Remove todos os containers, volumes e imagens
docker-compose down -v
docker system prune -a
docker-compose up -d --build
```

## Segurança

### Checklist de Segurança para Produção

- [ ] Alterar `JWT_SECRET` para valor seguro e único
- [ ] Alterar senha do PostgreSQL (`POSTGRES_PASSWORD`)
- [ ] Configurar HTTPS/SSL (usar reverse proxy como Traefik ou Nginx)
- [ ] Configurar firewall para limitar acesso às portas
- [ ] Habilitar backups automáticos
- [ ] Configurar logs centralizados
- [ ] Implementar rate limiting
- [ ] Revisar permissões de volumes e arquivos
- [ ] Manter imagens Docker atualizadas
- [ ] Configurar monitoramento e alertas

## Performance

### Recursos Recomendados

**Mínimo:**
- CPU: 2 cores
- RAM: 4GB
- Disco: 20GB SSD

**Recomendado:**
- CPU: 4 cores
- RAM: 8GB
- Disco: 50GB SSD

### Tuning do PostgreSQL

Edite `docker-compose.yml` para adicionar configurações:

```yaml
postgres:
  command:
    - "postgres"
    - "-c"
    - "max_connections=100"
    - "-c"
    - "shared_buffers=256MB"
    - "-c"
    - "effective_cache_size=1GB"
```

## Atualizações

### Atualizar a Aplicação

```bash
# Pull das últimas alterações
git pull

# Rebuild e restart dos serviços
docker-compose up -d --build

# Verificar se tudo está funcionando
docker-compose ps
docker-compose logs -f
```

### Rollback

```bash
# Voltar para versão anterior
git checkout <commit-anterior>
docker-compose up -d --build
```

## Suporte

Para problemas ou dúvidas:
1. Verifique os logs: `docker-compose logs -f`
2. Verifique o status: `docker-compose ps`
3. Consulte a documentação do projeto
