# Deploy no Railway - Sistema Sindicato Rural

Este guia mostra como fazer deploy da aplicação no Railway.

## 📋 Pré-requisitos

1. Conta no Railway (gratuita): https://railway.app
2. GitHub account (para conectar o repositório)
3. Git instalado localmente

## 🚀 Passo a Passo

### 1. Preparar o Repositório

```bash
# Se ainda não tem um repositório Git, inicialize
git init
git add .
git commit -m "Preparar para deploy no Railway"

# Crie um repositório no GitHub e faça push
git remote add origin https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
git branch -M main
git push -u origin main
```

### 2. Criar Projeto no Railway

1. Acesse https://railway.app e faça login
2. Clique em "New Project"
3. Selecione "Deploy from GitHub repo"
4. Autorize o Railway a acessar seus repositórios
5. Selecione o repositório do sistema

### 3. Adicionar PostgreSQL

1. No seu projeto Railway, clique em "+ New"
2. Selecione "Database" → "PostgreSQL"
3. O Railway criará automaticamente um banco PostgreSQL
4. Anote as credenciais (ou use as variáveis de referência)

### 4. Configurar Variáveis de Ambiente

No painel do Railway, vá em "Variables" e adicione:

#### Backend Service (obrigatórias):

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database (use as variáveis de referência do PostgreSQL)
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

# JWT Secret (IMPORTANTE: gere uma chave segura!)
JWT_SECRET=sua-chave-super-secreta-aqui-minimo-32-caracteres

# File Upload (Railway tem volume persistente)
FILE_UPLOAD_DIR=/app/uploads

# Port (Railway define automaticamente, mas pode especificar)
PORT=8080
```

#### Como gerar JWT_SECRET seguro:

```bash
# Linux/Mac
openssl rand -base64 32

# Ou use este comando
echo "$(date +%s | sha256sum | base64 | head -c 32)"
```

### 5. Configurar Volume Persistente (para uploads)

1. No serviço do backend, vá em "Settings"
2. Clique em "Volumes"
3. Adicione um volume:
   - Mount Path: `/app/uploads`
   - Size: 1GB (ou mais, conforme necessário)

### 6. Deploy Automático

O Railway detectará automaticamente:
- `Dockerfile` para build
- `railway.json` para configurações
- Fará deploy automaticamente a cada push no GitHub

### 7. Criar Usuário Admin

Após o primeiro deploy, você precisa criar o usuário admin:

#### Opção 1: Via Railway CLI

```bash
# Instale o Railway CLI
npm i -g @railway/cli

# Faça login
railway login

# Conecte ao projeto
railway link

# Execute o script
railway run bash create-admin.sh
```

#### Opção 2: Via Console do Railway

1. No painel do Railway, vá no serviço do backend
2. Clique em "Deployments" → selecione o deployment ativo
3. Clique em "View Logs"
4. Clique em "Shell" (terminal)
5. Execute:

```bash
bash create-admin.sh
```

#### Opção 3: Conectar ao PostgreSQL diretamente

```bash
# Use as credenciais do Railway para conectar
psql -h PGHOST -U PGUSER -d PGDATABASE

# Execute o SQL manualmente
INSERT INTO usuarios (username, password, role, ativo, data_criacao)
VALUES ('admin', 'SENHA_BCRYPT_AQUI', 'ADMIN', true, NOW());
```

Para gerar a senha BCrypt:

```bash
# Use o script standalone
javac GeneratePasswordStandalone.java
java GeneratePasswordStandalone
```

### 8. Verificar Deploy

1. Railway fornecerá uma URL pública (ex: `https://seu-app.up.railway.app`)
2. Teste o health check: `https://seu-app.up.railway.app/actuator/health`
3. Teste o login: `https://seu-app.up.railway.app/api/auth/login`

## 🔧 Configurações Adicionais

### Custom Domain (Opcional)

1. No Railway, vá em "Settings" → "Domains"
2. Clique em "Add Domain"
3. Configure seu DNS conforme instruções

### Monitoramento

O Railway fornece:
- Logs em tempo real
- Métricas de CPU/RAM
- Histórico de deploys
- Alertas de erro

Acesse em: Projeto → Serviço → "Observability"

### Backups do Banco

1. No serviço PostgreSQL, vá em "Data"
2. Clique em "Backups"
3. Configure backups automáticos

Ou use o script manual:

```bash
# Conecte via Railway CLI
railway connect postgres

# Faça backup
pg_dump -h PGHOST -U PGUSER PGDATABASE > backup.sql
```

## 💰 Custos Estimados

### Plano Hobby (Recomendado para produção pequena)
- $5/mês base
- Inclui:
  - 500 horas de execução
  - 100GB de tráfego
  - 1GB de RAM
  - PostgreSQL incluído
  - Volumes persistentes

### Plano Developer (Gratuito - para testes)
- $0/mês
- Inclui:
  - 500 horas de execução
  - 100GB de tráfego
  - Limitações: serviços dormem após inatividade

## 🐛 Troubleshooting

### Build falha

```bash
# Verifique os logs no Railway
# Geralmente é falta de memória ou dependências

# Solução: Aumente a RAM no plano ou otimize o build
```

### Aplicação não inicia

```bash
# Verifique as variáveis de ambiente
# Certifique-se que DB_HOST, DB_PORT, etc. estão corretas

# Verifique os logs:
railway logs
```

### Erro de conexão com banco

```bash
# Verifique se o PostgreSQL está rodando
# Verifique as credenciais nas variáveis de ambiente
# Certifique-se que usou as variáveis de referência: ${{Postgres.PGHOST}}
```

### Uploads não persistem

```bash
# Certifique-se que configurou o Volume
# Verifique se FILE_UPLOAD_DIR aponta para /app/uploads
# Verifique permissões no Dockerfile
```

## 📚 Recursos

- Documentação Railway: https://docs.railway.app
- Railway CLI: https://docs.railway.app/develop/cli
- Suporte: https://railway.app/discord

## 🔄 CI/CD Automático

O Railway já configura CI/CD automaticamente:
- Push no GitHub → Build automático → Deploy automático
- Rollback fácil: basta selecionar um deployment anterior
- Preview deployments: crie branches para testar

## 🎯 Próximos Passos

1. ✅ Deploy do backend no Railway
2. ⏭️ Deploy do frontend (Angular)
3. ⏭️ Configurar domínio customizado
4. ⏭️ Configurar SSL (automático no Railway)
5. ⏭️ Configurar backups automáticos

---

**Dica**: O Railway é muito mais simples que AWS/GCP para começar. Todo o processo acima leva ~15 minutos!
