# Sistema de Gerenciamento do Sindicato Rural

Sistema web completo para gerenciamento de sindicato de trabalhadores rurais, desenvolvido com Spring Boot (backend), Angular (frontend) e PostgreSQL (banco de dados).

## Funcionalidades

- **Autenticação Administrativa**: Login seguro com JWT
- **Gestão de Sócios**: Cadastro, edição e consulta de trabalhadores rurais
- **Controle de Pagamentos**: Registro e acompanhamento de contribuições mensais
- **Geração de Recibos**: Emissão automática de comprovantes em PDF
- **Gerenciamento de Arquivos**: Upload e organização de documentos por sócio
- **Interface Responsiva**: Acesso via web em diferentes dispositivos

## Tecnologias

### Backend
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados principal
- **JWT (jjwt)** - Tokens de autenticação
- **Maven** - Gerenciamento de dependências

### Frontend (a ser implementado)
- **Angular** - Framework frontend
- **Angular Material** - Componentes UI
- **TypeScript** - Linguagem principal

## Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- PostgreSQL 12 ou superior
- Node.js 18 ou superior (para o frontend)

## Configuração do Ambiente

### 1. Banco de Dados PostgreSQL

Crie o banco de dados de desenvolvimento:

```sql
CREATE DATABASE sindicato_rural_dev;
CREATE USER sindicato_user WITH PASSWORD 'sindicato_pass';
GRANT ALL PRIVILEGES ON DATABASE sindicato_rural_dev TO sindicato_user;
```

### 2. Variáveis de Ambiente

Configure as seguintes variáveis de ambiente (opcionais para desenvolvimento):

```bash
# Banco de dados
export DB_USERNAME=sindicato_user
export DB_PASSWORD=sindicato_pass
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=sindicato_rural_dev

# JWT
export JWT_SECRET=sua-chave-secreta-super-segura

# Arquivos
export FILE_UPLOAD_DIR=./uploads
```

## Executando a Aplicação

### Desenvolvimento

```bash
# Clone o repositório
git clone <repository-url>
cd sistema-sindicato-rural

# Execute a aplicação
mvn spring-boot:run

# Ou compile e execute
mvn clean package
java -jar target/sistema-sindicato-rural-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em: http://localhost:8080

### Perfis de Execução

- **dev** (padrão): Desenvolvimento local com PostgreSQL
- **prod**: Produção com configurações otimizadas
- **test**: Testes com banco H2 em memória

```bash
# Executar em produção
java -jar -Dspring.profiles.active=prod target/sistema-sindicato-rural-0.0.1-SNAPSHOT.jar
```

## Endpoints da API

### Health Check
- `GET /api/health` - Status da aplicação

### Autenticação (a ser implementado)
- `POST /api/auth/login` - Login de administrador
- `POST /api/auth/refresh` - Renovação de token

### Sócios (a ser implementado)
- `GET /api/socios` - Listar sócios
- `POST /api/socios` - Criar sócio
- `GET /api/socios/{id}` - Buscar sócio
- `PUT /api/socios/{id}` - Atualizar sócio
- `DELETE /api/socios/{id}` - Excluir sócio

### Pagamentos (a ser implementado)
- `GET /api/pagamentos` - Listar pagamentos
- `POST /api/pagamentos` - Registrar pagamento
- `DELETE /api/pagamentos/{id}` - Cancelar pagamento
- `GET /api/pagamentos/{id}/recibo` - Gerar recibo PDF

### Arquivos (a ser implementado)
- `POST /api/arquivos/upload/{socioId}` - Upload de arquivos
- `GET /api/arquivos/socio/{socioId}` - Listar arquivos do sócio
- `GET /api/arquivos/{id}/download` - Download de arquivo
- `DELETE /api/arquivos/{id}` - Excluir arquivo

## Testes

```bash
# Executar todos os testes
mvn test

# Executar testes com relatório de cobertura
mvn test jacoco:report

# Executar apenas testes unitários
mvn test -Dtest="*Test"

# Executar apenas testes de integração
mvn test -Dtest="*IT"
```

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/sindicato/
│   │   ├── config/          # Configurações da aplicação
│   │   ├── controller/      # Controllers REST
│   │   ├── service/         # Serviços de negócio
│   │   ├── repository/      # Repositórios JPA
│   │   ├── entity/          # Entidades JPA
│   │   ├── dto/             # DTOs de request/response
│   │   ├── security/        # Configurações de segurança
│   │   └── exception/       # Tratamento de exceções
│   └── resources/
│       ├── application.yml  # Configurações da aplicação
│       └── db/migration/    # Scripts de migração Flyway
└── test/
    ├── java/com/sindicato/  # Testes unitários e de integração
    └── resources/
        └── application-test.yml # Configurações de teste
```

## Próximos Passos

1. ✅ Configuração inicial do Spring Boot
2. 🔄 Implementação do sistema de autenticação
3. 📋 Desenvolvimento do módulo de gestão de sócios
4. 💰 Implementação do controle de pagamentos
5. 📄 Sistema de geração de recibos
6. 📁 Gerenciamento de arquivos
7. 🎨 Desenvolvimento do frontend Angular
8. 🚀 Deploy e configuração de produção

## Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.