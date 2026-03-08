# 🚂 Deploy no Railway - Sistema Sindicato Rural

Sistema de gerenciamento para sindicato rural com deploy otimizado para Railway.

## 🎯 O que é Railway?

Railway é uma plataforma moderna de deploy que:
- ✅ Deploy automático do GitHub
- ✅ PostgreSQL incluído
- ✅ Volumes persistentes para arquivos
- ✅ HTTPS automático
- ✅ $5/mês (muito mais barato que AWS/GCP)
- ✅ Zero configuração de infraestrutura

## 📚 Documentação

### Guias Disponíveis

1. **[QUICK_START_RAILWAY.md](QUICK_START_RAILWAY.md)** ⚡
   - Deploy em 15 minutos
   - Passo a passo simplificado
   - **Comece por aqui!**

2. **[RAILWAY_DEPLOY.md](RAILWAY_DEPLOY.md)** 📖
   - Guia completo do backend
   - Configurações detalhadas
   - Troubleshooting

3. **[RAILWAY_FRONTEND_DEPLOY.md](RAILWAY_FRONTEND_DEPLOY.md)** 🎨
   - Deploy do frontend Angular
   - Opções: Railway, Vercel, Netlify
   - Configuração de CORS

4. **[DEPLOY_CHECKLIST.md](DEPLOY_CHECKLIST.md)** ✅
   - Checklist completo
   - Verificações de segurança
   - Testes finais

### Scripts Úteis

- **`deploy-railway.sh`** - Script interativo de deploy
- **`create-admin.sh`** - Criar usuário admin
- **`reset-db.sh`** - Resetar banco de dados (dev)

## 🚀 Deploy Rápido

```bash
# 1. Instalar Railway CLI
npm i -g @railway/cli

# 2. Login
railway login

# 3. Criar projeto
railway init

# 4. Usar script helper
./deploy-railway.sh
```

## 📁 Arquivos de Configuração

```
.
├── railway.json              # Configuração Railway (backend)
├── nixpacks.toml            # Build alternativo
├── Dockerfile               # Container do backend
├── docker-compose.yml       # Desenvolvimento local
├── .env.example             # Variáveis de ambiente
├── frontend/
│   ├── railway.json         # Configuração Railway (frontend)
│   ├── vercel.json          # Configuração Vercel
│   ├── netlify.toml         # Configuração Netlify
│   └── Dockerfile           # Container do frontend
└── src/
    └── main/
        ├── resources/
        │   └── application.yml  # Config Spring Boot
        └── java/
            └── com/sindicato/
                └── config/
                    └── CorsConfig.java  # CORS
```

## 🏗️ Arquitetura no Railway

```
┌─────────────────────────────────────────┐
│           Railway Project               │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────┐  ┌─────────────────┐ │
│  │  PostgreSQL  │  │  Backend (Java) │ │
│  │              │◄─┤  Spring Boot    │ │
│  │  Port: 5432  │  │  Port: 8080     │ │
│  └──────────────┘  └─────────────────┘ │
│                           │             │
│                           │             │
│                    ┌──────▼──────┐      │
│                    │   Volume    │      │
│                    │  /uploads   │      │
│                    └─────────────┘      │
└─────────────────────────────────────────┘
                      │
                      │ HTTPS
                      ▼
              ┌───────────────┐
              │   Frontend    │
              │ Vercel/Netlify│
              │   (Grátis)    │
              └───────────────┘
```

## 💰 Custos

### Opção Recomendada: Railway + Vercel
- Backend (Railway): $5/mês
- PostgreSQL: incluído
- Frontend (Vercel): grátis
- **Total: $5/mês**

### Alternativa: Tudo no Railway
- Backend + Frontend + PostgreSQL: $5-10/mês
- **Total: $5-10/mês**

## 🔧 Variáveis de Ambiente

### Backend (Railway)

```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
JWT_SECRET=<gere com: openssl rand -base64 32>
FILE_UPLOAD_DIR=/app/uploads
PORT=8080
CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app
```

### Frontend (Vercel/Netlify)

```bash
API_URL=https://seu-backend.up.railway.app/api
```

## 🧪 Testar Deploy

```bash
# Health check
curl https://seu-app.up.railway.app/actuator/health

# Login
curl -X POST https://seu-app.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 📊 Monitoramento

### Railway Dashboard
- Logs em tempo real
- Métricas de CPU/RAM
- Histórico de deploys
- Alertas automáticos

### Acessar
```bash
railway open
```

## 🔒 Segurança

- ✅ HTTPS automático
- ✅ JWT para autenticação
- ✅ CORS configurado
- ✅ Headers de segurança
- ✅ Variáveis de ambiente seguras
- ✅ Volumes isolados

## 🐛 Troubleshooting

### Build falha
```bash
# Ver logs
railway logs

# Verificar variáveis
railway variables

# Redeploy
railway up
```

### Não conecta no banco
```bash
# Verificar se PostgreSQL está rodando
railway status

# Verificar variáveis de referência
# Use ${{Postgres.PGHOST}} não valores fixos
```

### Uploads não persistem
```bash
# Verificar se Volume está configurado
# Settings → Volumes → /app/uploads
```

## 📞 Suporte

- 📖 Docs Railway: https://docs.railway.app
- 💬 Discord Railway: https://railway.app/discord
- 📧 Suporte: support@railway.app

## 🎓 Recursos Adicionais

- [Railway Templates](https://railway.app/templates)
- [Railway Blog](https://blog.railway.app)
- [Railway Status](https://status.railway.app)

## 🔄 Atualizações

Para atualizar a aplicação:

```bash
# 1. Faça as alterações no código
git add .
git commit -m "Sua mensagem"
git push

# 2. Railway faz deploy automático!
# Acompanhe: railway logs
```

## 📝 Notas

- Railway faz deploy automático a cada push no GitHub
- Rollback é fácil: selecione deployment anterior no dashboard
- Backups do PostgreSQL são automáticos
- Volumes persistem entre deploys
- Logs são mantidos por 7 dias

---

**Pronto para começar?** Leia [QUICK_START_RAILWAY.md](QUICK_START_RAILWAY.md) e coloque no ar em 15 minutos! 🚀
