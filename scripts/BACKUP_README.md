# Sistema de Backup e Auditoria

Este documento descreve o sistema de backup automático e auditoria implementado para o Sistema de Gerenciamento do Sindicato Rural.

## Funcionalidades Implementadas

### 1. Backup Automático do PostgreSQL

O sistema inclui scripts para backup automático diário do banco de dados PostgreSQL.

#### Scripts Disponíveis

**Linux/Unix:**
- `backup-database.sh` - Executa backup do banco de dados
- `restore-database.sh` - Restaura um backup específico
- `setup-backup-cron.sh` - Configura backup automático diário via cron

**Windows:**
- `backup-database.bat` - Executa backup do banco de dados

#### Configuração do Backup Automático (Linux)

1. Tornar os scripts executáveis:
```bash
chmod +x scripts/*.sh
```

2. Configurar variáveis de ambiente (opcional):
```bash
export POSTGRES_DB=sindicato_rural
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=sua_senha
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
```

3. Executar configuração do cron:
```bash
./scripts/setup-backup-cron.sh
```

Isso configurará um backup automático diário às 2:00 AM.

#### Executar Backup Manual

**Linux:**
```bash
./scripts/backup-database.sh
```

**Windows:**
```cmd
scripts\backup-database.bat
```

#### Restaurar Backup

**Linux:**
```bash
./scripts/restore-database.sh /var/backups/sindicato-rural/backup_sindicato_rural_20240101_020000.sql.gz
```

#### Localização dos Backups

- **Linux:** `/var/backups/sindicato-rural/`
- **Windows:** `C:\backups\sindicato-rural\`

#### Retenção de Backups

Os backups são mantidos por **30 dias** por padrão. Backups mais antigos são automaticamente removidos.

Para alterar o período de retenção, edite a variável `RETENTION_DAYS` nos scripts.

### 2. Sistema de Auditoria

O sistema implementa auditoria completa de todas as operações CRUD nas entidades principais.

#### Entidades Auditadas

- **Socio** - Cadastro de sócios
- **Pagamento** - Registro de pagamentos
- **Arquivo** - Upload e exclusão de arquivos
- **Usuario** - Gerenciamento de usuários

#### Informações Registradas

Para cada operação, o sistema registra:
- Tipo de operação (CREATE, UPDATE, DELETE)
- Entidade afetada
- ID do registro
- Usuário que executou a operação
- Dados anteriores (para UPDATE e DELETE)
- Dados novos (para CREATE e UPDATE)
- Endereço IP do cliente
- User-Agent do navegador
- Data e hora da operação

#### Endpoints de Auditoria

Todos os endpoints requerem autenticação de administrador.

**Buscar logs por entidade:**
```
GET /api/audit/entidade/{entidade}?page=0&size=20
```

**Buscar logs por entidade e ID:**
```
GET /api/audit/entidade/{entidade}/{id}?page=0&size=20
```

**Buscar logs por usuário:**
```
GET /api/audit/usuario/{usuario}?page=0&size=20
```

**Buscar logs por período:**
```
GET /api/audit/periodo?inicio=2024-01-01T00:00:00&fim=2024-12-31T23:59:59&page=0&size=20
```

**Buscar logs por entidade e período:**
```
GET /api/audit/entidade/{entidade}/periodo?inicio=2024-01-01T00:00:00&fim=2024-12-31T23:59:59&page=0&size=20
```

#### Exemplo de Resposta de Auditoria

```json
{
  "content": [
    {
      "id": 1,
      "entidade": "Socio",
      "entidadeId": 123,
      "operacao": "CREATE",
      "usuario": "admin",
      "dadosAnteriores": null,
      "dadosNovos": "{\"nome\":\"João Silva\",\"cpf\":\"123.456.789-00\"}",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "criadoEm": "2024-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 3. Relatórios de Atividade

O sistema gera relatórios consolidados de atividade do sistema.

#### Endpoint de Relatório

```
GET /api/audit/relatorio?inicio=2024-01-01T00:00:00&fim=2024-12-31T23:59:59
```

#### Exemplo de Resposta de Relatório

```json
{
  "periodoInicio": "2024-01-01T00:00:00",
  "periodoFim": "2024-12-31T23:59:59",
  "totalOperacoes": 1250,
  "operacoesPorEntidade": {
    "Socio": 450,
    "Pagamento": 600,
    "Arquivo": 150,
    "Usuario": 50
  },
  "operacoesPorTipo": {
    "CREATE": 800,
    "UPDATE": 350,
    "DELETE": 100
  },
  "operacoesPorUsuario": {
    "admin": 800,
    "usuario1": 300,
    "usuario2": 150
  },
  "totalSociosCriados": 200,
  "totalPagamentosRegistrados": 500,
  "totalArquivosUpload": 120
}
```

## Integração com Serviços

O sistema de auditoria está integrado automaticamente com os seguintes serviços:

- **SocioService** - Registra criação, atualização e exclusão de sócios
- **PagamentoService** - Registra criação e cancelamento de pagamentos
- **ArquivoService** - Registra upload e exclusão de arquivos

A auditoria é executada de forma **assíncrona** para não impactar a performance das operações principais.

## Considerações de Performance

### Auditoria
- Logs de auditoria são salvos de forma assíncrona
- Usa transação separada (REQUIRES_NEW) para garantir persistência mesmo em caso de falha
- Índices otimizados para consultas frequentes

### Backup
- Backups são executados durante a madrugada (2:00 AM) para minimizar impacto
- Compressão gzip reduz espaço de armazenamento
- Limpeza automática de backups antigos

## Segurança

- Todos os endpoints de auditoria requerem autenticação de administrador
- Senhas do banco de dados devem ser configuradas via variáveis de ambiente
- Backups devem ser armazenados em local seguro com acesso restrito
- Logs de auditoria incluem IP e User-Agent para rastreabilidade

## Monitoramento

### Verificar Logs de Backup (Linux)
```bash
tail -f /var/log/sindicato-rural-backup.log
```

### Verificar Espaço em Disco
```bash
du -sh /var/backups/sindicato-rural/
```

### Verificar Últimos Backups
```bash
ls -lht /var/backups/sindicato-rural/ | head -10
```

## Troubleshooting

### Backup Falha com Erro de Permissão
- Verificar permissões do diretório de backup
- Verificar se o usuário tem permissão para executar pg_dump

### Auditoria Não Está Registrando
- Verificar se o AuditService está sendo injetado corretamente
- Verificar logs da aplicação para erros
- Verificar se a tabela audit_log foi criada (migration V5)

### Restauração Falha
- Verificar se o arquivo de backup existe e não está corrompido
- Verificar credenciais do banco de dados
- Verificar se há espaço em disco suficiente

## Requisitos Atendidos

Este sistema atende aos seguintes requisitos:

- **Requisito 6.3**: Backup automático dos dados críticos diariamente
- **Requisito 2.3**: Preservação de histórico de alterações
- **Requisito 3.5**: Manutenção de registro de operações
- **Requisito 5.5**: Registro de operações de exclusão de arquivos
