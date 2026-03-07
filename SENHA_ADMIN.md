# Senha do Administrador - BCrypt Hash

## Informações da Senha

**Senha original**: `admin123`

**Senha criptografada (BCrypt)**: 
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

## Como Inserir o Usuário Admin no Banco de Dados

### Opção 1: Usando o arquivo SQL

```bash
sudo docker exec -i sindicato-postgres psql -U sindicato_user -d sindicato_rural_dev < insert_admin_user.sql
```

### Opção 2: Manualmente via psql

1. Conectar ao PostgreSQL:
```bash
sudo docker exec -it sindicato-postgres psql -U sindicato_user -d sindicato_rural_dev
```

2. Executar o INSERT:
```sql
INSERT INTO usuarios (username, password, nome, status, criado_em, atualizado_em) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', 'ATIVO', NOW(), NOW());
```

3. Verificar se foi criado:
```sql
SELECT id, username, nome, status, criado_em FROM usuarios WHERE username = 'admin';
```

4. Sair do PostgreSQL:
```
\q
```

## Credenciais de Login

Após inserir o usuário no banco de dados, use estas credenciais para fazer login:

- **Username**: `admin`
- **Password**: `admin123`

## Testar o Login

### Via Frontend (Angular)
1. Acesse: http://localhost:4200
2. Digite as credenciais acima
3. Clique em "Entrar"

### Via API (cURL)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Resposta esperada:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

## Observações

- O hash BCrypt foi gerado usando o algoritmo BCrypt com cost factor 10 (padrão do Spring Security)
- A senha `admin123` é apenas para desenvolvimento/testes
- Em produção, use uma senha forte e altere-a imediatamente após o primeiro login
- O BCrypt gera um salt aleatório, então cada execução do encoder produz um hash diferente (mas todos são válidos para a mesma senha)
