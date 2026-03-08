# 🚀 Quick Start - Deploy no Railway

Guia rápido para colocar o sistema no ar em 15 minutos.

## ⚡ Passo a Passo Rápido

### 1. Criar conta no Railway (2 min)

```bash
# Acesse e crie conta gratuita
https://railway.app

# Conecte com GitHub
```

### 2. Preparar repositório (3 min)

```bash
# Se ainda não tem Git configurado
git init
git add .
git commit -m "Initial commit"

# Criar repositório no GitHub e fazer push
git remote add origin https://github.com/SEU_USUARIO/sindicato-rural.git
git push -u origin main
```

### 3. Criar projeto no Railway (2 min)

1. No Railway: "New Project"
2. "Deploy from GitHub repo"
3. Selecione seu repositório
4. Aguarde o build (pode falhar, é normal - falta o banco)

### 4. Adicionar PostgreSQL (1 min)

1. No projeto: "+ New"
2. "Database" → "PostgreSQL"
3. Pronto! Credenciais criadas automaticamente

### 5. Configurar variáveis (3 min)

No serviço do backend, vá em "Variables" e cole:

```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
FILE_UPLOAD_DIR=/app/uploads
PORT=8080
```

Gere e adicione JWT_SECRET:

```bash
# No terminal local, gere a chave
openssl rand -base64 32

# Adicione no Railway
JWT_SECRET=cole-a-chave-gerada-aqui
```

### 6. Adicionar Volume (2 min)

1. No serviço backend: "Settings" → "Volumes"
2. "+ Add Volume"
3. Mount Path: `/app/uploads`
4. Size: 1GB

### 7. Fazer Redeploy (1 min)

1. "Deployments" → último deployment
2. "⋮" → "Redeploy"
3. Aguarde o build completar

### 8. Criar usuário admin (1 min)

```bash
# Instale Railway CLI
npm i -g @railway/cli

# Login
railway login

# Conecte ao projeto
railway link

# Execute o script
railway run bash create-admin.sh
```

Ou use o script helper:

```bash
./deploy-railway.sh
# Escolha opção 8
```

### 9. Testar (1 min)

```bash
# Pegue a URL do Railway (ex: https://seu-app.up.railway.app)

# Teste health check
curl https://seu-app.up.railway.app/actuator/health

# Teste login
curl -X POST https://seu-app.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## ✅ Pronto!

Backend está no ar! Agora escolha como fazer deploy do frontend:

### Opção A: Frontend no Vercel (Grátis, Recomendado)

```bash
cd frontend

# Atualize vercel.json com a URL do backend
# Substitua "seu-backend.up.railway.app" pela URL real

# Deploy
npx vercel

# Produção
npx vercel --prod
```

### Opção B: Frontend no Railway (Pago)

1. No projeto Railway: "+ New"
2. "GitHub Repo" → mesmo repositório
3. "Settings" → Root Directory: `frontend`
4. Aguarde deploy

### Opção C: Frontend no Netlify (Grátis)

```bash
cd frontend

# Atualize netlify.toml com a URL do backend

# Deploy
npx netlify-cli deploy

# Produção
npx netlify-cli deploy --prod
```

## 🔧 Configurar CORS

Adicione no Railway (variáveis do backend):

```bash
# Se frontend estiver em domínio diferente
CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app,https://seu-frontend.netlify.app
```

## 💰 Custos

- Backend Railway: $5/mês
- PostgreSQL: incluído
- Frontend Vercel/Netlify: grátis
- **Total: $5/mês**

## 🐛 Problemas Comuns

### Build falha
```bash
# Verifique os logs no Railway
# Geralmente falta memória ou variáveis

# Solução: Configure as variáveis corretamente
```

### Não conecta no banco
```bash
# Certifique-se que usou ${{Postgres.PGHOST}} e não valores fixos
# Verifique se o PostgreSQL está rodando
```

### Uploads não funcionam
```bash
# Certifique-se que adicionou o Volume
# Mount path deve ser /app/uploads
```

## 📚 Próximos Passos

- [ ] Configurar domínio customizado
- [ ] Configurar backups automáticos
- [ ] Adicionar monitoramento
- [ ] Configurar CI/CD avançado

## 🆘 Ajuda

- Documentação completa: `RAILWAY_DEPLOY.md`
- Script helper: `./deploy-railway.sh`
- Suporte Railway: https://railway.app/discord

---

**Tempo total: ~15 minutos** ⚡
