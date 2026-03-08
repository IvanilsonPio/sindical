# ✅ Checklist de Deploy - Railway

Use este checklist para garantir que tudo está configurado corretamente.

## 📋 Pré-Deploy

### Repositório
- [ ] Código commitado no Git
- [ ] Repositório criado no GitHub
- [ ] Push feito para o GitHub
- [ ] Arquivos de configuração presentes:
  - [ ] `railway.json`
  - [ ] `Dockerfile`
  - [ ] `pom.xml`
  - [ ] `src/main/resources/application.yml`

### Conta Railway
- [ ] Conta criada em https://railway.app
- [ ] GitHub conectado ao Railway
- [ ] Railway CLI instalado (opcional): `npm i -g @railway/cli`

## 🚀 Deploy do Backend

### 1. Criar Projeto
- [ ] Projeto criado no Railway
- [ ] Repositório GitHub conectado
- [ ] Build inicial executado (pode falhar sem banco)

### 2. PostgreSQL
- [ ] PostgreSQL adicionado ao projeto
- [ ] Credenciais geradas automaticamente
- [ ] Banco está "Running" (verde)

### 3. Variáveis de Ambiente
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `DB_HOST=${{Postgres.PGHOST}}`
- [ ] `DB_PORT=${{Postgres.PGPORT}}`
- [ ] `DB_NAME=${{Postgres.PGDATABASE}}`
- [ ] `DB_USERNAME=${{Postgres.PGUSER}}`
- [ ] `DB_PASSWORD=${{Postgres.PGPASSWORD}}`
- [ ] `JWT_SECRET=<gerado com openssl rand -base64 32>`
- [ ] `FILE_UPLOAD_DIR=/app/uploads`
- [ ] `PORT=8080`
- [ ] `CORS_ALLOWED_ORIGINS=<se frontend em domínio diferente>`

### 4. Volume Persistente
- [ ] Volume adicionado
- [ ] Mount Path: `/app/uploads`
- [ ] Size: 1GB ou mais

### 5. Deploy
- [ ] Redeploy executado após configurar variáveis
- [ ] Build completou com sucesso
- [ ] Status: "Active" (verde)
- [ ] URL pública gerada

### 6. Verificação
- [ ] Health check funcionando: `https://seu-app.up.railway.app/actuator/health`
- [ ] Resposta: `{"status":"UP"}`
- [ ] Logs sem erros críticos

### 7. Usuário Admin
- [ ] Script `create-admin.sh` executado
- [ ] Usuário admin criado com sucesso
- [ ] Login testado e funcionando

## 🎨 Deploy do Frontend

Escolha uma opção:

### Opção A: Vercel (Recomendado - Grátis)
- [ ] Conta criada em https://vercel.com
- [ ] Repositório conectado
- [ ] Root Directory: `frontend`
- [ ] Build Command: `npm run build -- --configuration production`
- [ ] Output Directory: `dist/frontend/browser`
- [ ] Variável `API_URL` configurada com URL do backend Railway
- [ ] `vercel.json` atualizado com URL do backend
- [ ] Deploy executado
- [ ] Site acessível

### Opção B: Netlify (Alternativa - Grátis)
- [ ] Conta criada em https://netlify.com
- [ ] Repositório conectado
- [ ] Base Directory: `frontend`
- [ ] Build Command: `npm run build -- --configuration production`
- [ ] Publish Directory: `dist/frontend/browser`
- [ ] `netlify.toml` atualizado com URL do backend
- [ ] Deploy executado
- [ ] Site acessível

### Opção C: Railway (Pago - $5/mês adicional)
- [ ] Novo serviço adicionado ao projeto
- [ ] Root Directory: `frontend`
- [ ] Variável `API_URL` configurada
- [ ] Deploy executado
- [ ] Site acessível

## 🔧 Configuração CORS

Se frontend em domínio diferente do backend:
- [ ] `CorsConfig.java` criado
- [ ] Variável `CORS_ALLOWED_ORIGINS` configurada no Railway
- [ ] Backend redeployado
- [ ] CORS testado (sem erros no console do navegador)

## ✅ Testes Finais

### Backend
- [ ] Health check: `GET /actuator/health` → 200 OK
- [ ] Login: `POST /api/auth/login` → 200 OK com token
- [ ] Listar sócios: `GET /api/socios` → 200 OK (com token)
- [ ] Upload de arquivo: `POST /api/arquivos` → 200 OK
- [ ] Download de arquivo: `GET /api/arquivos/{id}` → 200 OK

### Frontend
- [ ] Página de login carrega
- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] Listagem de sócios funciona
- [ ] Cadastro de sócio funciona
- [ ] Upload de arquivo funciona
- [ ] Geração de recibo funciona
- [ ] Sem erros no console do navegador

### Integração
- [ ] Frontend consegue se comunicar com backend
- [ ] Autenticação JWT funciona
- [ ] Upload de arquivos persiste (não some após redeploy)
- [ ] Dados do banco persistem

## 📊 Monitoramento

### Railway Dashboard
- [ ] Métricas de CPU/RAM normais
- [ ] Logs sem erros críticos
- [ ] Uptime > 99%
- [ ] Latência aceitável

### Backups
- [ ] Backup automático do PostgreSQL configurado
- [ ] Backup manual testado
- [ ] Procedimento de restore documentado

## 🔒 Segurança

- [ ] JWT_SECRET é único e seguro (mínimo 32 caracteres)
- [ ] Senhas de banco não estão hardcoded
- [ ] HTTPS habilitado (automático no Railway)
- [ ] CORS configurado corretamente
- [ ] Headers de segurança configurados
- [ ] Senha do admin alterada do padrão

## 📝 Documentação

- [ ] URL de produção documentada
- [ ] Credenciais salvas em local seguro (1Password, etc)
- [ ] Procedimentos de deploy documentados
- [ ] Contatos de suporte anotados

## 💰 Custos

- [ ] Plano Railway confirmado: $5/mês (Hobby)
- [ ] Método de pagamento configurado
- [ ] Alertas de billing configurados

## 🎯 Próximos Passos (Opcional)

- [ ] Domínio customizado configurado
- [ ] SSL customizado (se necessário)
- [ ] Monitoramento externo (UptimeRobot, etc)
- [ ] Alertas de erro configurados
- [ ] CI/CD avançado configurado
- [ ] Testes automatizados no deploy

---

## 🆘 Troubleshooting

### Build falha
1. Verifique logs no Railway
2. Confirme que todas as variáveis estão configuradas
3. Verifique se PostgreSQL está rodando
4. Tente redeploy

### Aplicação não inicia
1. Verifique variáveis de ambiente
2. Verifique conexão com banco
3. Verifique logs de erro
4. Confirme que Volume está montado

### CORS error
1. Verifique `CORS_ALLOWED_ORIGINS`
2. Confirme que inclui protocolo (https://)
3. Redeploy após alterar
4. Limpe cache do navegador

### Uploads não persistem
1. Confirme que Volume está configurado
2. Verifique mount path: `/app/uploads`
3. Confirme `FILE_UPLOAD_DIR=/app/uploads`
4. Verifique permissões no Dockerfile

---

**Data do deploy**: _______________
**Deployado por**: _______________
**URL de produção**: _______________
**Versão**: _______________
