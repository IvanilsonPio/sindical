# Configuração de Segurança - Sistema Sindicato Rural

## Visão Geral

Este documento descreve a implementação da segurança do sistema usando Spring Security com autenticação JWT (JSON Web Token).

## Componentes Principais

### 1. SecurityConfig

Classe principal de configuração de segurança que define:

- **PasswordEncoder**: BCrypt para criptografia de senhas
- **AuthenticationProvider**: Provedor de autenticação usando UserDetailsService
- **AuthenticationManager**: Gerenciador de autenticação
- **SecurityFilterChain**: Cadeia de filtros de segurança com proteção de endpoints
- **CorsConfigurationSource**: Configuração CORS para permitir requisições do frontend

#### Endpoints Públicos (Não Requerem Autenticação)

- `/api/auth/**` - Endpoints de autenticação (login, refresh)
- `/api/health/**` - Endpoint de health check
- `/error` - Endpoint de erro padrão

#### Endpoints Protegidos (Requerem Autenticação)

Todos os outros endpoints requerem token JWT válido no header `Authorization: Bearer <token>`.

### 2. JwtAuthenticationFilter

Filtro que intercepta todas as requisições HTTP e valida tokens JWT.

**Fluxo de Processamento:**

1. Extrai o header `Authorization` da requisição
2. Verifica se o header começa com "Bearer "
3. Extrai o token JWT do header
4. Valida o token usando JwtUtil
5. Carrega os detalhes do usuário usando UserDetailsService
6. Cria e define a autenticação no SecurityContext
7. Continua a cadeia de filtros

**Tratamento de Erros:**

- Tokens inválidos ou expirados são silenciosamente rejeitados
- Erros são logados mas não interrompem a cadeia de filtros
- Usuário não autenticado recebe status 401 Unauthorized

### 3. CustomUserDetailsService

Implementação de UserDetailsService que carrega usuários do banco de dados.

**Responsabilidades:**

- Buscar usuário por username no UsuarioRepository
- Converter entidade Usuario para UserDetails do Spring Security
- Lançar UsernameNotFoundException se usuário não encontrado

## Configuração de Sessão

O sistema usa **SessionCreationPolicy.STATELESS**, o que significa:

- Não cria sessões HTTP no servidor
- Toda autenticação é baseada em tokens JWT
- Cada requisição deve incluir o token JWT
- Não há estado de sessão mantido no servidor

## Configuração CORS

### Origens Permitidas

- `http://localhost:4200` - Angular development server
- `http://localhost:8080` - Produção local

### Métodos HTTP Permitidos

- GET, POST, PUT, DELETE, OPTIONS, PATCH

### Headers Permitidos

- Authorization
- Content-Type
- Accept
- X-Requested-With

### Headers Expostos

- Authorization (para permitir que o frontend leia o token)

### Configurações Adicionais

- **allowCredentials**: true (permite cookies e headers de autorização)
- **maxAge**: 3600 segundos (1 hora de cache da configuração CORS)

## Fluxo de Autenticação

### 1. Login

```
Cliente → POST /api/auth/login
        ↓
AuthController recebe credenciais
        ↓
AuthService valida credenciais
        ↓
JwtUtil gera token JWT
        ↓
Cliente recebe token no response
```

### 2. Requisição Autenticada

```
Cliente → GET /api/socios
        ↓ (Header: Authorization: Bearer <token>)
JwtAuthenticationFilter intercepta
        ↓
Extrai e valida token
        ↓
Define autenticação no SecurityContext
        ↓
Controller processa requisição
        ↓
Cliente recebe resposta
```

### 3. Token Expirado

```
Cliente → GET /api/socios
        ↓ (Header: Authorization: Bearer <token_expirado>)
JwtAuthenticationFilter intercepta
        ↓
Valida token (falha - expirado)
        ↓
Não define autenticação
        ↓
Spring Security retorna 401 Unauthorized
        ↓
Cliente deve fazer novo login
```

## Requisitos Atendidos

### Requisito 1.3: Expiração de Sessão

**Implementação:**
- Tokens JWT têm tempo de expiração configurável (padrão: 30 minutos)
- JwtUtil valida expiração em cada requisição
- Tokens expirados são automaticamente rejeitados
- Cliente recebe 401 Unauthorized e deve fazer novo login

### Requisito 1.5: Encerramento Automático de Sessão

**Implementação:**
- SessionCreationPolicy.STATELESS garante que não há sessão persistente
- Token JWT expira após período de inatividade (configurável via `jwt.expiration`)
- Cada requisição valida a expiração do token
- Não há necessidade de logout explícito - token simplesmente expira

## Configuração de Propriedades

### application.yml

```yaml
jwt:
  secret: ${JWT_SECRET:sua-chave-secreta-muito-longa-e-segura-aqui}
  expiration: 1800000  # 30 minutos em milissegundos
  refresh-expiration: 86400000  # 24 horas em milissegundos
```

### Variáveis de Ambiente

- `JWT_SECRET`: Chave secreta para assinatura de tokens (deve ser forte e única por ambiente)

## Testes

### SecurityConfigTest

Testes unitários para configuração de segurança:
- Validação do PasswordEncoder BCrypt
- Criptografia de senhas
- Geração de hashes diferentes para mesma senha
- Validação de senhas corretas e incorretas

### JwtAuthenticationFilterTest

Testes unitários para o filtro JWT:
- Processamento de requisições sem token
- Processamento de requisições com token válido
- Rejeição de tokens inválidos ou expirados
- Tratamento de erros e exceções

### SecurityConfigIntegrationTest

Testes de integração end-to-end:
- Acesso a endpoints públicos sem autenticação
- Bloqueio de endpoints protegidos sem token
- Acesso a endpoints protegidos com token válido
- Validação de configuração CORS
- Testes de preflight CORS

## Segurança

### Boas Práticas Implementadas

1. **Senhas Criptografadas**: BCrypt com salt automático
2. **Tokens Assinados**: JWT assinado com HMAC-SHA256
3. **Sessões Stateless**: Sem estado no servidor
4. **CORS Configurado**: Apenas origens confiáveis
5. **CSRF Desabilitado**: Apropriado para APIs stateless com JWT
6. **Validação de Token**: Em cada requisição
7. **Expiração de Token**: Tokens têm tempo de vida limitado
8. **Refresh Token**: Suporte para renovação de tokens

### Recomendações de Produção

1. **JWT_SECRET**: Use uma chave forte e única (mínimo 256 bits)
2. **HTTPS**: Sempre use HTTPS em produção
3. **CORS**: Configure apenas origens necessárias
4. **Logging**: Monitore tentativas de acesso não autorizado
5. **Rate Limiting**: Implemente rate limiting para endpoints de autenticação
6. **Token Rotation**: Considere rotação periódica de tokens
7. **Blacklist**: Implemente blacklist de tokens para logout forçado

## Troubleshooting

### Problema: 401 Unauthorized em todas as requisições

**Possíveis Causas:**
- Token não está sendo enviado no header
- Token está malformado (não começa com "Bearer ")
- Token expirado
- JWT_SECRET diferente entre geração e validação

**Solução:**
- Verificar que o header Authorization está presente
- Verificar formato: `Authorization: Bearer <token>`
- Verificar expiração do token
- Verificar consistência do JWT_SECRET

### Problema: CORS errors no frontend

**Possíveis Causas:**
- Origem do frontend não está na lista de origens permitidas
- Headers necessários não estão permitidos

**Solução:**
- Adicionar origem do frontend em `corsConfigurationSource()`
- Verificar que headers necessários estão em `allowedHeaders`

### Problema: Testes falhando com 401

**Possíveis Causas:**
- Filtros de segurança habilitados em testes unitários
- Token não está sendo gerado corretamente nos testes

**Solução:**
- Usar `@AutoConfigureMockMvc(addFilters = false)` em testes unitários
- Gerar token válido nos testes de integração usando JwtUtil
