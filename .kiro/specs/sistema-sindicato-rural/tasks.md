# Plano de Implementação: Sistema de Gerenciamento do Sindicato Rural

## Visão Geral

Este plano implementa um sistema web completo para gerenciamento de sindicato de trabalhadores rurais usando Angular (frontend), Spring Boot (backend) e PostgreSQL (banco de dados). A implementação seguirá uma abordagem incremental, construindo primeiro a infraestrutura base, depois os módulos de negócio e finalmente a integração completa.

## Tarefas

- [-] 1. Configuração da infraestrutura base
  - [x] 1.1 Configurar projeto Spring Boot com dependências necessárias
    - Criar projeto Spring Boot com Spring Security, JPA, PostgreSQL driver, JWT
    - Configurar application.yml com perfis de desenvolvimento e produção
    - Configurar conexão com PostgreSQL e pool de conexões
    - _Requisitos: 6.1, 6.4_

  - [x] 1.2 Configurar projeto Angular com estrutura base
    - Criar projeto Angular com Angular Material e routing
    - Configurar estrutura de pastas (components, services, guards, models)
    - Configurar interceptors HTTP e guards de autenticação
    - _Requisitos: 7.1, 7.2_

  - [x] 1.3 Criar schema do banco de dados PostgreSQL
    - Implementar scripts SQL para criação de tabelas (usuarios, socios, pagamentos, arquivos)
    - Criar índices para performance e constraints de integridade
    - Configurar Flyway para migração de banco de dados
    - _Requisitos: 6.1, 6.5_

- [ ]* 1.4 Configurar testes de propriedade para infraestrutura
  - **Propriedade 21: Transações ACID**
  - **Valida: Requisitos 6.1, 6.5**

- [ ] 2. Implementar sistema de autenticação e segurança
  - [x] 2.1 Criar entidade Usuario e repository JPA
    - Implementar entidade Usuario com validações
    - Criar UsuarioRepository com métodos de busca
    - Implementar criptografia de senha com BCrypt
    - _Requisitos: 1.4_

  - [x] 2.2 Implementar AuthService e JWT utilities
    - Criar AuthService para autenticação e geração de tokens
    - Implementar JwtUtil para criação, validação e extração de dados do token
    - Configurar expiração de token e refresh token
    - _Requisitos: 1.1, 1.2, 1.5_

  - [x] 2.3 Criar AuthController com endpoints de autenticação
    - Implementar endpoint POST /api/auth/login
    - Implementar endpoint POST /api/auth/refresh
    - Configurar tratamento de erros de autenticação
    - _Requisitos: 1.1, 1.2_

  - [x] 2.4 Configurar Spring Security com JWT
    - Implementar JwtAuthenticationFilter
    - Configurar SecurityConfig com proteção de endpoints
    - Implementar controle de sessão e CORS
    - _Requisitos: 1.3, 1.5_

  - [ ]* 2.5 Implementar testes de propriedade para autenticação
    - **Propriedade 1: Autenticação de credenciais válidas**
    - **Propriedade 2: Rejeição de credenciais inválidas**
    - **Propriedade 3: Criptografia segura de senhas**
    - **Valida: Requisitos 1.1, 1.2, 1.4**

- [ ] 3. Desenvolver módulo de gestão de sócios
  - [ ] 3.1 Criar entidade Socio e SocioRepository
    - Implementar entidade Socio com todas as propriedades e validações
    - Criar SocioRepository com métodos de busca customizados
    - Implementar validação de CPF único e formato
    - _Requisitos: 2.4_

  - [ ] 3.2 Implementar SocioService com regras de negócio
    - Criar métodos para CRUD de sócios com validações
    - Implementar busca por nome, CPF e matrícula
    - Implementar controle de histórico de alterações
    - _Requisitos: 2.1, 2.2, 2.3, 2.5_

  - [ ] 3.3 Criar SocioController com endpoints REST
    - Implementar endpoints GET, POST, PUT, DELETE para sócios
    - Configurar paginação e filtros de busca
    - Implementar tratamento de erros específicos
    - _Requisitos: 2.1, 2.2_

  - [ ]* 3.4 Implementar testes de propriedade para gestão de sócios
    - **Propriedade 5: Validação de dados obrigatórios**
    - **Propriedade 6: Busca por critérios múltiplos**
    - **Propriedade 7: Unicidade de CPF**
    - **Propriedade 8: Preservação de histórico**
    - **Valida: Requisitos 2.1, 2.2, 2.3, 2.4**

- [ ] 4. Checkpoint - Verificar funcionalidades básicas
  - Garantir que todos os testes passem, perguntar ao usuário se surgem dúvidas.

- [ ] 5. Implementar sistema de pagamentos
  - [ ] 5.1 Criar entidade Pagamento e PagamentoRepository
    - Implementar entidade Pagamento com relacionamento com Socio
    - Criar constraint única para socio_id + mes + ano
    - Implementar geração automática de número de recibo
    - _Requisitos: 3.1, 3.4, 4.1_

  - [ ] 5.2 Implementar PagamentoService com lógica de negócio
    - Criar métodos para registro e cancelamento de pagamentos
    - Implementar atualização automática de status de adimplência
    - Implementar validação de pagamentos duplicados
    - _Requisitos: 3.2, 3.4, 3.5_

  - [ ] 5.3 Criar PagamentoController com endpoints REST
    - Implementar endpoints para CRUD de pagamentos
    - Configurar filtros por período, sócio e status
    - Implementar endpoint para consulta de inadimplentes
    - _Requisitos: 3.1, 3.3_

  - [ ]* 5.4 Implementar testes de propriedade para pagamentos
    - **Propriedade 9: Associação correta de pagamentos**
    - **Propriedade 10: Prevenção de pagamentos duplicados**
    - **Propriedade 11: Consistência de status de adimplência**
    - **Propriedade 12: Filtros de consulta de pagamentos**
    - **Valida: Requisitos 3.1, 3.2, 3.3, 3.4, 3.5**

- [ ] 6. Desenvolver sistema de geração de recibos
  - [ ] 6.1 Implementar ReciboService para geração de PDFs
    - Configurar biblioteca iText para geração de PDF
    - Criar template de recibo com dados obrigatórios
    - Implementar numeração sequencial e controle de segunda via
    - _Requisitos: 4.1, 4.2, 4.3_

  - [ ] 6.2 Integrar geração de recibos com pagamentos
    - Modificar PagamentoService para gerar recibo automaticamente
    - Implementar armazenamento permanente de recibos
    - Criar endpoint para download e reimpressão de recibos
    - _Requisitos: 4.4, 4.5_

  - [ ]* 6.3 Implementar testes de propriedade para recibos
    - **Propriedade 13: Geração automática de recibos**
    - **Propriedade 14: Completude de dados em recibos**
    - **Propriedade 15: Consistência de reimpressão**
    - **Propriedade 16: Exportação PDF válida**
    - **Valida: Requisitos 4.1, 4.2, 4.3, 4.4**

- [ ] 7. Implementar sistema de gerenciamento de arquivos
  - [ ] 7.1 Criar entidade Arquivo e ArquivoRepository
    - Implementar entidade Arquivo com relacionamento com Socio
    - Configurar armazenamento no sistema de arquivos
    - Implementar validação de tipos e tamanhos de arquivo
    - _Requisitos: 5.2_

  - [ ] 7.2 Implementar ArquivoService para upload e download
    - Criar métodos para upload múltiplo de arquivos
    - Implementar validação de formato e tamanho
    - Configurar armazenamento seguro e organizado por sócio
    - _Requisitos: 5.1, 5.2, 5.4_

  - [ ] 7.3 Criar ArquivoController com endpoints de arquivo
    - Implementar endpoint para upload múltiplo
    - Criar endpoints para listagem, download e exclusão
    - Configurar streaming de arquivos grandes
    - _Requisitos: 5.3, 5.4, 5.5_

  - [ ]* 7.4 Implementar testes de propriedade para arquivos
    - **Propriedade 17: Validação de upload de arquivos**
    - **Propriedade 18: Completude de listagem de arquivos**
    - **Propriedade 19: Disponibilidade de download**
    - **Propriedade 20: Exclusão completa de arquivos**
    - **Valida: Requisitos 5.1, 5.2, 5.3, 5.4, 5.5**

- [ ] 8. Checkpoint - Verificar backend completo
  - Garantir que todos os testes passem, perguntar ao usuário se surgem dúvidas.

- [ ] 9. Desenvolver frontend Angular - Autenticação
  - [ ] 9.1 Criar modelos TypeScript e interfaces
    - Implementar interfaces para todas as entidades (Usuario, Socio, Pagamento, Arquivo)
    - Criar DTOs para requests e responses
    - Definir enums para status e tipos
    - _Requisitos: 1.1, 2.1_

  - [ ] 9.2 Implementar AuthService e interceptors
    - Criar AuthService para login, logout e gerenciamento de token
    - Implementar JwtInterceptor para adicionar token nas requisições
    - Criar AuthGuard para proteção de rotas
    - _Requisitos: 1.1, 1.2, 1.3_

  - [ ] 9.3 Criar componente de login
    - Implementar LoginComponent com formulário reativo
    - Configurar validação de campos e feedback de erro
    - Implementar redirecionamento após login bem-sucedido
    - _Requisitos: 1.1, 1.2_

  - [ ]* 9.4 Implementar testes unitários para autenticação frontend
    - Testar AuthService, AuthGuard e LoginComponent
    - Validar fluxos de login e logout
    - _Requisitos: 1.1, 1.2, 1.3_

- [ ] 10. Desenvolver frontend Angular - Dashboard e navegação
  - [ ] 10.1 Criar layout principal e navegação
    - Implementar AppComponent com toolbar e sidenav
    - Criar DashboardComponent com cards de navegação
    - Configurar roteamento para todas as funcionalidades
    - _Requisitos: 7.1, 7.2_

  - [ ] 10.2 Implementar componentes de listagem de sócios
    - Criar SocioListComponent com tabela paginada
    - Implementar filtros de busca por nome, CPF e matrícula
    - Configurar ações de visualizar, editar e excluir
    - _Requisitos: 2.2, 7.2_

  - [ ] 10.3 Criar formulário de cadastro/edição de sócios
    - Implementar SocioFormComponent com validação reativa
    - Configurar máscaras para CPF, telefone e CEP
    - Implementar upload de foto do sócio
    - _Requisitos: 2.1, 2.3_

  - [ ]* 10.4 Implementar testes de propriedade para responsividade
    - **Propriedade 24: Responsividade de layout**
    - **Valida: Requisitos 7.2**

- [ ] 11. Desenvolver frontend Angular - Gestão de pagamentos
  - [ ] 11.1 Criar componentes de listagem de pagamentos
    - Implementar PagamentoListComponent com filtros avançados
    - Configurar filtros por período, sócio e status
    - Implementar visualização de inadimplentes
    - _Requisitos: 3.3_

  - [ ] 11.2 Implementar formulário de registro de pagamentos
    - Criar PagamentoFormComponent com seleção de sócio
    - Configurar validação de período e valor
    - Implementar geração automática de recibo após pagamento
    - _Requisitos: 3.1, 4.1_

  - [ ] 11.3 Criar funcionalidades de recibos
    - Implementar visualização e download de recibos em PDF
    - Configurar reimpressão de segunda via
    - Criar histórico de recibos por sócio
    - _Requisitos: 4.3, 4.4, 4.5_

  - [ ]* 11.4 Implementar testes unitários para pagamentos frontend
    - Testar componentes de pagamento e recibos
    - Validar filtros e geração de PDFs
    - _Requisitos: 3.1, 3.3, 4.1_

- [ ] 12. Desenvolver frontend Angular - Gestão de arquivos
  - [ ] 12.1 Implementar componente de upload de arquivos
    - Criar ArquivoUploadComponent com drag-and-drop
    - Configurar validação de tipo e tamanho de arquivo
    - Implementar barra de progresso para uploads
    - _Requisitos: 5.1, 5.2_

  - [ ] 12.2 Criar gerenciador de arquivos por sócio
    - Implementar ArquivoManagerComponent com listagem
    - Configurar visualização de thumbnails para imagens
    - Implementar download e exclusão de arquivos
    - _Requisitos: 5.3, 5.4, 5.5_

  - [ ]* 12.3 Implementar testes unitários para arquivos frontend
    - Testar upload, listagem e download de arquivos
    - Validar componentes de gerenciamento
    - _Requisitos: 5.1, 5.3, 5.4_

- [ ] 13. Implementar tratamento de erros e performance
  - [ ] 13.1 Configurar tratamento global de erros
    - Implementar GlobalErrorHandler no Angular
    - Criar GlobalExceptionHandler no Spring Boot
    - Configurar logging estruturado e monitoramento
    - _Requisitos: 7.5_

  - [ ] 13.2 Otimizar performance e responsividade
    - Implementar lazy loading de módulos no Angular
    - Configurar cache HTTP e otimização de queries
    - Implementar paginação virtual para listas grandes
    - _Requisitos: 7.4_

  - [ ]* 13.3 Implementar testes de propriedade para performance
    - **Propriedade 25: Performance de carregamento**
    - **Propriedade 26: Mensagens de erro informativas**
    - **Valida: Requisitos 7.4, 7.5**

- [ ] 14. Integração final e testes de sistema
  - [ ] 14.1 Configurar build e deployment
    - Configurar build do Angular para produção
    - Criar Dockerfile para Spring Boot
    - Configurar docker-compose com PostgreSQL
    - _Requisitos: 6.1_

  - [ ] 14.2 Implementar funcionalidades de backup e auditoria
    - Configurar backup automático do PostgreSQL
    - Implementar logs de auditoria para todas as operações
    - Criar relatórios de atividade do sistema
    - _Requisitos: 6.3_

  - [ ]* 14.3 Executar testes de integração completos
    - Testar fluxos end-to-end completos
    - Validar integração entre todos os módulos
    - Verificar performance sob carga
    - _Requisitos: 6.2, 6.4_

- [ ] 15. Checkpoint final - Garantir que todos os testes passem
  - Garantir que todos os testes passem, perguntar ao usuário se surgem dúvidas.

## Notas

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- Cada tarefa referencia requisitos específicos para rastreabilidade
- Checkpoints garantem validação incremental
- Testes de propriedade validam propriedades universais de correção
- Testes unitários validam exemplos específicos e casos extremos
- A implementação segue uma abordagem incremental: infraestrutura → backend → frontend → integração