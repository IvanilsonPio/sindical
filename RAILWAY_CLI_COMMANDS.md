# 🚂 Railway CLI - Comandos Úteis

Referência rápida de comandos do Railway CLI.

## 📦 Instalação

```bash
# NPM
npm i -g @railway/cli

# Homebrew (Mac)
brew install railway

# Verificar instalação
railway --version
```

## 🔐 Autenticação

```bash
# Login
railway login

# Logout
railway logout

# Verificar usuário atual
railway whoami
```

## 🚀 Projeto

```bash
# Criar novo projeto
railway init

# Conectar a projeto existente
railway link

# Ver informações do projeto
railway status

# Abrir dashboard no navegador
railway open

# Desconectar do projeto
railway unlink
```

## 📊 Deploy e Logs

```bash
# Deploy manual (geralmente automático via GitHub)
railway up

# Ver logs em tempo real
railway logs

# Ver logs com filtro
railway logs --filter "ERROR"

# Ver últimas 100 linhas
railway logs --tail 100
```

## 🔧 Variáveis de Ambiente

```bash
# Listar todas as variáveis
railway variables

# Adicionar variável
railway variables set KEY=value

# Adicionar múltiplas variáveis
railway variables set KEY1=value1 KEY2=value2

# Remover variável
railway variables delete KEY

# Exportar variáveis para arquivo
railway variables > .env.railway
```

## 🗄️ Banco de Dados

```bash
# Conectar ao PostgreSQL
railway connect postgres

# Executar comando SQL
railway run psql -c "SELECT * FROM usuarios;"

# Fazer backup
railway connect postgres -- pg_dump > backup.sql

# Restaurar backup
railway connect postgres -- psql < backup.sql
```

## 🐚 Shell e Comandos

```bash
# Abrir shell no container
railway shell

# Executar comando único
railway run bash create-admin.sh

# Executar comando com variáveis de ambiente
railway run java -jar app.jar

# Executar script local com variáveis do Railway
railway run ./local-script.sh
```

## 📦 Serviços

```bash
# Listar serviços do projeto
railway service

# Selecionar serviço específico
railway service select

# Ver informações do serviço
railway service info
```

## 🔄 Deployments

```bash
# Listar deployments
railway deployment list

# Ver detalhes de um deployment
railway deployment info <deployment-id>

# Fazer rollback para deployment anterior
railway deployment rollback <deployment-id>
```

## 📁 Volumes

```bash
# Listar volumes
railway volume list

# Ver detalhes de um volume
railway volume info <volume-id>

# Fazer backup de volume
railway volume backup <volume-id>
```

## 🌐 Domínios

```bash
# Listar domínios
railway domain

# Adicionar domínio customizado
railway domain add example.com

# Remover domínio
railway domain remove example.com
```

## 🔍 Debugging

```bash
# Ver logs de build
railway logs --deployment <deployment-id>

# Ver logs de erro
railway logs --filter "ERROR|FATAL"

# Ver métricas
railway metrics

# Ver eventos do projeto
railway events
```

## 💻 Desenvolvimento Local

```bash
# Rodar aplicação localmente com variáveis do Railway
railway run npm start

# Rodar aplicação localmente com variáveis do Railway (Java)
railway run mvn spring-boot:run

# Criar arquivo .env local com variáveis do Railway
railway variables > .env
```

## 🔧 Configuração

```bash
# Ver configuração atual
railway config

# Definir projeto padrão
railway config set project <project-id>

# Definir ambiente padrão
railway config set environment production
```

## 📊 Monitoramento

```bash
# Ver uso de recursos
railway metrics

# Ver status dos serviços
railway status

# Ver histórico de deploys
railway deployment list --limit 10
```

## 🎯 Exemplos Práticos

### Deploy completo do zero

```bash
# 1. Login
railway login

# 2. Criar projeto
railway init

# 3. Adicionar PostgreSQL (via dashboard)
railway open

# 4. Configurar variáveis
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set JWT_SECRET=$(openssl rand -base64 32)
railway variables set FILE_UPLOAD_DIR=/app/uploads

# 5. Deploy
git push  # Railway faz deploy automático

# 6. Ver logs
railway logs

# 7. Criar admin
railway run bash create-admin.sh
```

### Backup e Restore

```bash
# Backup do banco
railway connect postgres -- pg_dump -Fc > backup_$(date +%Y%m%d).dump

# Restore do banco
railway connect postgres -- pg_restore -d $DATABASE_URL backup_20260308.dump

# Backup de volume (uploads)
railway volume backup <volume-id>
```

### Debugging de problemas

```bash
# Ver logs de erro
railway logs --filter "ERROR" --tail 100

# Verificar variáveis
railway variables

# Verificar status
railway status

# Abrir shell para investigar
railway shell

# Ver arquivos no volume
railway shell
ls -la /app/uploads
```

### Testar localmente com variáveis de produção

```bash
# Exportar variáveis
railway variables > .env.railway

# Rodar localmente
source .env.railway
mvn spring-boot:run

# Ou usar railway run
railway run mvn spring-boot:run
```

## 🆘 Troubleshooting

### Comando não encontrado
```bash
# Reinstalar CLI
npm i -g @railway/cli

# Verificar PATH
echo $PATH
```

### Não consegue conectar ao projeto
```bash
# Relink
railway unlink
railway link

# Ou especificar projeto
railway link <project-id>
```

### Variáveis não aparecem
```bash
# Verificar ambiente
railway environment

# Selecionar ambiente correto
railway environment select production
```

## 📚 Recursos

- Documentação: https://docs.railway.app/develop/cli
- GitHub: https://github.com/railwayapp/cli
- Discord: https://railway.app/discord

## 🔑 Atalhos Úteis

```bash
# Alias úteis para adicionar no ~/.bashrc ou ~/.zshrc
alias rl='railway'
alias rll='railway logs'
alias rls='railway status'
alias rlo='railway open'
alias rlv='railway variables'
alias rlr='railway run'
alias rlsh='railway shell'
```

## 💡 Dicas

1. Use `railway run` para executar comandos com variáveis de ambiente
2. `railway logs` aceita flags do Docker (--tail, --follow, etc)
3. `railway connect` cria túnel temporário para serviços
4. Variáveis com `${{}}` são referências entre serviços
5. Use `railway shell` para debugging interativo

---

**Comando mais usado**: `railway logs` 📊
**Comando mais útil**: `railway run` 🚀
**Comando para emergências**: `railway deployment rollback` 🔄
