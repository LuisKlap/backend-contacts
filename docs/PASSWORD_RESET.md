# Serviço de Recuperação de Senha (Forgot Password)

## Visão Geral

O serviço de recuperação de senha permite que usuários que esqueceram suas credenciais possam redefinir a senha através de um link enviado por email.

## Funcionalidades

### 1. Solicitar Recuperação de Senha
- **Endpoint**: `POST /api/auth/forgot-password`
- **Autenticação**: Não requerida (endpoint público)
- **Descrição**: Envia um email com link de recuperação para o usuário

#### Request Body
```json
{
  "email": "usuario@exemplo.com"
}
```

#### Response (200 OK)
```json
{
  "message": "Email de recuperação de senha enviado com sucesso",
  "email": "usuario@exemplo.com"
}
```

#### Erros Possíveis
- `404 Not Found`: Usuário não encontrado com o email fornecido
- `500 Internal Server Error`: Falha ao enviar email

### 2. Redefinir Senha
- **Endpoint**: `POST /api/auth/reset-password`
- **Autenticação**: Não requerida (endpoint público)
- **Descrição**: Redefine a senha usando o token recebido por email

#### Request Body
```json
{
  "token": "uuid-token-recebido-por-email",
  "newPassword": "novaSenhaSegura123"
}
```

#### Response (200 OK)
```json
{
  "message": "Senha alterada com sucesso"
}
```

#### Erros Possíveis
- `400 Bad Request`: Token inválido, expirado ou já utilizado
- `400 Bad Request`: Senha não atende aos requisitos (mínimo 6 caracteres)

## Fluxo de Funcionamento

1. **Usuário solicita recuperação**
   - Acessa o formulário de "Esqueci minha senha"
   - Informa o email cadastrado
   - Sistema gera um token único (UUID)

2. **Sistema processa solicitação**
   - Valida se o email existe no banco
   - Invalida tokens anteriores não utilizados do mesmo usuário
   - Cria novo token com validade de 30 minutos (configurável)
   - Envia email com link de recuperação

3. **Usuário recebe email**
   - Email contém link: `{frontend-url}/reset-password?token={uuid}`
   - Token válido por 30 minutos
   - Link é de uso único

4. **Usuário redefine senha**
   - Clica no link e é direcionado ao formulário
   - Informa nova senha
   - Sistema valida token (não expirado, não utilizado)
   - Atualiza senha no banco (hash bcrypt)
   - Marca token como utilizado

## Estrutura de Dados

### Tabela: password_reset_tokens
```sql
- id (BIGSERIAL PRIMARY KEY)
- token (VARCHAR 255, UNIQUE)
- user_id (BIGINT, FK para users)
- created_at (TIMESTAMP WITH TIME ZONE)
- expires_at (TIMESTAMP WITH TIME ZONE)
- used (BOOLEAN, default false)
- used_at (TIMESTAMP WITH TIME ZONE)
```

### Índices
- `idx_password_reset_token`: Otimiza busca por token
- `idx_password_reset_user_id`: Otimiza busca por usuário

## Configurações

### application.yml
```yaml
app:
  frontend:
    url: "http://localhost:3000"  # URL do frontend para construir o link
  password-reset:
    token-expiration-minutes: 30  # Tempo de validade do token
```

## Segurança

### Medidas Implementadas
1. **Token Único**: UUID aleatório de 36 caracteres
2. **Expiração**: Tokens expiram após tempo configurável (default 30 min)
3. **Uso Único**: Token só pode ser usado uma vez
4. **Invalidação**: Tokens anteriores são invalidados ao solicitar novo
5. **Hash de Senha**: Nova senha é armazenada com BCrypt
6. **Sem Informação Sensível**: Não revela se email existe (por segurança, retorna sucesso sempre)

### Proteção contra Ataques
- **Força Bruta**: Token aleatório impossível de adivinhar
- **Replay Attack**: Token marcado como usado após redefinição
- **Time-based Attack**: Expiração automática dos tokens

## Manutenção

### Limpeza de Tokens Expirados
O serviço fornece método para limpeza de tokens expirados:

```java
passwordResetService.cleanupExpiredTokens();
```

**Recomendação**: Agendar job para executar periodicamente (ex: diariamente)

### Exemplo com Spring Scheduling
```java
@Scheduled(cron = "0 0 2 * * *") // Todo dia às 2h da manhã
public void cleanupExpiredTokens() {
    passwordResetService.cleanupExpiredTokens();
}
```

## Exemplo de Email Enviado

```
Olá João Silva,

Você solicitou a recuperação de senha para sua conta no Sistema de Contatos.

Para criar uma nova senha, clique no link abaixo:
http://localhost:3000/reset-password?token=a1b2c3d4-e5f6-7890-abcd-ef1234567890

Este link é válido por 30 minutos.

Se você não solicitou esta recuperação, ignore este email. Sua senha permanecerá inalterada.

Atenciosamente,
Equipe Sistema de Contatos
```

## Testes

### Testando Localmente

1. **Solicitar recuperação de senha**
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "usuario@exemplo.com"}'
```

2. **Redefinir senha**
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "token-recebido-por-email",
    "newPassword": "novaSenha123"
  }'
```

## Observações Importantes

1. **Email em Produção**: Configure corretamente o SMTP no `application.yml` para ambiente produtivo
2. **URL do Frontend**: Ajuste a configuração `app.frontend.url` para o domínio correto em produção
3. **Tempo de Expiração**: Considere ajustar `token-expiration-minutes` conforme necessidade do negócio
4. **Rate Limiting**: Recomenda-se implementar rate limiting para evitar abuso da funcionalidade

## Componentes Criados

- `PasswordResetToken.java` - Entidade JPA
- `PasswordResetTokenRepository.java` - Repositório Spring Data
- `PasswordResetService.java` - Lógica de negócio
- `ForgotPasswordRequest.java` - DTO de requisição
- `ForgotPasswordResponse.java` - DTO de resposta
- `ResetPasswordRequest.java` - DTO de requisição
- `ResetPasswordResponse.java` - DTO de resposta
- `AuthController.java` - Endpoints adicionados
- `V4__create_password_reset_tokens_table.sql` - Migração Flyway
