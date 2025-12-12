# Configuração de Variáveis de Ambiente

Este projeto utiliza variáveis de ambiente para configurações sensíveis. Siga os passos abaixo para configurar seu ambiente local.

## Setup Local

1. **Copie o arquivo de exemplo:**
   ```bash
   cp src/main/resources/.env.example src/main/resources/.env
   ```

2. **Edite o arquivo `.env` com suas credenciais:**
   ```bash
   nano src/main/resources/.env
   # ou use seu editor preferido
   ```

3. **Configure as variáveis necessárias:**

   ### Database (PostgreSQL)
   - `PG_HOST`: Host do banco de dados (padrão: localhost)
   - `PG_PORT`: Porta do PostgreSQL (padrão: 5432)
   - `PG_DATABASE`: Nome do banco de dados
   - `PG_USER`: Usuário do banco
   - `PG_PASSWORD`: Senha do banco

   ### JWT
   - `JWT_SECRET`: Chave secreta para assinar tokens JWT (mínimo 256 bits)

   ### Google API
   - `APIKEY`: Chave da API do Google Maps Geocoding

   ### Email (SMTP)
   - `SPRING_MAIL_HOST`: Servidor SMTP (ex: smtp.gmail.com)
   - `SPRING_MAIL_PORT`: Porta SMTP (587 para TLS, 465 para SSL)
   - `SPRING_MAIL_USERNAME`: Seu email
   - `SPRING_MAIL_PASSWORD`: Senha de aplicativo do Gmail
   - `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`: true
   - `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE`: true

   **Nota para Gmail:** 
   - Você precisa gerar uma "Senha de App" em vez de usar sua senha normal
   - Acesse: https://myaccount.google.com/apppasswords
   - A senha de app não tem espaços, mas o Gmail a mostra com espaços para legibilidade

## Produção

Em produção, defina as variáveis de ambiente diretamente no servidor/container, não use o arquivo `.env`.

### Docker
```bash
docker run -e PG_HOST=db -e PG_USER=prod_user -e JWT_SECRET=xxx ...
```

### Docker Compose
```yaml
environment:
  - PG_HOST=db
  - PG_USER=prod_user
  - JWT_SECRET=${JWT_SECRET}
```

### Kubernetes
Use ConfigMaps e Secrets:
```yaml
envFrom:
  - configMapRef:
      name: app-config
  - secretRef:
      name: app-secrets
```
