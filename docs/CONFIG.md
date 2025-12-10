# Documentação - Pacote Config

Este documento detalha todas as classes de configuração do projeto, localizadas em `src/main/java/com/dev/contacts/config/`.

## Índice

1. [AddressLookupProperties](#addresslookupproperties)
2. [CorsConfig](#corsconfig)
3. [JwtAuthFilter](#jwtauthfilter)
4. [JwtUtil](#jwtutil)
5. [OpenApiConfig](#openapiconfig)
6. [RestTemplateConfig](#resttemplateconfig)
7. [SecurityConfig](#securityconfig)

---

## AddressLookupProperties

**Arquivo**: `AddressLookupProperties.java`

### Descrição

Classe de configuração que carrega as propriedades relacionadas aos serviços de busca de endereços (ViaCEP e Google Geocoding API) a partir do arquivo `application.yml`.

### Anotações

- `@Component` - Registra a classe como um bean gerenciado pelo Spring
- `@ConfigurationProperties(prefix = "address.lookup")` - Vincula propriedades do arquivo de configuração com o prefixo `address.lookup`

### Propriedades

| Propriedade          | Tipo   | Valor Padrão                                        | Descrição                                                 |
| -------------------- | ------ | --------------------------------------------------- | --------------------------------------------------------- |
| `viacepBaseUrl`      | String | `https://viacep.com.br/ws`                          | URL base da API ViaCEP                                    |
| `googleGeocodingUrl` | String | `https://maps.googleapis.com/maps/api/geocode/json` | URL base da API Google Geocoding                          |
| `googleApiKey`       | String | `null`                                              | Chave de API do Google Cloud (obrigatória para geocoding) |

### Configuração no application.yml

```yaml
address:
  lookup:
    viacep-base-url: https://viacep.com.br/ws
    google-geocoding-url: https://maps.googleapis.com/maps/api/geocode/json
    google-api-key: sua-chave-api-aqui
```

### Uso

```java
@Autowired
private AddressLookupProperties properties;

public void exemploUso() {
    String apiKey = properties.getGoogleApiKey();
    String viacepUrl = properties.getViacepBaseUrl();
}
```

### Observações

- A chave da API do Google é **obrigatória** para funcionalidades de geocoding
- As URLs base podem ser sobrescritas para testes ou ambientes alternativos

---

## CorsConfig

**Arquivo**: `CorsConfig.java`

### Descrição

Configura o Cross-Origin Resource Sharing (CORS) para permitir que aplicações frontend em diferentes domínios acessem a API.

### Anotações

- `@Configuration` - Indica que é uma classe de configuração do Spring

### Propriedades Injetadas

```java
@Value("${cors.allowed-origins:https://front-contacts.vercel.app,http://localhost:3000,http://localhost:4200}")
private String allowedOrigins;
```

### Bean: corsFilter()

**Retorno**: `CorsFilter`

**Configurações**:

| Configuração      | Valor                                  | Descrição                                          |
| ----------------- | -------------------------------------- | -------------------------------------------------- |
| Allowed Origins   | Lista configurável                     | Origens permitidas (separadas por vírgula)         |
| Allowed Methods   | GET, POST, PUT, DELETE, PATCH, OPTIONS | Métodos HTTP permitidos                            |
| Allowed Headers   | `*`                                    | Todos os headers são permitidos                    |
| Allow Credentials | `true`                                 | Permite envio de cookies e headers de autenticação |
| Exposed Headers   | Authorization, Content-Type            | Headers expostos ao cliente                        |
| Max Age           | 3600 segundos                          | Tempo de cache da configuração CORS                |

### Origens Padrão

1. `https://front-contacts.vercel.app` - Frontend em produção (Vercel)
2. `http://localhost:3000` - Desenvolvimento React/Next.js
3. `http://localhost:4200` - Desenvolvimento Angular

### Configuração no application.yml

```yaml
cors:
  allowed-origins: "https://front-contacts.vercel.app,http://localhost:3000,http://localhost:4200"
```

### Como Adicionar Novas Origens

Para adicionar novas origens permitidas, adicione ao arquivo de configuração:

```yaml
cors:
  allowed-origins: "https://meu-app.com,http://localhost:3000"
```

### Observações

- As origens são separadas por vírgula
- Não use `*` em produção por questões de segurança
- O CORS é aplicado a todos os endpoints (`/**`)

---

## JwtAuthFilter

**Arquivo**: `JwtAuthFilter.java`

### Descrição

Filtro de autenticação JWT que intercepta todas as requisições HTTP, extrai e valida tokens JWT do header `Authorization`, e estabelece o contexto de segurança do Spring Security.

### Hierarquia

Estende `OncePerRequestFilter` - garante que o filtro seja executado apenas uma vez por requisição.

### Anotações

- `@Component` - Registra como bean do Spring

### Dependências Injetadas

```java
private final JwtUtil jwtUtil;
private final UserDetailsService userDetailsService;
```

### Fluxo de Execução

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Requisição HTTP chega                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Extrai header "Authorization"                            │
│    Formato esperado: "Bearer {token}"                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. Remove prefixo "Bearer " e obtém o token                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. Valida token usando JwtUtil                              │
│    - Verifica assinatura                                    │
│    - Verifica expiração                                     │
│    - Verifica formato                                       │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
        ▼                         ▼
    Token Válido              Token Inválido
        │                         │
        ▼                         ▼
┌───────────────────┐    ┌────────────────────┐
│ 5. Extrai username│    │ Loga erro e        │
│    do token       │    │ continua sem       │
└────────┬──────────┘    │ autenticação       │
         │                └────────────────────┘
         ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. Carrega UserDetails do banco via UserDetailsService      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. Cria UsernamePasswordAuthenticationToken                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 8. Define autenticação no SecurityContextHolder             │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 9. Continua a cadeia de filtros (filterChain.doFilter)      │
└─────────────────────────────────────────────────────────────┘
```

### Método Principal: doFilterInternal()

```java
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException
```

**Parâmetros**:
- `request` - Requisição HTTP
- `response` - Resposta HTTP
- `filterChain` - Cadeia de filtros

**Lógica**:
1. Extrai header `Authorization`
2. Verifica se começa com `"Bearer "`
3. Remove prefixo e obtém o token
4. Valida o token
5. Extrai username do token
6. Carrega dados do usuário
7. Cria objeto de autenticação
8. Define no contexto de segurança

### Tratamento de Exceções

| Exceção                     | Tratamento                           |
| --------------------------- | ------------------------------------ |
| `UsernameNotFoundException` | Loga e continua (usuário não existe) |
| `JwtException`              | Loga e continua (token inválido)     |
| Outras exceções             | Loga e continua                      |

### Logging

O filtro registra as seguintes informações:
- Username extraído do token
- Sucesso/falha na autenticação
- Erros de processamento

### Observações

- **Não bloqueia requisições** mesmo se o token for inválido
- Permite que endpoints públicos continuem funcionando
- A verificação de autorização é feita pelo SecurityConfig
- Utiliza `OncePerRequestFilter` para evitar execução duplicada

---

## JwtUtil

**Arquivo**: `JwtUtil.java`

### Descrição

Utilitário para geração, validação e extração de informações de tokens JWT (JSON Web Tokens). Implementa toda a lógica relacionada ao ciclo de vida dos tokens de autenticação.

### Anotações

- `@Component` - Registra como bean do Spring

### Propriedades Injetadas

```java
@Value("${jwt.secret}")
private String secret;  // Chave secreta para assinar tokens

@Value("${jwt.expiration}")
private long expirationMs;  // Tempo de expiração em milissegundos
```

### Atributos

```java
private Key key;  // Chave HMAC SHA-256 derivada do secret
```

### Métodos

#### @PostConstruct init()

**Descrição**: Inicializa a chave de assinatura após a construção do bean.

```java
@PostConstruct
public void init() {
    key = Keys.hmacShaKeyFor(secret.getBytes());
}
```

**Quando é executado**: Automaticamente após a criação do bean pelo Spring.

---

#### generateToken(String username)

**Descrição**: Gera um novo token JWT para o usuário especificado.

**Parâmetros**:
- `username` - Nome do usuário (geralmente email)

**Retorno**: `String` - Token JWT codificado

**Estrutura do Token**:
```json
{
  "sub": "usuario@email.com",
  "iat": 1702123456,
  "exp": 1702127056
}
```

**Algoritmo**: HMAC SHA-256 (HS256)

**Exemplo de uso**:
```java
String token = jwtUtil.generateToken("usuario@email.com");
// Retorna: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Implementação**:
```java
public String generateToken(String username) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .setSubject(username)           // Define o usuário
        .setIssuedAt(now)               // Data de criação
        .setExpiration(exp)             // Data de expiração
        .signWith(key, SignatureAlgorithm.HS256)  // Assina com HMAC SHA-256
        .compact();
}
```

---

#### validateToken(String token)

**Descrição**: Valida a integridade e validade de um token JWT.

**Parâmetros**:
- `token` - Token JWT a ser validado

**Retorno**: `boolean`
- `true` - Token válido
- `false` - Token inválido (expirado, malformado, assinatura inválida, etc.)

**Validações Realizadas**:
1. ✅ Assinatura (verifica se o token foi gerado com a chave correta)
2. ✅ Formato (estrutura JWT válida)
3. ✅ Expiração (verifica se o token ainda está válido)
4. ✅ Algoritmo suportado

**Exceções Capturadas**:

| Exceção                    | Causa                   | Log                          |
| -------------------------- | ----------------------- | ---------------------------- |
| `ExpiredJwtException`      | Token expirado          | "JWT expired: {message}"     |
| `UnsupportedJwtException`  | Algoritmo não suportado | "JWT invalid: {message}"     |
| `MalformedJwtException`    | Token malformado        | "JWT invalid: {message}"     |
| `SecurityException`        | Assinatura inválida     | "JWT invalid: {message}"     |
| `IllegalArgumentException` | Argumento ilegal        | "JWT illegal arg: {message}" |

**Exemplo de uso**:
```java
if (jwtUtil.validateToken(token)) {
    // Token válido - prosseguir com autenticação
} else {
    // Token inválido - rejeitar
}
```

---

#### getUsernameFromToken(String token)

**Descrição**: Extrai o username (subject) de um token JWT válido.

**Parâmetros**:
- `token` - Token JWT

**Retorno**: `String` - Username extraído do token

**Exceções**: Pode lançar exceções JWT se o token for inválido (normalmente já validado antes).

**Implementação**:
```java
public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
    return claims.getSubject();
}
```

**Exemplo de uso**:
```java
String username = jwtUtil.getUsernameFromToken(token);
// Retorna: "usuario@email.com"
```

---

### Configuração

#### application.yml

```yaml
jwt:
  secret: "sua-chave-secreta-segura-aqui"  # Mínimo 256 bits (32 caracteres)
  expiration: 3600000  # 1 hora em milissegundos
```

#### Recomendações de Segurança

1. **Secret deve ter no mínimo 256 bits** (32 caracteres) para HS256
2. **Não commitar o secret no repositório** - usar variáveis de ambiente
3. **Usar secrets diferentes por ambiente** (dev, staging, prod)
4. **Rotacionar o secret periodicamente**

#### Exemplos de Configuração de Tempo de Expiração

```yaml
# 30 minutos
jwt.expiration: 1800000

# 1 hora (padrão)
jwt.expiration: 3600000

# 24 horas
jwt.expiration: 86400000

# 7 dias
jwt.expiration: 604800000
```

### Fluxo Completo de Autenticação

```
┌──────────────┐
│ 1. Login     │
│ (AuthService)│
└──────┬───────┘
       │
       ▼
┌─────────────────────────┐
│ 2. generateToken()      │
│    - Cria JWT           │
│    - Define expiração   │
│    - Assina com secret  │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│ 3. Retorna token ao     │
│    cliente              │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│ 4. Cliente armazena     │
│    token (localStorage) │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│ 5. Requisições seguintes│
│    incluem token no     │
│    header Authorization │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│ 6. JwtAuthFilter        │
│    - validateToken()    │
│    - getUsernameFrom... │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│ 7. Acesso autorizado    │
└─────────────────────────┘
```

### Observações

- Utiliza a biblioteca JJWT (io.jsonwebtoken)
- Tokens são stateless (não armazenados no servidor)
- O username é armazenado no campo `subject` do token
- Logging com SLF4J para rastreabilidade

---

## OpenApiConfig

**Arquivo**: `OpenApiConfig.java`

### Descrição

Configura a documentação OpenAPI 3.0 (Swagger) da aplicação, incluindo informações da API, servidores e esquema de autenticação JWT.

### Anotações

- `@Configuration` - Classe de configuração do Spring

### Bean: contactsOpenAPI()

**Retorno**: `OpenAPI`

**Descrição**: Configura e retorna a especificação OpenAPI completa da aplicação.

### Configurações

#### 1. Informações da API

```java
.info(new Info()
    .title("Contacts API")
    .version("v1")
    .description("API para o teste dev - cadastro de contatos"))
```

| Campo     | Valor                                       |
| --------- | ------------------------------------------- |
| Título    | Contacts API                                |
| Versão    | v1                                          |
| Descrição | API para o teste dev - cadastro de contatos |

#### 2. Servidores

O método `resolveServerUrl()` detecta automaticamente a URL do servidor:

**Ordem de Prioridade**:
1. **Produção (Railway)**: Se a variável de ambiente `RAILWAY_PUBLIC_DOMAIN` estiver definida
   - Formato: `https://{RAILWAY_PUBLIC_DOMAIN}`
2. **Desenvolvimento**: URL padrão local
   - Formato: `http://localhost:8080`

**Implementação**:
```java
private String resolveServerUrl() {
    String railwayDomain = System.getenv("RAILWAY_PUBLIC_DOMAIN");
    
    if (railwayDomain != null && !railwayDomain.isBlank()) {
        return "https://" + railwayDomain;
    }
    
    return "http://localhost:8080";
}
```

#### 3. Esquema de Segurança (JWT)

**Nome do Esquema**: `bearerAuth`

**Configuração**:
```java
.addSecuritySchemes(securitySchemeName,
    new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .in(SecurityScheme.In.HEADER)
        .name("Authorization"))
```

| Propriedade   | Valor         | Descrição                 |
| ------------- | ------------- | ------------------------- |
| Type          | HTTP          | Tipo de autenticação HTTP |
| Scheme        | bearer        | Esquema Bearer Token      |
| Bearer Format | JWT           | Formato do token          |
| In            | HEADER        | Token enviado no header   |
| Name          | Authorization | Nome do header            |

#### 4. Requisito de Segurança Global

```java
.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
```

Aplica o esquema de autenticação JWT a **todos os endpoints** por padrão.

### Acessando a Documentação

#### Swagger UI (Interface Interativa)

```
http://localhost:8080/swagger-ui.html
```

ou

```
https://{RAILWAY_PUBLIC_DOMAIN}/swagger-ui.html
```

#### OpenAPI JSON

```
http://localhost:8080/v3/api-docs
```

#### OpenAPI YAML

```
http://localhost:8080/v3/api-docs.yaml
```

### Recursos do Swagger UI

1. **Visualização de Endpoints**: Lista todos os endpoints organizados por tags
2. **Try it out**: Testar endpoints diretamente pela interface
3. **Autenticação JWT**: Botão "Authorize" para inserir o token
4. **Schemas**: Visualizar estrutura dos DTOs e entidades
5. **Download**: Baixar especificação OpenAPI em JSON ou YAML

### Como Autenticar no Swagger UI

1. Acesse `/swagger-ui.html`
2. Clique no botão **"Authorize"** (cadeado)
3. Digite o token JWT no formato: `Bearer {seu-token}`
4. Clique em **"Authorize"**
5. Agora todas as requisições incluirão o token automaticamente

### Exemplo de Token no Swagger

```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImlhdCI6MTcwMjEyMzQ1NiwiZXhwIjoxNzAyMTI3MDU2fQ.abc123...
```

### Endpoints Públicos no Swagger

Os seguintes endpoints **não requerem autenticação**:
- `/api/auth/**` - Autenticação (login, signup)
- `/swagger-ui/**` - Interface Swagger
- `/v3/api-docs/**` - Especificação OpenAPI
- `/health` - Health check

### Customização

Para adicionar mais informações à documentação OpenAPI:

```java
.info(new Info()
    .title("Contacts API")
    .version("v1")
    .description("API para o teste dev - cadastro de contatos")
    .contact(new Contact()
        .name("Seu Nome")
        .email("seu@email.com"))
    .license(new License()
        .name("MIT")
        .url("https://opensource.org/licenses/MIT")))
```

### Observações

- A configuração é automaticamente integrada com SpringDoc
- Suporta detecção automática de endpoints
- Compatível com OpenAPI 3.0
- Interface Swagger UI vem pré-configurada

---

## RestTemplateConfig

**Arquivo**: `RestTemplateConfig.java`

### Descrição

Configura o `RestTemplate` para chamadas HTTP a APIs externas (ViaCEP e Google Geocoding). Define timeouts e factory personalizada.

### Anotações

- `@Configuration` - Classe de configuração do Spring

### Bean: restTemplate()

**Retorno**: `RestTemplate`

**Descrição**: Cria e configura uma instância de RestTemplate com timeouts personalizados.

### Configuração

```java
@Bean
public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(5000);
    return new RestTemplate(factory);
}
```

### Timeouts

| Tipo                | Valor                | Descrição                             |
| ------------------- | -------------------- | ------------------------------------- |
| **Connect Timeout** | 3000 ms (3 segundos) | Tempo máximo para estabelecer conexão |
| **Read Timeout**    | 5000 ms (5 segundos) | Tempo máximo para ler a resposta      |

### Request Factory

**Tipo**: `SimpleClientHttpRequestFactory`

Implementação básica do Spring para requisições HTTP usando `HttpURLConnection` do Java.

### Uso no Projeto

O RestTemplate configurado é injetado nos seguintes clientes:

1. **ViaCepClient** - Busca de endereços
2. **GoogleGeocodingClient** - Geocodificação

**Exemplo de injeção**:
```java
@Component
@RequiredArgsConstructor
public class ViaCepClient {
    private final RestTemplate restTemplate;
    
    public ViaCepAddress findByCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        return restTemplate.getForObject(url, ViaCepAddress.class);
    }
}
```

### Tratamento de Timeouts

Quando um timeout ocorre:

```java
try {
    ResponseEntity<T> response = restTemplate.getForEntity(url, T.class);
} catch (ResourceAccessException e) {
    // Timeout ou erro de conexão
    throw new ExternalServiceException("Serviço externo indisponível", e);
}
```

### Customizações Possíveis

#### Adicionar Interceptors

```java
@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    
    // Adicionar interceptor para logging
    restTemplate.setInterceptors(Collections.singletonList(
        (request, body, execution) -> {
            log.info("URI: {}", request.getURI());
            return execution.execute(request, body);
        }
    ));
    
    return restTemplate;
}
```

#### Configurar Error Handler

```java
@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    
    restTemplate.setErrorHandler(new ResponseErrorHandler() {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().is4xxClientError() 
                || response.getStatusCode().is5xxServerError();
        }
        
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // Tratamento customizado de erros
        }
    });
    
    return restTemplate;
}
```

#### Adicionar Message Converters

```java
@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    
    // Adicionar conversor JSON customizado
    MappingJackson2HttpMessageConverter converter = 
        new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(customObjectMapper());
    
    restTemplate.getMessageConverters().add(0, converter);
    
    return restTemplate;
}
```

### Alternativas ao RestTemplate

Para projetos novos, considere usar:

1. **WebClient** (reativo)
```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
        .baseUrl("https://api.example.com")
        .defaultHeader("User-Agent", "Contacts API")
        .build();
}
```

2. **Feign Client** (declarativo)
```java
@FeignClient(name = "viacep", url = "https://viacep.com.br")
public interface ViaCepClient {
    @GetMapping("/ws/{cep}/json/")
    ViaCepAddress findByCep(@PathVariable String cep);
}
```

### Observações

- RestTemplate é **thread-safe** e pode ser compartilhado
- Os timeouts evitam que requisições fiquem travadas indefinidamente
- **RestTemplate está em modo de manutenção** - Spring recomenda WebClient para novos projetos
- Esta configuração é adequada para o escopo do projeto atual

---

## SecurityConfig

**Arquivo**: `SecurityConfig.java`

### Descrição

Configuração principal de segurança da aplicação usando Spring Security. Define regras de autorização, autenticação JWT, CORS, criptografia de senhas e gerenciamento de sessões.

### Anotações

- `@Configuration` - Classe de configuração do Spring
- `@EnableWebSecurity` - Habilita a configuração de segurança web do Spring Security
- `@RequiredArgsConstructor` - Gera construtor para injeção de dependências (Lombok)

### Beans Configurados

---

#### 1. SecurityFilterChain filterChain(HttpSecurity, JwtAuthFilter)

**Retorno**: `SecurityFilterChain`

**Descrição**: Define a cadeia de filtros de segurança e regras de autorização.

**Configurações**:

##### CORS
```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```
Habilita CORS usando a configuração definida no método `corsConfigurationSource()`.

##### CSRF
```java
.csrf(csrf -> csrf.disable())
```
**Desabilita CSRF** (Cross-Site Request Forgery) - adequado para APIs REST stateless com autenticação JWT.

##### Gerenciamento de Sessão
```java
.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```
**Política**: `STATELESS`
- Não cria sessões HTTP
- Cada requisição é independente
- Autenticação via JWT (stateless)

##### Regras de Autorização
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/api/auth/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/v3/api-docs.yaml",
        "/health",
        "/actuator/health",
        "/error")
    .permitAll()
    .anyRequest().authenticated())
```

**Endpoints Públicos** (não requerem autenticação):

| Padrão              | Descrição                                 |
| ------------------- | ----------------------------------------- |
| `/api/auth/**`      | Endpoints de autenticação (login, signup) |
| `/swagger-ui/**`    | Interface Swagger UI                      |
| `/swagger-ui.html`  | Página principal do Swagger               |
| `/v3/api-docs/**`   | Especificação OpenAPI                     |
| `/v3/api-docs.yaml` | OpenAPI em formato YAML                   |
| `/health`           | Health check básico                       |
| `/actuator/health`  | Health check do Spring Actuator           |
| `/error`            | Página de erro padrão                     |

**Todos os outros endpoints**: Requerem autenticação (`authenticated()`)

##### Adição do Filtro JWT
```java
http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

Adiciona o `JwtAuthFilter` **antes** do filtro padrão de autenticação, garantindo que tokens JWT sejam processados primeiro.

---

#### 2. CorsConfigurationSource corsConfigurationSource()

**Retorno**: `CorsConfigurationSource`

**Descrição**: Configura CORS para a aplicação.

**Configuração Detalhada**:

```java
CorsConfiguration configuration = new CorsConfiguration();

// Origens permitidas
configuration.setAllowedOrigins(List.of(
    "https://front-contacts.vercel.app",  // Produção
    "http://localhost:3000",               // React/Next.js
    "http://localhost:4200"                // Angular
));

// Métodos HTTP permitidos
configuration.setAllowedMethods(
    List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
);

// Headers permitidos
configuration.setAllowedHeaders(List.of("*"));

// Permite credenciais (cookies, Authorization header)
configuration.setAllowCredentials(true);

// Headers expostos ao cliente
configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

// Cache da configuração CORS (1 hora)
configuration.setMaxAge(3600L);
```

**Aplicação**: Registra a configuração para todos os endpoints (`/**`)

---

#### 3. AuthenticationManager authenticationManager(AuthenticationConfiguration)

**Retorno**: `AuthenticationManager`

**Descrição**: Fornece o gerenciador de autenticação do Spring Security, usado para autenticar credenciais de usuário.

**Uso**: Injetado no `AuthService` para validar login.

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) 
    throws Exception {
    return authConfig.getAuthenticationManager();
}
```

---

#### 4. PasswordEncoder passwordEncoder()

**Retorno**: `PasswordEncoder`

**Descrição**: Configura o encoder de senhas usando BCrypt.

**Implementação**: `BCryptPasswordEncoder`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Características do BCrypt**:
- Algoritmo de hash adaptativo
- Inclui salt aleatório automaticamente
- Proteção contra rainbow tables
- Custo computacional configurável (padrão: 10 rounds)

**Uso**:
```java
// Criptografar senha
String hashedPassword = passwordEncoder.encode("senhaPlana");

// Verificar senha
boolean matches = passwordEncoder.matches("senhaPlana", hashedPassword);
```

---

### Fluxo de Segurança

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Requisição HTTP chega                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Filtro CORS processa a requisição                        │
│    - Verifica origem permitida                              │
│    - Adiciona headers CORS na resposta                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. JwtAuthFilter processa (ANTES de UsernamePassword...)    │
│    - Extrai e valida token JWT                              │
│    - Define autenticação no SecurityContext                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. Spring Security verifica autorização                     │
│    - Endpoint é público? → Permite                          │
│    - Endpoint requer autenticação?                          │
│      → Usuário autenticado? → Permite                       │
│      → Não autenticado? → 401 Unauthorized                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. Se autorizado, continua para o Controller                │
└─────────────────────────────────────────────────────────────┘
```

---

### Cenários de Uso

#### 1. Login (Endpoint Público)

```
GET /api/auth/login
→ CORS: OK
→ JwtAuthFilter: Nenhum token (OK para endpoint público)
→ Authorization: Endpoint público (permitAll)
→ Result: Controller processa login
```

#### 2. Listar Contatos (Endpoint Protegido - COM Token)

```
GET /api/contacts
Header: Authorization: Bearer {token-válido}

→ CORS: OK
→ JwtAuthFilter: Token válido → Autentica usuário
→ Authorization: Requer autenticação → Usuário autenticado → OK
→ Result: Controller lista contatos do usuário
```

#### 3. Listar Contatos (Endpoint Protegido - SEM Token)

```
GET /api/contacts
(Sem header Authorization)

→ CORS: OK
→ JwtAuthFilter: Nenhum token → Não autentica
→ Authorization: Requer autenticação → Usuário NÃO autenticado
→ Result: 401 Unauthorized
```

#### 4. Swagger UI (Endpoint Público)

```
GET /swagger-ui.html

→ CORS: OK
→ JwtAuthFilter: Pode ou não ter token
→ Authorization: Endpoint público (permitAll)
→ Result: Página Swagger carrega
```

---

### Segurança em Produção

#### Checklist de Segurança

- [ ] **JWT Secret**: Usar secret forte e seguro (mínimo 256 bits)
- [ ] **HTTPS**: Sempre usar HTTPS em produção
- [ ] **CORS**: Limitar origens ao mínimo necessário
- [ ] **Rate Limiting**: Adicionar proteção contra força bruta
- [ ] **Headers de Segurança**: Configurar headers HTTP de segurança
- [ ] **Logging**: Não logar tokens ou senhas
- [ ] **Expiração de Token**: Configurar tempo adequado

#### Headers de Segurança Recomendados

Adicione ao SecurityConfig:

```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> 
        csp.policyDirectives("default-src 'self'"))
    .frameOptions(frame -> frame.deny())
    .xssProtection(xss -> xss.enable())
);
```

---

### Observações

- **Stateless**: Não usa sessões HTTP - completamente stateless
- **JWT Only**: Autenticação exclusivamente via tokens JWT
- **BCrypt**: Senhas criptografadas com BCrypt (irreversível)
- **CORS**: Configurado para permitir frontend em diferentes domínios
- **Swagger**: Documentação acessível sem autenticação
- **Extensível**: Fácil adicionar novos endpoints públicos ou regras de autorização

---

## Resumo da Arquitetura de Segurança

### Componentes

1. **SecurityConfig** - Define regras de autorização e configurações gerais
2. **JwtAuthFilter** - Intercepta requisições e processa tokens JWT
3. **JwtUtil** - Gera e valida tokens
4. **CorsConfig/corsConfigurationSource** - Permite acesso cross-origin
5. **PasswordEncoder** - Criptografa senhas com BCrypt
6. **AuthenticationManager** - Autentica credenciais de usuário

### Fluxo Completo de Autenticação

```
┌──────────────┐
│ 1. Signup    │ → passwordEncoder.encode()
└──────┬───────┘   → Salva no banco
       │
       ▼
┌──────────────┐
│ 2. Login     │ → authenticationManager.authenticate()
└──────┬───────┘   → jwtUtil.generateToken()
       │            → Retorna token ao cliente
       ▼
┌──────────────────┐
│ 3. Requisições   │ → Cliente inclui token no header
│    subsequentes  │   → JwtAuthFilter valida e autentica
└──────┬───────────┘   → SecurityConfig autoriza endpoint
       │                → Controller processa
       ▼
┌──────────────┐
│ 4. Resposta  │
└──────────────┘
```

---

**Última atualização**: Dezembro 2025  
**Versão do Spring Security**: 6.x (Spring Boot 4.0.0)
