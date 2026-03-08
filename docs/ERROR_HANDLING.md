# Sistema de Tratamento Global de Erros

## Visão Geral

O Sistema de Gerenciamento do Sindicato Rural implementa um sistema robusto de tratamento global de erros que fornece:

- Tratamento centralizado de exceções no backend (Spring Boot)
- Tratamento global de erros no frontend (Angular)
- Códigos de erro padronizados entre frontend e backend
- Mensagens de erro amigáveis ao usuário em português
- Logging estruturado para auditoria e debugging
- Respostas HTTP consistentes

## Arquitetura

### Backend (Spring Boot)

#### GlobalExceptionHandler

Classe anotada com `@RestControllerAdvice` que intercepta todas as exceções lançadas pelos controllers e as converte em respostas HTTP padronizadas.

**Localização:** `src/main/java/com/sindicato/exception/GlobalExceptionHandler.java`

**Exceções Tratadas:**

1. **Autenticação e Autorização**
   - `InvalidCredentialsException` → 401 Unauthorized
   - `BadCredentialsException` → 401 Unauthorized
   - `SessionExpiredException` → 401 Unauthorized
   - `AccessDeniedException` → 403 Forbidden

2. **Validação de Dados**
   - `MethodArgumentNotValidException` → 400 Bad Request (validação @Valid)
   - `ValidationException` → 400 Bad Request (validação customizada)
   - `ServletRequestBindingException` → 400/401 Bad Request

3. **Regras de Negócio**
   - `DuplicateEntryException` → 409 Conflict
   - `ResourceNotFoundException` → 404 Not Found
   - `BusinessException` → 400 Bad Request

4. **Arquivos**
   - `FileStorageException` → 500 Internal Server Error
   - `MaxUploadSizeExceededException` → 413 Payload Too Large

5. **Banco de Dados**
   - `DataIntegrityViolationException` → 409 Conflict

6. **Erros Genéricos**
   - `Exception` → 500 Internal Server Error

#### ErrorResponse DTO

Estrutura padronizada para respostas de erro:

```java
{
  "errorCode": "AUTH001",
  "message": "Credenciais inválidas",
  "timestamp": "2026-03-08T16:14:35.885",
  "path": "/api/auth/login",
  "status": 401,
  "fieldErrors": [
    {
      "field": "username",
      "message": "Username é obrigatório",
      "rejectedValue": null
    }
  ],
  "details": {
    "additionalInfo": "value"
  }
}
```

#### ErrorCode Enum

Códigos de erro padronizados seguindo o padrão CATEGORIA + NÚMERO:

**Autenticação (AUTH001-003)**
- `AUTH001` - Credenciais inválidas
- `AUTH002` - Sessão expirada
- `AUTH003` - Acesso negado

**Validação (VAL001-003)**
- `VAL001` - Campo obrigatório ausente
- `VAL002` - Formato inválido
- `VAL003` - Entrada duplicada

**Negócio (BUS001-003)**
- `BUS001` - Sócio não encontrado
- `BUS002` - Pagamento já existe para este período
- `BUS003` - Arquivo excede tamanho máximo permitido

**Sistema (SYS001-003)**
- `SYS001` - Erro de banco de dados
- `SYS002` - Erro no sistema de arquivos
- `SYS003` - Erro interno do servidor

### Frontend (Angular)

#### GlobalErrorHandler

Implementa a interface `ErrorHandler` do Angular para capturar todos os erros não tratados.

**Localização:** `frontend/src/app/core/error-handler/global-error-handler.ts`

**Funcionalidades:**

1. **Tratamento de Erros HTTP**
   - Intercepta erros de requisições HTTP
   - Extrai informações do ErrorResponse do backend
   - Trata casos especiais (sessão expirada, acesso negado)
   - Loga erros estruturados no console

2. **Tratamento de Erros JavaScript**
   - Captura erros de runtime do JavaScript
   - Loga stack traces para debugging
   - Exibe mensagens genéricas ao usuário

3. **Redirecionamentos Automáticos**
   - Sessão expirada → redireciona para /login
   - Acesso negado → redireciona para /dashboard

#### ErrorInterceptor

Interceptor HTTP que captura erros de requisições antes de chegarem aos componentes.

**Localização:** `frontend/src/app/interceptors/error.interceptor.ts`

**Funcionalidades:**

- Loga informações estruturadas de erros HTTP
- Trata sessão expirada automaticamente
- Re-lança o erro para tratamento no componente

#### ErrorService

Serviço utilitário para manipulação e formatação de erros.

**Localização:** `frontend/src/app/services/error.service.ts`

**Métodos:**

- `getErrorMessage(error)` - Extrai mensagem amigável do erro
- `getFieldErrors(error)` - Extrai erros de validação por campo
- `isValidationError(error)` - Verifica se é erro de validação
- `isAuthenticationError(error)` - Verifica se é erro de autenticação
- `logError(error, context)` - Loga erro com contexto

#### Modelos de Erro

**Localização:** `frontend/src/app/models/error.model.ts`

```typescript
interface ErrorResponse {
  errorCode: string;
  message: string;
  timestamp: string;
  path?: string;
  status?: number;
  fieldErrors?: FieldError[];
  details?: Record<string, any>;
}

interface FieldError {
  field: string;
  message: string;
  rejectedValue?: any;
}

enum ErrorCode {
  INVALID_CREDENTIALS = 'AUTH001',
  SESSION_EXPIRED = 'AUTH002',
  // ... outros códigos
}

const ERROR_MESSAGES: Record<string, string> = {
  [ErrorCode.INVALID_CREDENTIALS]: 'Credenciais inválidas. Verifique seu usuário e senha.',
  // ... outras mensagens
}
```

## Logging Estruturado

### Configuração Logback

**Localização:** `src/main/resources/logback-spring.xml`

**Appenders Configurados:**

1. **CONSOLE** - Saída para console (desenvolvimento)
2. **FILE** - Arquivo de log geral (`logs/sindicato-rural.log`)
3. **ERROR_FILE** - Arquivo apenas para erros (`logs/sindicato-rural-error.log`)
4. **JSON_FILE** - Logs estruturados em JSON para produção

**Políticas de Rotação:**

- Rotação diária de arquivos
- Retenção de 30 dias para logs gerais
- Retenção de 90 dias para logs de erro
- Limite de tamanho total: 1GB (geral), 500MB (erros)

**Perfis:**

- **dev** - Logs DEBUG no console e arquivo
- **prod** - Logs INFO em arquivo, WARN para Spring Security
- **test** - Logs DEBUG no console

### Formato de Log

```
2026-03-08 16:14:35.885 [http-nio-8080-exec-1] WARN  c.s.e.GlobalExceptionHandler - Authentication failed: Invalid credentials - Path: /api/auth/login
```

## Uso nos Componentes

### Tratamento de Erros em Serviços

```typescript
// Exemplo: SocioService
criarSocio(socio: SocioRequest): Observable<Socio> {
  return this.http.post<Socio>('/api/socios', socio).pipe(
    catchError((error: HttpErrorResponse) => {
      // Log do erro
      this.errorService.logError(error, 'SocioService.criarSocio');
      
      // Extrair mensagem amigável
      const message = this.errorService.getErrorMessage(error);
      
      // Exibir notificação ao usuário
      this.snackBar.open(message, 'Fechar', { duration: 5000 });
      
      // Re-lançar o erro
      return throwError(() => error);
    })
  );
}
```

### Tratamento de Erros de Validação

```typescript
// Exemplo: Formulário com validação
onSubmit(): void {
  this.socioService.criarSocio(this.form.value).subscribe({
    next: (socio) => {
      this.router.navigate(['/socios', socio.id]);
    },
    error: (error: HttpErrorResponse) => {
      if (this.errorService.isValidationError(error)) {
        // Aplicar erros de validação aos campos do formulário
        const fieldErrors = this.errorService.getFieldErrors(error);
        fieldErrors.forEach((message, field) => {
          const control = this.form.get(field);
          if (control) {
            control.setErrors({ serverError: message });
          }
        });
      }
    }
  });
}
```

## Exceções Customizadas (Backend)

### InvalidCredentialsException

Lançada quando credenciais de autenticação são inválidas.

```java
throw new InvalidCredentialsException("Usuário ou senha incorretos");
```

### SessionExpiredException

Lançada quando uma sessão de usuário expirou.

```java
throw new SessionExpiredException("Sessão expirada. Faça login novamente.");
```

### DuplicateEntryException

Lançada quando há tentativa de criar entrada duplicada.

```java
throw new DuplicateEntryException("cpf", cpf);
```

### ResourceNotFoundException

Lançada quando um recurso solicitado não é encontrado.

```java
throw new ResourceNotFoundException("Socio", "id", id);
```

### ValidationException

Lançada quando validação de dados falha.

```java
ValidationException ex = new ValidationException("Erro de validação");
ex.addFieldError("cpf", "CPF inválido");
ex.addFieldError("nome", "Nome é obrigatório");
throw ex;
```

### BusinessException

Lançada quando uma regra de negócio é violada.

```java
throw new BusinessException("BUS002", "Pagamento já existe para este período");
```

### FileStorageException

Lançada quando operações de armazenamento de arquivo falham.

```java
throw new FileStorageException("Erro ao salvar arquivo: " + filename);
```

## Boas Práticas

### Backend

1. **Use exceções específicas** - Não lance `RuntimeException` genérica
2. **Forneça contexto** - Inclua informações relevantes na mensagem
3. **Não exponha detalhes internos** - Evite stack traces em produção
4. **Valide entrada** - Use `@Valid` e validações customizadas
5. **Logue apropriadamente** - WARN para erros de usuário, ERROR para erros de sistema

### Frontend

1. **Sempre trate erros** - Use `catchError` em observables
2. **Forneça feedback** - Mostre mensagens ao usuário
3. **Não bloqueie a UI** - Permita que o usuário continue usando o sistema
4. **Logue para debugging** - Use `ErrorService.logError()`
5. **Valide no cliente** - Previna erros antes de enviar ao servidor

## Monitoramento e Auditoria

### Logs de Auditoria

Todos os erros são logados com:
- Timestamp
- Código de erro
- Mensagem
- Path da requisição
- Stack trace (para erros de sistema)

### Métricas Recomendadas

1. Taxa de erros por endpoint
2. Tipos de erro mais comuns
3. Tempo de resposta em caso de erro
4. Erros de autenticação/autorização

## Testes

### Backend

**Localização:** `src/test/java/com/sindicato/exception/GlobalExceptionHandlerTest.java`

Testes cobrem:
- Todos os tipos de exceção
- Códigos de status HTTP corretos
- Estrutura de ErrorResponse
- Logging apropriado

### Frontend

**Localização:**
- `frontend/src/app/core/error-handler/global-error-handler.spec.ts`
- `frontend/src/app/services/error.service.spec.ts`

Testes cobrem:
- Tratamento de erros HTTP
- Tratamento de erros JavaScript
- Redirecionamentos automáticos
- Extração de mensagens de erro
- Validação de erros de campo

## Extensibilidade

### Adicionar Novo Código de Erro

1. **Backend** - Adicione ao enum `ErrorCode`:
```java
NEW_ERROR("CAT001", "Descrição do erro")
```

2. **Frontend** - Adicione ao enum e mensagens:
```typescript
enum ErrorCode {
  NEW_ERROR = 'CAT001'
}

const ERROR_MESSAGES = {
  [ErrorCode.NEW_ERROR]: 'Mensagem amigável em português'
}
```

### Adicionar Nova Exceção Customizada

1. Crie a classe de exceção:
```java
public class CustomException extends RuntimeException {
    private final String errorCode;
    
    public CustomException(String message) {
        super(message);
        this.errorCode = ErrorCode.CUSTOM_ERROR.getCode();
    }
}
```

2. Adicione handler no `GlobalExceptionHandler`:
```java
@ExceptionHandler(CustomException.class)
public ResponseEntity<ErrorResponse> handleCustomException(
        CustomException ex, HttpServletRequest request) {
    // Implementação
}
```

## Referências

- [Spring Boot Exception Handling](https://spring.io/guides/tutorials/rest/)
- [Angular Error Handling](https://angular.io/api/core/ErrorHandler)
- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [Logback Documentation](https://logback.qos.ch/documentation.html)
