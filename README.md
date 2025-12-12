# Contacts API - Backend

API REST para gerenciamento de contatos com autenticação JWT, desenvolvida como parte do teste técnico dev.

## 📋 Sobre o Projeto

Sistema de gerenciamento de contatos que permite aos usuários cadastrar, editar, visualizar e excluir seus contatos. Cada contato inclui informações pessoais e de endereço, com busca automática de endereços via ViaCEP e geolocalização via Google Geocoding API.

### Funcionalidades Principais

- ✅ Autenticação e autorização com JWT
- ✅ CRUD completo de contatos
- ✅ Busca de endereços por CEP (ViaCEP)
- ✅ Busca de endereços por UF/Cidade/Rua
- ✅ Geolocalização automática (Google Geocoding)
- ✅ Filtro e paginação de contatos
- ✅ Validação de CPF único por usuário
- ✅ Gerenciamento de perfil de usuário
- ✅ Documentação interativa com Swagger/OpenAPI

## 🛠 Tecnologias Utilizadas

### Core
- **Java 21** - Linguagem de programação
- **Spring Boot 4.0.0** - Framework principal
- **Maven** - Gerenciamento de dependências

### Dependências Principais
- **Spring Web MVC** - API REST
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados
- **JWT (jjwt 0.11.5)** - Tokens de autenticação
- **Flyway** - Migrations de banco de dados
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI 3.0.0** - Documentação da API
- **Bean Validation** - Validação de dados

### Testes
- **Spring Boot Test** - Testes unitários e de integração
- **Spring Security Test** - Testes de segurança
- **H2 Database** - Banco em memória para testes
- **JaCoCo** - Cobertura de código

## 🏗 Arquitetura

O projeto segue uma arquitetura em camadas:

```
src/main/java/com/dev/contacts/
├── config/              # Configurações (Security, JWT, CORS, OpenAPI)
├── controller/          # Endpoints REST
├── service/            # Lógica de negócio
├── repository/         # Acesso a dados (JPA)
├── entity/             # Entidades JPA
├── dto/                # Data Transfer Objects
├── integration/        # Clientes de APIs externas
├── exception/          # Exceções customizadas e handlers
└── ContactsApplication.java
```

### Entidades

#### User (Usuário)
- `id` - Identificador único
- `fullName` - Nome completo
- `email` - Email (único)
- `passwordHash` - Senha criptografada
- `createdAt` / `updatedAt` - Timestamps
- Relacionamento: Um usuário tem muitos contatos

#### Contact (Contato)
- `id` - Identificador único
- `name` - Nome do contato
- `cpf` - CPF (único por usuário)
- `phone` - Telefone
- Endereço:
  - `cep`, `state`, `city`, `street`
  - `number`, `complement`, `neighborhood`
  - `latitude`, `longitude` - Coordenadas geográficas
- `createdAt` / `updatedAt` - Timestamps
- Relacionamento: Pertence a um usuário

## 🚀 Começando

### Pré-requisitos

- Java 21+
- Maven 3.8+
- PostgreSQL 12+
- Conta Google Cloud (para Google Geocoding API)

### Configuração do Ambiente

1. **Clone o repositório**
```bash
git clone <repository-url>
cd contacts
```

2. **Configure o banco de dados PostgreSQL**
```sql
CREATE DATABASE contacts;
CREATE USER dev WITH PASSWORD 'dev';
GRANT ALL PRIVILEGES ON DATABASE contacts TO dev;
```

3. **Configure as variáveis de ambiente**

Crie um arquivo `application-local.yml` em `src/main/resources/`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/contacts
    username: dev
    password: dev

jwt:
  secret: "seu-secret-jwt-seguro-aqui"
  expiration: 3600000  # 1 hora em milissegundos

address:
  google:
    api-key: "sua-google-api-key-aqui"
```

4. **Execute as migrations**
```bash
mvn flyway:migrate
```

5. **Execute a aplicação**
```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

A API estará disponível em `http://localhost:8080`

## 📚 Documentação da API

### Swagger UI

Acesse a documentação interativa em:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

Especificação OpenAPI disponível em:
```
http://localhost:8080/v3/api-docs
```

### Endpoints Principais

#### Autenticação (`/api/auth`)

**POST** `/api/auth/signup` - Registro de novo usuário
```json
{
  "fullName": "João Silva",
  "email": "joao@example.com",
  "password": "senha123"
}
```

**POST** `/api/auth/login` - Login
```json
{
  "email": "joao@example.com",
  "password": "senha123"
}
```
Retorna:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "expiresIn": 3600000
}
```

#### Usuários (`/api/users`)

**GET** `/api/users/me` - Perfil do usuário autenticado

**PUT** `/api/users` - Atualizar perfil

**DELETE** `/api/users` - Deletar conta

#### Contatos (`/api/contacts`)

**POST** `/api/contacts` - Criar contato
```json
{
  "name": "Maria Santos",
  "cpf": "123.456.789-00",
  "phone": "(11) 98765-4321",
  "cep": "01310-100",
  "state": "SP",
  "city": "São Paulo",
  "street": "Avenida Paulista",
  "number": "1000",
  "complement": "Apto 101",
  "neighborhood": "Bela Vista"
}
```

**GET** `/api/contacts` - Listar contatos (com paginação e filtro)
- Query params: `filter`, `page`, `size`, `sort`

**GET** `/api/contacts/{id}` - Buscar contato por ID

**PUT** `/api/contacts/{id}` - Atualizar contato

**DELETE** `/api/contacts/{id}` - Deletar contato

#### Busca de Endereços (`/api/address`)

**GET** `/api/address/cep/{cep}` - Buscar endereço por CEP
```
GET /api/address/cep/01310-100
```

**GET** `/api/address/search?uf={uf}&city={city}&street={street}` - Buscar endereços
```
GET /api/address/search?uf=SP&city=São Paulo&street=Paulista
```

**GET** `/api/address/geocode?address={address}` - Obter coordenadas
```
GET /api/address/geocode?address=Avenida Paulista, 1000, São Paulo, SP
```

#### Health Check (`/api/health`)

**GET** `/api/health` - Verificar status da API

## 🔒 Segurança

### Autenticação JWT

A API utiliza JWT (JSON Web Tokens) para autenticação. Após o login, inclua o token no header de todas as requisições:

```
Authorization: Bearer {seu-token-jwt}
```

### CORS

O CORS está configurado para aceitar requisições das seguintes origens:
- `https://front-contacts.vercel.app` (produção)
- `http://localhost:3000` (desenvolvimento React)
- `http://localhost:4200` (desenvolvimento Angular)

### Senhas

As senhas são criptografadas usando BCrypt antes de serem armazenadas no banco de dados.

## 🧪 Testes

### Executar todos os testes
```bash
mvn test
```

### Executar com relatório de cobertura
```bash
mvn test jacoco:report
```

O relatório HTML estará disponível em: `target/site/jacoco/index.html`

### Estrutura de Testes

- **Unit Tests**: Testes de serviços e componentes isolados
- **Integration Tests**: Testes de controllers com Spring Boot Test
- **Repository Tests**: Testes de persistência com banco H2 em memória

Principais classes de teste:
- `AuthServiceTest` - Testes de autenticação
- `ContactServiceTest` - Testes de lógica de contatos
- `UserServiceTest` - Testes de gerenciamento de usuários
- `ContactControllerTest` - Testes de endpoints de contatos
- `AddressLookupControllerTest` - Testes de busca de endereços

## 🗄 Banco de Dados

### Migrations (Flyway)

As migrations estão em `src/main/resources/db/migration/`:

- `V1__create_users_and_contacts.sql` - Criação das tabelas iniciais
- `V2__rename_user_id_to_owner_id.sql` - Renomeação de coluna

### Schema

```sql
-- Tabela users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Tabela contacts
CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    cpf VARCHAR(20) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    state VARCHAR(2) NOT NULL,
    city VARCHAR(100) NOT NULL,
    street VARCHAR(200) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_contact_user FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT uk_user_cpf UNIQUE (owner_id, cpf)
);
```

## 🔧 Configuração

### application.yml

Configurações principais:

```yaml
spring:
  application:
    name: contacts
  
  datasource:
    url: jdbc:postgresql://localhost:5432/contacts
    username: dev
    password: dev
  
  jpa:
    hibernate:
      ddl-auto: none  # Flyway gerencia o schema
    show-sql: false

jwt:
  secret: "troque-por-um-secret-real"
  expiration: 3600000  # 1 hora

cors:
  allowed-origins: "https://front-contacts.vercel.app,http://localhost:3000"

server:
  port: 8080
```

### Profiles

- **default**: Configuração padrão (application.yml)
- **test**: Configuração para testes (application-test.yml) - usa H2
- **local**: Configuração para desenvolvimento local (criar manualmente)

## 📦 Build e Deploy

### Build da aplicação
```bash
mvn clean package
```

O arquivo JAR será gerado em: `target/contacts-0.0.14-SNAPSHOT.jar`

### Executar JAR
```bash
java -jar target/contacts-0.0.14-SNAPSHOT.jar
```

### Build com Docker (exemplo)
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 🔍 Integrações Externas

### ViaCEP
- **Uso**: Busca de endereços por CEP
- **Documentação**: https://viacep.com.br/
- **Não requer autenticação**

### Google Geocoding API
- **Uso**: Obtenção de coordenadas geográficas (latitude/longitude)
- **Documentação**: https://developers.google.com/maps/documentation/geocoding
- **Requer**: Google Cloud API Key

## 🐛 Troubleshooting

### Erro de conexão com PostgreSQL
- Verifique se o PostgreSQL está rodando: `sudo systemctl status postgresql`
- Confirme as credenciais em `application.yml`
- Teste a conexão: `psql -U dev -d contacts -h localhost`

### JWT Token inválido
- Verifique se o secret está configurado corretamente
- Confirme que o token não expirou (padrão: 1 hora)
- Certifique-se de incluir "Bearer " antes do token

### Erro ao buscar endereços
- **ViaCEP**: Verifique se o CEP é válido (8 dígitos)
- **Google Geocoding**: Confirme se a API Key está configurada e ativa
- Verifique os logs para mensagens de erro detalhadas

## 📄 Licença

Este projeto foi desenvolvido como parte de um teste técnico para a dev.

## 👥 Autor

Desenvolvido por Luis Klap para o teste técnico dev - Backend.

## 📞 Suporte

Para dúvidas ou problemas:
- Crie uma issue no repositório
- Entre em contato através do email fornecido no processo seletivo

---

**Versão atual**: 0.0.14-SNAPSHOT
**Data**: Dezembro de 2025
