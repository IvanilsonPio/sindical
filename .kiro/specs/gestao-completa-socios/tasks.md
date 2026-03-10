
# Plano de Implementação: Gestão Completa de Sócios

## Visão Geral

Este plano implementa três funcionalidades integradas para gestão completa de sócios: visualização detalhada, edição de dados e gestão de arquivos vinculados. A implementação será incremental, começando pelo backend e seguindo para o frontend, com integração contínua entre as funcionalidades.

## Tasks

- [ ] 1. Estender backend para visualização detalhada de sócios
  - [x] 1.1 Criar DTO SocioDetalhadoResponse com todos os campos e relacionamentos
    - Incluir dados pessoais, endereço, contato, status, pagamentos e arquivos
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_
  
  - [x] 1.2 Adicionar query otimizada no SocioRepository com JOIN FETCH
    - Implementar findByIdWithRelations para carregar pagamentos e arquivos em uma query
    - _Requirements: 1.1_
  
  - [x] 1.3 Implementar método getSocioDetalhado no SocioService
    - Usar query otimizada e mapear para SocioDetalhadoResponse
    - Ordenar pagamentos por data decrescente
    - Ordenar arquivos por data de upload decrescente
    - _Requirements: 1.1, 1.6, 1.7_
  
  - [x] 1.4 Adicionar endpoint GET /api/socios/{id}/detalhes no SocioController
    - Retornar 404 se sócio não encontrado
    - _Requirements: 1.1, 6.1_
  
  - [ ]* 1.5 Escrever testes unitários para getSocioDetalhado
    - Testar sócio existente com dados completos
    - Testar sócio não encontrado
    - Testar sócio sem pagamentos
    - Testar sócio sem arquivos
    - _Requirements: 1.9, 1.10_
  
  - [ ]* 1.6 Escrever teste de propriedade para ordenação de pagamentos
    - **Property 2: Ordenação de Pagamentos por Data**
    - **Validates: Requirements 1.6**

- [ ] 2. Implementar edição de sócios no backend
  - [x] 2.1 Criar DTO SocioUpdateRequest com validações Bean Validation
    - Adicionar @NotBlank, @Pattern, @Email, @Size conforme especificação
    - Validar formato CPF, telefone, CEP e email
    - _Requirements: 2.3, 2.4, 2.5, 2.6, 2.7_
  
  - [x] 2.2 Implementar método updateSocio no SocioService
    - Buscar sócio existente ou lançar ResourceNotFoundException
    - Aplicar alterações dos campos
    - Salvar no repositório
    - Registrar histórico via SocioHistoryService
    - _Requirements: 2.1, 2.9, 2.10_
  
  - [x] 2.3 Adicionar endpoint PUT /api/socios/{id} no SocioController
    - Validar request com @Valid
    - Capturar usuário autenticado via @AuthenticationPrincipal
    - Retornar 404 se sócio não encontrado
    - Retornar 400 para validações falhadas
    - Retornar 409 para conflitos de CPF/matrícula
    - _Requirements: 2.1, 2.8, 6.2, 6.5_
  
  - [ ]* 2.4 Escrever testes unitários para updateSocio
    - Testar atualização com dados válidos
    - Testar sócio não encontrado
    - Testar campos obrigatórios vazios
    - Testar CPF duplicado
    - Testar matrícula duplicada
    - _Requirements: 2.7, 2.8_
  
  - [ ]* 2.5 Escrever testes de propriedade para validações
    - **Property 4: Validação de CPF**
    - **Validates: Requirements 2.3**
  
  - [ ]* 2.6 Escrever teste de propriedade para validação de telefone
    - **Property 5: Validação de Telefone**
    - **Validates: Requirements 2.4**
  
  - [ ]* 2.7 Escrever teste de propriedade para validação de CEP
    - **Property 6: Validação de CEP**
    - **Validates: Requirements 2.5**
  
  - [ ]* 2.8 Escrever teste de propriedade para round-trip de atualização
    - **Property 9: Persistência de Atualização (Round-trip)**
    - **Validates: Requirements 2.9**

- [ ] 3. Implementar histórico de alterações no backend
  - [x] 3.1 Criar DTO HistoricoAlteracaoResponse e CampoAlterado
    - Incluir id, socioId, usuario, dataHora, operacao e camposAlterados
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  
  - [x] 3.2 Adicionar endpoint GET /api/socios/{id}/historico no SocioController
    - Buscar histórico via SocioHistoryService
    - Ordenar por data/hora decrescente
    - Mapear para HistoricoAlteracaoResponse
    - _Requirements: 5.5, 5.6_
  
  - [ ]* 3.3 Escrever teste de propriedade para registro completo de histórico
    - **Property 10: Registro Completo de Histórico**
    - **Validates: Requirements 2.10, 5.1, 5.2, 5.3, 5.4**
  
  - [ ]* 3.4 Escrever teste de propriedade para ordenação de histórico
    - **Property 20: Ordenação de Histórico de Alterações**
    - **Validates: Requirements 5.6**

- [x] 4. Checkpoint - Validar backend completo
  - Executar todos os testes do backend
  - Verificar se todos os endpoints estão funcionando
  - Perguntar ao usuário se há dúvidas ou ajustes necessários

- [ ] 5. Implementar componente de visualização detalhada no frontend
  - [x] 5.1 Criar SocioDetailComponent com template e estilos
    - Criar estrutura de seções: dados pessoais, endereço, contato, status
    - Adicionar seção de histórico de pagamentos
    - Adicionar seção de arquivos vinculados
    - Adicionar botões "Editar" e "Fechar"
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.2_
  
  - [x] 5.2 Implementar lógica do SocioDetailComponent
    - Carregar sócio detalhado via SocioService no ngOnInit
    - Implementar indicador de carregamento
    - Implementar tratamento de erro com mensagens específicas
    - Implementar navegação para edição
    - _Requirements: 1.1, 6.1, 6.3, 6.4, 6.6_
  
  - [x] 5.3 Adicionar método getSocioDetalhado no SocioService (frontend)
    - Fazer requisição GET para /api/socios/{id}/detalhes
    - Tratar erros HTTP com mensagens apropriadas
    - _Requirements: 1.1, 6.3_
  
  - [x] 5.4 Exibir mensagens informativas para listas vazias
    - "Nenhum pagamento registrado" quando não há pagamentos
    - "Nenhum arquivo vinculado" quando não há arquivos
    - _Requirements: 1.9, 1.10_
  
  - [ ]* 5.5 Escrever testes unitários para SocioDetailComponent
    - Testar carregamento de dados
    - Testar exibição de erro quando sócio não encontrado
    - Testar mensagens para listas vazias
    - Testar navegação para edição
  
  - [ ]* 5.6 Escrever teste de propriedade para renderização completa
    - **Property 1: Visualização Completa de Dados do Sócio**
    - **Validates: Requirements 1.2, 1.3, 1.4, 1.5**

- [ ] 6. Estender componente de edição de sócios no frontend
  - [x] 6.1 Adicionar validadores customizados no SocioFormComponent
    - Implementar validateCPF com regex brasileiro
    - Implementar validateTelefone com regex brasileiro
    - Implementar validateCEP com regex de 8 dígitos
    - Validar email com padrão RFC 5322
    - _Requirements: 2.3, 2.4, 2.5, 2.6_
  
  - [x] 6.2 Estender formulário reativo com todos os campos e validações
    - Adicionar campos de endereço completo (CEP, logradouro, número, complemento, bairro, cidade, estado)
    - Adicionar RG, data de nascimento, estado civil
    - Adicionar telefone, celular, email
    - Aplicar validadores em cada campo
    - _Requirements: 2.1, 2.2_
  
  - [x] 6.3 Implementar modo de edição no SocioFormComponent
    - Carregar dados do sócio existente quando socioId fornecido
    - Preencher formulário com dados atuais
    - _Requirements: 2.1_
  
  - [x] 6.4 Implementar método updateSocio no SocioService (frontend)
    - Fazer requisição PUT para /api/socios/{id}
    - Tratar erros de validação (400)
    - Tratar erro de não encontrado (404)
    - Tratar erro de conflito (409)
    - _Requirements: 2.9, 6.5_
  
  - [x] 6.5 Adicionar feedback visual de validação no template
    - Destacar campos inválidos com borda vermelha
    - Exibir mensagens de erro específicas abaixo de cada campo
    - Desabilitar botão de submit enquanto formulário inválido
    - Exibir indicador de carregamento durante salvamento
    - _Requirements: 2.7, 2.8, 6.7, 6.8, 6.9_
  
  - [x] 6.6 Implementar confirmação de sucesso e navegação
    - Exibir toast "Dados atualizados com sucesso"
    - Retornar à lista de sócios após salvamento
    - Implementar cancelamento com descarte de alterações
    - _Requirements: 2.11, 2.12, 2.13_
  
  - [ ]* 6.7 Escrever testes unitários para validadores customizados
    - Testar validateCPF com CPFs válidos e inválidos
    - Testar validateTelefone com telefones válidos e inválidos
    - Testar validateCEP com CEPs válidos e inválidos
  
  - [ ]* 6.8 Escrever testes de propriedade para validadores frontend
    - **Property 4: Validação de CPF** (frontend)
    - **Property 5: Validação de Telefone** (frontend)
    - **Property 6: Validação de CEP** (frontend)
    - **Property 7: Validação de Email** (frontend)
    - **Validates: Requirements 2.3, 2.4, 2.5, 2.6**

- [ ] 7. Implementar componente de gestão de arquivos no frontend
  - [x] 7.1 Criar ArquivoManagerComponent com template e estilos
    - Criar área de upload com seleção de arquivo
    - Criar lista de arquivos com nome, tipo, tamanho e data
    - Adicionar botões de visualizar, baixar e excluir para cada arquivo
    - _Requirements: 3.1, 3.9, 3.10, 3.11, 3.13_
  
  - [x] 7.2 Implementar validação de arquivo no frontend
    - Validar tipos permitidos: PDF, DOC, DOCX, JPG, JPEG, PNG
    - Validar tamanho máximo de 10MB
    - Exibir mensagens de erro específicas
    - _Requirements: 3.2, 3.3, 3.4, 3.5_
  
  - [x] 7.3 Implementar upload de arquivo
    - Criar FormData com arquivo e socioId
    - Fazer requisição POST para /api/arquivos
    - Exibir indicador de carregamento
    - Atualizar lista após upload bem-sucedido
    - Tratar erros de upload
    - _Requirements: 3.6, 3.7, 3.8, 3.19_
  
  - [x] 7.4 Implementar visualização e download de arquivos
    - Implementar método para visualizar arquivo no navegador (PDF, imagens)
    - Implementar método para download com nome original
    - _Requirements: 3.10, 3.11, 3.12_
  
  - [x] 7.5 Implementar exclusão de arquivo
    - Exibir dialog de confirmação antes de excluir
    - Fazer requisição DELETE para /api/arquivos/{id}
    - Remover arquivo da lista após exclusão bem-sucedida
    - Exibir mensagem de confirmação
    - Tratar erros de exclusão
    - _Requirements: 3.13, 3.14, 3.15, 3.16, 3.17, 3.20_
  
  - [x] 7.6 Implementar ordenação e formatação
    - Ordenar arquivos por data de upload decrescente
    - Implementar método formatFileSize para exibir tamanho legível
    - _Requirements: 3.18_
  
  - [ ]* 7.7 Escrever testes unitários para ArquivoManagerComponent
    - Testar validação de tipo de arquivo
    - Testar validação de tamanho de arquivo
    - Testar exibição de dialog de confirmação
    - Testar formatação de tamanho de arquivo
  
  - [ ]* 7.8 Escrever testes de propriedade para validação de arquivos
    - **Property 11: Validação de Tipo de Arquivo**
    - **Property 12: Validação de Tamanho de Arquivo**
    - **Validates: Requirements 3.1, 3.2, 3.3**

- [ ] 8. Integrar componentes no frontend
  - [x] 8.1 Adicionar ArquivoManagerComponent dentro do SocioDetailComponent
    - Passar socioId como input
    - Exibir na seção de arquivos
    - _Requirements: 4.2_
  
  - [x] 8.2 Implementar navegação entre visualização e edição
    - Adicionar botão "Editar" no SocioDetailComponent que navega para SocioFormComponent
    - Adicionar botão "Voltar" no SocioFormComponent que retorna ao SocioDetailComponent
    - _Requirements: 4.1, 4.3_
  
  - [x] 8.3 Implementar atualização automática após mudanças
    - Recarregar dados do sócio após retornar da edição
    - Recarregar lista de arquivos após upload/exclusão
    - _Requirements: 4.4, 4.5_
  
  - [x] 8.4 Garantir manutenção de contexto do sócio
    - Passar socioId via route params
    - Manter socioId consistente em todos os componentes
    - _Requirements: 4.6_
  
  - [ ]* 8.5 Escrever testes de integração para navegação
    - Testar fluxo completo: lista → visualização → edição → visualização → lista
    - Testar manutenção de contexto do sócio
  
  - [ ]* 8.6 Escrever testes de propriedade para consistência
    - **Property 17: Consistência de Dados Após Atualização**
    - **Property 18: Consistência de Lista de Arquivos**
    - **Property 19: Manutenção de Contexto do Sócio**
    - **Validates: Requirements 4.4, 4.5, 4.6**

- [ ] 9. Implementar visualização de histórico de alterações no frontend
  - [x] 9.1 Criar componente ou seção para exibir histórico
    - Criar lista de alterações com data/hora, usuário e campos modificados
    - Exibir valores anteriores e novos para cada campo
    - Ordenar por data/hora decrescente
    - _Requirements: 5.5, 5.6_
  
  - [x] 9.2 Adicionar método getHistoricoAlteracoes no SocioService (frontend)
    - Fazer requisição GET para /api/socios/{id}/historico
    - _Requirements: 5.5_
  
  - [x] 9.3 Integrar histórico no SocioDetailComponent
    - Adicionar seção de histórico de alterações
    - Carregar histórico junto com dados do sócio
    - Exibir mensagem "Nenhuma alteração registrada" quando vazio
    - _Requirements: 5.5, 5.7_
  
  - [ ]* 9.4 Escrever testes unitários para exibição de histórico
    - Testar carregamento de histórico
    - Testar exibição de mensagem quando vazio
    - Testar ordenação por data decrescente

- [ ] 10. Adicionar tratamento de erros e acessibilidade
  - [x] 10.1 Implementar tratamento global de erros HTTP no frontend
    - Interceptor para erros de rede (status 0)
    - Tratamento para erros 500+
    - Mensagens de erro descritivas
    - _Requirements: 6.3, 6.4_
  
  - [x] 10.2 Adicionar atributos ARIA para acessibilidade
    - Labels descritivos em todos os campos
    - aria-label, aria-required, aria-invalid nos inputs
    - aria-describedby para mensagens de erro
    - role="alert" para mensagens de erro
    - _Requirements: 6.8, 6.9_
  
  - [x] 10.3 Garantir navegação por teclado
    - Testar navegação com Tab
    - Garantir foco visível
    - Suporte para Enter/Escape em dialogs
  
  - [ ]* 10.4 Escrever testes de acessibilidade
    - Verificar presença de labels
    - Verificar atributos ARIA
    - Testar navegação por teclado

- [x] 11. Checkpoint final - Testes end-to-end
  - Executar todos os testes do backend e frontend
  - Testar fluxo completo manualmente: visualizar → editar → salvar → verificar histórico → gerenciar arquivos
  - Verificar mensagens de erro e validações
  - Verificar acessibilidade básica
  - Perguntar ao usuário se há ajustes necessários

- [ ] 12. Integração final e documentação
  - [x] 12.1 Atualizar rotas do Angular
    - Adicionar rota para /socios/:id/detalhes
    - Adicionar rota para /socios/:id/editar
    - _Requirements: 4.1, 4.7_
  
  - [x] 12.2 Atualizar lista de sócios com novos botões
    - Adicionar botão "Visualizar" que navega para detalhes
    - Manter botão "Editar" existente
    - _Requirements: 1.1, 2.1_
  
  - [x] 12.3 Verificar integração com sistema de autenticação
    - Garantir que JWT é enviado em todas as requisições
    - Verificar que usuário autenticado é capturado no backend
    - _Requirements: 2.10, 5.2_
  
  - [ ]* 12.4 Executar testes de integração completos
    - Testar fluxo completo com autenticação
    - Verificar registro de histórico com usuário correto
    - Verificar upload e exclusão de arquivos

## Notas

- Tasks marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- Cada task referencia requisitos específicos para rastreabilidade
- Checkpoints garantem validação incremental
- Testes de propriedade validam correção universal
- Testes unitários validam exemplos específicos e casos extremos
- A implementação segue uma abordagem incremental: backend → frontend → integração
