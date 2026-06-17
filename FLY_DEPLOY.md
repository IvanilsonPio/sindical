# Deploy no Fly.io

## Pré-requisitos

1. Instalar flyctl:
```bash
curl -L https://fly.io/install.sh | sh
```

2. Login:
```bash
fly auth login
```

---

## 1. Criar o app

```bash
fly apps create sindicato-rural-backend
```

> Se o nome já estiver em uso, escolha outro e atualize o campo `app` no `fly.toml`.

---

## 2. Criar o PostgreSQL

```bash
fly postgres create \
  --name sindicato-rural-db \
  --vm-size shared-cpu-1x \
  --volume-size 1
```

Depois conectar ao app:
```bash
fly postgres attach sindicato-rural-db --app sindicato-rural-backend
```

Isso cria automaticamente a variável `DATABASE_URL` no app.

---

## 3. Criar o volume persistente para uploads

```bash
fly volumes create sindicato_uploads \
  --size 1 \
  --app sindicato-rural-backend
```

---

## 4. Configurar variáveis de ambiente

```bash
fly secrets set \
  JWT_SECRET=$(openssl rand -base64 32) \
  MAIL_HOST=smtp.gmail.com \
  MAIL_PORT=587 \
  MAIL_USERNAME=seu-email@gmail.com \
  MAIL_PASSWORD=sua-senha-de-app \
  FRONTEND_URL=https://seu-frontend.vercel.app \
  --app sindicato-rural-backend
```

As variáveis de banco (DB_HOST, DB_PORT, etc.) vêm automaticamente do `fly postgres attach`.
Mas a aplicação usa variáveis separadas, então mapeie assim:

```bash
# Pegar a DATABASE_URL gerada pelo attach
fly secrets list --app sindicato-rural-backend

# Setar as variáveis que o application.yml espera
fly secrets set \
  DB_HOST=<PGHOST do postgres> \
  DB_PORT=<PGPORT> \
  DB_NAME=<PGDATABASE> \
  DB_USERNAME=<PGUSER> \
  DB_PASSWORD=<PGPASSWORD> \
  --app sindicato-rural-backend
```

Ou use a DATABASE_URL diretamente — veja a seção abaixo.

---

## 5. Deploy

```bash
fly deploy --app sindicato-rural-backend
```

---

## 6. Verificar

```bash
# Ver logs
fly logs --app sindicato-rural-backend

# Status das máquinas
fly status --app sindicato-rural-backend

# Abrir no browser
fly open --app sindicato-rural-backend
```

---

## Notas sobre o plano gratuito

- Fly.io exige cartão de crédito após o trial de 7 dias
- Com cartão cadastrado, projetos pequenos ficam dentro do allowance mensal gratuito:
  - 3 VMs shared-cpu-1x com 256MB RAM
  - 3GB de volumes
- Esta configuração usa 512MB de RAM — pode ultrapassar o free tier dependendo do uso
- Para economizar, reduza para `memory = "256mb"` no `fly.toml` se a aplicação aguentar

---

## Comandos úteis

```bash
# Redeploy sem rebuild
fly deploy --strategy immediate

# SSH na máquina
fly ssh console --app sindicato-rural-backend

# Ver variáveis configuradas
fly secrets list --app sindicato-rural-backend

# Escalar para zero (economizar)
fly scale count 0 --app sindicato-rural-backend
```
