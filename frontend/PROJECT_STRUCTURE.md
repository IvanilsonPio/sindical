# Frontend - Sistema Sindicato Rural

## Estrutura do Projeto

```
src/app/
├── components/          # Componentes da aplicação
│   ├── login/          # Componente de autenticação
│   └── dashboard/      # Componente principal com navegação
├── services/           # Serviços HTTP
│   ├── auth.service.ts       # Serviço de autenticação
│   ├── socio.service.ts      # Serviço de gestão de sócios
│   ├── pagamento.service.ts  # Serviço de pagamentos
│   └── arquivo.service.ts    # Serviço de arquivos
├── guards/             # Guards de rota
│   └── auth.guard.ts   # Proteção de rotas autenticadas
├── interceptors/       # Interceptors HTTP
│   └── jwt.interceptor.ts    # Adiciona token JWT às requisições
├── models/             # Interfaces TypeScript
│   ├── auth.model.ts
│   ├── socio.model.ts
│   ├── pagamento.model.ts
│   └── arquivo.model.ts
└── environments/       # Configurações de ambiente
    ├── environment.ts
    └── environment.prod.ts
```

## Tecnologias

- **Angular 17**: Framework frontend
- **Angular Material**: Biblioteca de componentes UI
- **RxJS**: Programação reativa
- **TypeScript**: Linguagem tipada

## Funcionalidades Implementadas

### Autenticação
- Login com JWT
- Armazenamento de token no localStorage
- Logout com limpeza de sessão
- Guard para proteção de rotas

### Interceptor HTTP
- Adiciona automaticamente o token JWT em todas as requisições
- Configurado globalmente no app.config.ts

### Serviços
- **AuthService**: Gerenciamento de autenticação
- **SocioService**: CRUD de sócios
- **PagamentoService**: Gestão de pagamentos e recibos
- **ArquivoService**: Upload e download de arquivos

### Design Responsivo
- Layout adaptável para mobile e desktop
- Breakpoint em 768px
- Navegação lateral responsiva
- Estilos globais para responsividade

## Comandos

```bash
# Instalar dependências
npm install

# Desenvolvimento
npm start

# Build de produção
npm run build

# Testes
npm test
```

## Configuração da API

A URL da API é configurada em `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## Próximos Passos

Os seguintes componentes precisam ser implementados nas próximas tarefas:
- SocioListComponent
- SocioFormComponent
- PagamentoListComponent
- PagamentoFormComponent
- ArquivoManagerComponent
