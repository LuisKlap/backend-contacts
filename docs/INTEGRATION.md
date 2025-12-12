# Documentação - Pacote Integration

Este documento detalha todas as classes de integração com APIs externas do projeto, localizadas em `src/main/java/com/dev/contacts/integration/`.

## Índice

1. [Visão Geral](#visão-geral)
2. [GoogleGeocodingClient](#googlegeocodingclient)
3. [ViaCepClient](#viacepclient)
4. [Tratamento de Erros](#tratamento-de-erros)
5. [Boas Práticas](#boas-práticas)

---

## Visão Geral

O pacote `integration` contém clientes para comunicação com APIs externas:

| Cliente                   | API Externa                                                                        | Propósito                                                |
| ------------------------- | ---------------------------------------------------------------------------------- | -------------------------------------------------------- |
| **ViaCepClient**          | [ViaCEP](https://viacep.com.br)                                                    | Busca de endereços por CEP e por UF/Cidade/Rua           |
| **GoogleGeocodingClient** | [Google Geocoding API](https://developers.google.com/maps/documentation/geocoding) | Obtenção de coordenadas geográficas (latitude/longitude) |

### Arquitetura

```
┌─────────────────┐
│   Controller    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│     Service     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│          Integration Layer              │
│  ┌───────────────┐  ┌──────────────────┐│
│  │ ViaCepClient  │  │GoogleGeocoding.. ││
│  └───────────────┘  └──────────────────┘│
└────────┬──────────────────┬─────────────┘
         │                  │
         ▼                  ▼
┌─────────────────┐  ┌─────────────────┐
│   ViaCEP API    │  │ Google Maps API │
└─────────────────┘  └─────────────────┘
```

### Dependências Comuns

Ambos os clientes utilizam:

- **RestTemplate** - Para fazer requisições HTTP (configurado em `RestTemplateConfig`)
- **AddressLookupProperties** - Para carregar URLs e chaves de API
- **Jackson Annotations** - Para deserialização JSON

---

## GoogleGeocodingClient

**Arquivo**: `GoogleGeocodingClient.java`

### Descrição

Cliente para a API Google Geocoding que converte endereços em coordenadas geográficas (latitude e longitude). Utilizado para enriquecer dados de contatos com geolocalização.

### Anotações

- `@Component` - Registra como bean do Spring
- `@RequiredArgsConstructor` - Gera construtor com dependências final (Lombok)

### Dependências Injetadas

```java
private final AddressLookupProperties props;
private final RestTemplate restTemplate;
```

---

### Classes Internas (DTOs)

#### Location
```java
public static class Location {
    public Double lat;  // Latitude
    public Double lng;  // Longitude
}
```

Representa coordenadas geográficas.

#### Geometry
```java
@JsonIgnoreProperties(ignoreUnknown = true)
static class Geometry {
    @JsonProperty("location")
    public Location location;
}
```

Contém informações geométricas do resultado.

#### Result
```java
@JsonIgnoreProperties(ignoreUnknown = true)
static class Result {
    public Geometry geometry;
    
    @JsonProperty("formatted_address")
    public String formattedAddress;
}
```

Representa um resultado individual da geocodificação.

#### GeocodingResponse
```java
@JsonIgnoreProperties(ignoreUnknown = true)
static class GeocodingResponse {
    public String status;
    public Result[] results;
}
```

Resposta completa da API Google Geocoding.

**Possíveis valores de `status`**:

| Status             | Descrição                            |
| ------------------ | ------------------------------------ |
| `OK`               | Geocodificação bem-sucedida          |
| `ZERO_RESULTS`     | Nenhum resultado encontrado          |
| `OVER_QUERY_LIMIT` | Limite de consultas excedido         |
| `REQUEST_DENIED`   | Requisição negada (API key inválida) |
| `INVALID_REQUEST`  | Parâmetros inválidos                 |
| `UNKNOWN_ERROR`    | Erro no servidor do Google           |

---

### Métodos

#### geocode(String query)

**Descrição**: Converte um endereço textual em coordenadas geográficas.

**Parâmetros**:
- `query` - Endereço completo em formato textual

**Retorno**: `Optional<Location>`
- `Optional.of(Location)` - Se coordenadas foram encontradas
- `Optional.empty()` - Se não encontrou ou ocorreu erro

**Exceções**:
- `ExternalServiceException` - Em caso de erro na comunicação ou status inválido

---

**Fluxo de Execução**:

```
┌──────────────────────────┐
│ 1. Validação inicial     │
│    - API key existe?     │
│    - Query não vazio?    │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│ 2. Constrói URI          │
│    - URL base            │
│    - Parâmetro address   │
│    - Parâmetro key       │
│    - Encode              │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│ 3. Faz requisição GET    │
│    via RestTemplate      │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│ 4. Processa resposta     │
│    - Status OK?          │
│    - Tem resultados?     │
│    - Extrai Location     │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│ 5. Retorna Optional      │
└──────────────────────────┘
```

---

**Implementação Detalhada**:

```java
public Optional<Location> geocode(String query) {
    // 1. Validação inicial
    if (props.getGoogleApiKey() == null || 
        props.getGoogleApiKey().isBlank() || 
        query == null || 
        query.isBlank()) {
        return Optional.empty();
    }

    try {
        // 2. Construção da URI
        URI uri = UriComponentsBuilder
            .fromUriString(props.getGoogleGeocodingUrl())
            .queryParam("address", query)
            .queryParam("key", props.getGoogleApiKey())
            .build()
            .encode()  // Encode automático de caracteres especiais
            .toUri();

        // 3. Requisição HTTP GET
        ResponseEntity<GeocodingResponse> resp = 
            restTemplate.getForEntity(uri, GeocodingResponse.class);
        
        GeocodingResponse body = resp.getBody();
        
        if (body == null) {
            return Optional.empty();
        }

        // 4. Processamento baseado no status
        switch (body.status) {
            case "OK":
                if (body.results != null && 
                    body.results.length > 0 && 
                    body.results[0].geometry != null) {
                    
                    Location loc = body.results[0].geometry.location;
                    return Optional.ofNullable(loc);
                } else {
                    return Optional.empty();
                }
            
            case "ZERO_RESULTS":
                return Optional.empty();
            
            default:
                throw new ExternalServiceException(
                    "Google Geocoding returned status: " + body.status);
        }
    } catch (ExternalServiceException e) {
        throw e;  // Propaga exceção customizada
    } catch (Exception e) {
        throw new ExternalServiceException(
            "Google Geocoding lookup failed", e);
    }
}
```

---

### Exemplo de Uso

#### No Service

```java
@Service
@RequiredArgsConstructor
public class ContactService {
    private final GoogleGeocodingClient geocodingClient;
    
    public ContactResponse createContact(User owner, ContactRequest request) {
        // ... validações ...
        
        // Montar endereço completo
        String fullAddress = String.format("%s, %s, %s, %s - %s",
            request.street(),
            request.number(),
            request.city(),
            request.state(),
            request.cep()
        );
        
        // Obter coordenadas
        Optional<GoogleGeocodingClient.Location> location = 
            geocodingClient.geocode(fullAddress);
        
        Contact contact = new Contact();
        // ... preencher outros campos ...
        
        if (location.isPresent()) {
            contact.setLatitude(BigDecimal.valueOf(location.get().lat));
            contact.setLongitude(BigDecimal.valueOf(location.get().lng));
        }
        
        // ... salvar e retornar ...
    }
}
```

---

### Configuração

#### application.yml

```yaml
address:
  lookup:
    google-geocoding-url: https://maps.googleapis.com/maps/api/geocode/json
    google-api-key: AIzaSyC...  # Sua chave da API Google
```

#### Obtendo API Key

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um projeto ou selecione um existente
3. Ative a **Geocoding API**
4. Crie credenciais → API Key
5. (Recomendado) Restrinja a chave:
   - Restrição de aplicativo: IP do servidor
   - Restrição de API: Apenas Geocoding API

---

### Formato da Requisição

```
GET https://maps.googleapis.com/maps/api/geocode/json
    ?address=Avenida+Paulista%2C+1000%2C+S%C3%A3o+Paulo%2C+SP
    &key=AIzaSyC...
```

### Formato da Resposta (OK)

```json
{
  "status": "OK",
  "results": [
    {
      "formatted_address": "Av. Paulista, 1000 - Bela Vista, São Paulo - SP, Brasil",
      "geometry": {
        "location": {
          "lat": -23.5617714,
          "lng": -46.6560186
        }
      }
    }
  ]
}
```

### Formato da Resposta (ZERO_RESULTS)

```json
{
  "status": "ZERO_RESULTS",
  "results": []
}
```

---

### Custos e Limites

#### Google Geocoding API

- **Gratuito**: 40.000 requisições/mês
- **Custo adicional**: $5 por 1.000 requisições extras
- **Rate Limit**: 50 requisições/segundo

#### Recomendações

1. **Cache**: Implementar cache para endereços já consultados
2. **Otimização**: Geocodificar apenas quando necessário
3. **Fallback**: Aplicação deve funcionar mesmo sem coordenadas
4. **Monitoramento**: Acompanhar uso via Google Cloud Console

---

### Tratamento de Casos Especiais

#### API Key Não Configurada

```java
if (props.getGoogleApiKey() == null || props.getGoogleApiKey().isBlank()) {
    return Optional.empty();  // Retorna vazio sem erro
}
```

**Comportamento**: Aplicação continua funcionando, mas sem coordenadas.

#### Query Inválida ou Vazia

```java
if (query == null || query.isBlank()) {
    return Optional.empty();
}
```

#### Endereço Não Encontrado

```java
case "ZERO_RESULTS":
    return Optional.empty();  // Retorna vazio, não é erro
```

#### Erro de Comunicação

```java
catch (Exception e) {
    throw new ExternalServiceException("Google Geocoding lookup failed", e);
}
```

---

### Observações

- **Thread-safe**: RestTemplate é thread-safe
- **Encoding**: URI é automaticamente encoded para caracteres especiais
- **Timeout**: Configurado no RestTemplateConfig (5 segundos)
- **Primeiro resultado**: Retorna apenas o primeiro resultado (mais relevante)
- **Opcional**: Uso de `Optional<Location>` indica que coordenadas são opcionais

---

## ViaCepClient

**Arquivo**: `ViaCepClient.java`

### Descrição

Cliente para a API ViaCEP que permite buscar endereços brasileiros por CEP ou por combinação de UF/Cidade/Rua. API pública e gratuita mantida por [ViaCEP](https://viacep.com.br).

### Anotações

- `@Component` - Registra como bean do Spring
- `@RequiredArgsConstructor` - Gera construtor com dependências final (Lombok)

### Dependências Injetadas

```java
private final AddressLookupProperties props;
private final RestTemplate restTemplate;
```

---

### Classes Internas (DTOs)

#### ViaCepAddress

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public static class ViaCepAddress {
    public String cep;              // CEP (com ou sem hífen)
    
    @JsonProperty("logradouro")
    public String logradouro;       // Rua/Avenida/etc
    
    @JsonProperty("complemento")
    public String complemento;      // Complemento
    
    @JsonProperty("bairro")
    public String bairro;          // Bairro
    
    @JsonProperty("localidade")
    public String localidade;      // Cidade
    
    @JsonProperty("uf")
    public String uf;              // Estado (sigla)
    
    @JsonProperty("erro")
    public Boolean erro;           // true se CEP não existe
}
```

**Observações**:
- Usa `@JsonProperty` para mapear nomes em português da API
- Campo `erro` indica quando um CEP não é encontrado
- `@JsonIgnoreProperties(ignoreUnknown = true)` ignora campos extras da API

---

### Métodos

---

#### 1. findByCep(String cep)

**Descrição**: Busca endereço diretamente por CEP (consulta exata).

**Parâmetros**:
- `cep` - CEP sem hífen (8 dígitos: "01310100")

**Retorno**: `ViaCepAddress` ou `null` se não encontrado

**Exceções**:
- `ExternalServiceException` - Em caso de erro na comunicação

**URL**: `https://viacep.com.br/ws/{cep}/json/`

---

**Implementação**:

```java
public ViaCepAddress findByCep(String cep) {
    try {
        String url = String.format("%s/%s/json/", 
            props.getViacepBaseUrl(), cep);
        
        ResponseEntity<ViaCepAddress> resp = 
            restTemplate.getForEntity(url, ViaCepAddress.class);
        
        return resp.getBody();
    } catch (Exception e) {
        throw new ExternalServiceException("ViaCep CEP lookup failed", e);
    }
}
```

---

**Exemplo de Uso**:

```java
@Service
@RequiredArgsConstructor
public class AddressLookupService {
    private final ViaCepClient viaCepClient;
    
    public AddressResponse findByCep(String cep) {
        // Limpar CEP (remover hífen, espaços, etc)
        String cleanCep = cep.replaceAll("\\D", "");
        
        // Buscar na API
        ViaCepClient.ViaCepAddress address = viaCepClient.findByCep(cleanCep);
        
        // Verificar se foi encontrado
        if (address == null || Boolean.TRUE.equals(address.erro)) {
            throw new ResourceNotFoundException("CEP não encontrado");
        }
        
        // Converter para DTO de resposta
        return AddressResponse.builder()
            .cep(address.cep)
            .street(address.logradouro)
            .neighborhood(address.bairro)
            .city(address.localidade)
            .state(address.uf)
            .build();
    }
}
```

---

**Formato da Requisição**:

```
GET https://viacep.com.br/ws/01310100/json/
```

**Formato da Resposta (Sucesso)**:

```json
{
  "cep": "01310-100",
  "logradouro": "Avenida Paulista",
  "complemento": "de 612 a 1510 - lado par",
  "bairro": "Bela Vista",
  "localidade": "São Paulo",
  "uf": "SP",
  "ibge": "3550308",
  "gia": "1004",
  "ddd": "11",
  "siafi": "7107"
}
```

**Formato da Resposta (CEP Não Encontrado)**:

```json
{
  "erro": true
}
```

---

#### 2. searchByUfCityStreet(String uf, String city, String street)

**Descrição**: Busca endereços por combinação de UF/Cidade/Rua. Retorna uma lista de endereços que correspondem aos critérios.

**Parâmetros**:
- `uf` - Sigla do estado (2 letras: "SP", "RJ", etc)
- `city` - Nome da cidade ("São Paulo", "Rio de Janeiro", etc)
- `street` - Nome da rua (mínimo 3 caracteres)

**Retorno**: `List<ViaCepAddress>` - Lista de endereços encontrados (pode ser vazia)

**Exceções**:
- `ExternalServiceException` - Em caso de erro na comunicação

**URL**: `https://viacep.com.br/ws/{uf}/{city}/{street}/json/`

---

**Implementação**:

```java
public List<ViaCepAddress> searchByUfCityStreet(String uf, String city, String street) {
    try {
        // Validação e limpeza
        String cleanUf = (uf != null) ? uf.trim() : "";
        String cleanCity = (city != null) ? city.trim() : "";
        String cleanStreet = (street != null) ? street.trim() : "";

        // Construir URL com encoding automático
        String url = UriComponentsBuilder
            .fromUriString(props.getViacepBaseUrl())
            .path("/{uf}/{city}/{street}/json/")
            .buildAndExpand(cleanUf, cleanCity, cleanStreet)
            .toUriString();

        // Fazer requisição
        ResponseEntity<ViaCepAddress[]> resp = 
            restTemplate.getForEntity(url, ViaCepAddress[].class);
        
        ViaCepAddress[] body = resp.getBody();
        
        if (body == null) {
            return List.of();
        }
        
        return Arrays.asList(body);
    } catch (Exception e) {
        throw new ExternalServiceException("ViaCep lookup failed", e);
    }
}
```

---

**Exemplo de Uso**:

```java
@Service
@RequiredArgsConstructor
public class AddressLookupService {
    private final ViaCepClient viaCepClient;
    
    public List<AddressResponse> searchAddresses(String uf, String city, String street) {
        // Buscar na API
        List<ViaCepClient.ViaCepAddress> addresses = 
            viaCepClient.searchByUfCityStreet(uf, city, street);
        
        // Converter para DTOs de resposta
        return addresses.stream()
            .map(addr -> AddressResponse.builder()
                .cep(addr.cep)
                .street(addr.logradouro)
                .neighborhood(addr.bairro)
                .city(addr.localidade)
                .state(addr.uf)
                .build())
            .collect(Collectors.toList());
    }
}
```

---

**Formato da Requisição**:

```
GET https://viacep.com.br/ws/SP/São%20Paulo/Paulista/json/
```

**Formato da Resposta (Múltiplos Resultados)**:

```json
[
  {
    "cep": "01310-100",
    "logradouro": "Avenida Paulista",
    "complemento": "de 612 a 1510 - lado par",
    "bairro": "Bela Vista",
    "localidade": "São Paulo",
    "uf": "SP"
  },
  {
    "cep": "01310-200",
    "logradouro": "Avenida Paulista",
    "complemento": "de 1353 a 2265 - lado ímpar",
    "bairro": "Bela Vista",
    "localidade": "São Paulo",
    "uf": "SP"
  }
]
```

**Formato da Resposta (Nenhum Resultado)**:

```json
[]
```

---

### Configuração

#### application.yml

```yaml
address:
  lookup:
    viacep-base-url: https://viacep.com.br/ws
```

**Observação**: Não requer API key (serviço público gratuito).

---

### Validações e Regras

#### CEP

- **Formato**: 8 dígitos numéricos
- **Com hífen**: "01310-100" (aceito)
- **Sem hífen**: "01310100" (aceito)
- **Limpeza**: Remover caracteres não numéricos antes de consultar

#### Busca por UF/Cidade/Rua

**Regras da API ViaCEP**:

| Parâmetro | Mínimo de Caracteres | Exemplo     |
| --------- | -------------------- | ----------- |
| UF        | 2 (exatamente)       | "SP", "RJ"  |
| Cidade    | 3                    | "São Paulo" |
| Rua       | 3                    | "Paulista"  |

**Recomendações**:

1. **Trim**: Remover espaços no início/fim
2. **Case**: A API é case-insensitive
3. **Acentos**: Preservar acentuação correta
4. **Encoding**: UriComponentsBuilder faz encoding automático

---

### Características da API ViaCEP

#### Vantagens

✅ **Gratuita** - Sem necessidade de API key  
✅ **Sem limites** - Uso ilimitado  
✅ **Pública** - Mantida pela comunidade  
✅ **Confiável** - Alta disponibilidade  
✅ **Completa** - Cobertura nacional (Brasil)  

#### Limitações

❌ **Apenas Brasil** - Não suporta endereços internacionais  
❌ **Sem rate limit** - Mas recomenda-se uso responsável  
❌ **Sem coordenadas** - Não fornece latitude/longitude  
❌ **Atualização** - Depende dos Correios (pode ter atraso)  

---

### Casos de Uso

#### 1. Autocompletar CEP

```java
// Usuário digita CEP
String cep = "01310100";

// Buscar endereço
ViaCepAddress address = viaCepClient.findByCep(cep);

// Preencher automaticamente os campos do formulário
if (address != null && !Boolean.TRUE.equals(address.erro)) {
    form.setStreet(address.logradouro);
    form.setNeighborhood(address.bairro);
    form.setCity(address.localidade);
    form.setState(address.uf);
}
```

#### 2. Sugestão de Endereços

```java
// Usuário digita parte do endereço
List<ViaCepAddress> suggestions = 
    viaCepClient.searchByUfCityStreet("SP", "São Paulo", "Paulista");

// Exibir lista de sugestões para seleção
return suggestions.stream()
    .map(addr -> String.format("%s - %s", addr.logradouro, addr.cep))
    .limit(10)  // Limitar a 10 sugestões
    .collect(Collectors.toList());
```

#### 3. Validação de CEP

```java
public boolean isCepValid(String cep) {
    try {
        String cleanCep = cep.replaceAll("\\D", "");
        ViaCepAddress address = viaCepClient.findByCep(cleanCep);
        return address != null && !Boolean.TRUE.equals(address.erro);
    } catch (ExternalServiceException e) {
        return false;
    }
}
```

---

### Tratamento de Erros

#### CEP Não Encontrado

```java
ViaCepAddress address = viaCepClient.findByCep("99999999");

if (address == null || Boolean.TRUE.equals(address.erro)) {
    throw new ResourceNotFoundException("CEP não encontrado");
}
```

#### Nenhum Resultado na Busca

```java
List<ViaCepAddress> addresses = 
    viaCepClient.searchByUfCityStreet("ZZ", "Inexistente", "Rua");

if (addresses.isEmpty()) {
    return AddressResponse.empty();  // Retorna lista vazia
}
```

#### Erro de Comunicação

```java
try {
    ViaCepAddress address = viaCepClient.findByCep(cep);
} catch (ExternalServiceException e) {
    log.error("Erro ao consultar ViaCEP", e);
    throw new ServiceUnavailableException("Serviço de CEP temporariamente indisponível");
}
```

---

### Performance e Cache

#### Recomendação: Implementar Cache

```java
@Service
public class AddressLookupService {
    private final ViaCepClient viaCepClient;
    
    @Cacheable(value = "cep-cache", key = "#cep")
    public AddressResponse findByCep(String cep) {
        ViaCepAddress address = viaCepClient.findByCep(cep);
        // ... conversão ...
    }
}
```

**Configuração de Cache**:

```yaml
spring:
  cache:
    cache-names: cep-cache
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=24h
```

**Benefícios**:
- Reduz chamadas à API externa
- Melhora tempo de resposta
- Diminui dependência de serviços externos

---

### Observações

- **Thread-safe**: RestTemplate é thread-safe
- **Encoding**: UriComponentsBuilder faz encoding de caracteres especiais
- **Timeout**: Configurado no RestTemplateConfig (5 segundos)
- **Sem autenticação**: API é completamente pública
- **Rate limiting**: Não há limite oficial, mas use com responsabilidade

---

## Tratamento de Erros

### ExternalServiceException

Exceção customizada para erros em serviços externos.

```java
// Lançada quando:
// - API externa retorna erro
// - Timeout de conexão
// - Erro de parsing JSON
// - Status HTTP de erro

throw new ExternalServiceException("Google Geocoding lookup failed", e);
```

### Estratégias de Tratamento

#### 1. Retry com Backoff (Recomendado para Produção)

```java
@Service
public class ResilientViaCepClient {
    private final ViaCepClient viaCepClient;
    
    @Retryable(
        value = {ExternalServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public ViaCepAddress findByCepWithRetry(String cep) {
        return viaCepClient.findByCep(cep);
    }
}
```

#### 2. Fallback

```java
public Optional<Location> geocodeWithFallback(String address) {
    try {
        return geocodingClient.geocode(address);
    } catch (ExternalServiceException e) {
        log.warn("Geocoding failed, returning empty", e);
        return Optional.empty();  // Aplicação continua sem coordenadas
    }
}
```

#### 3. Circuit Breaker (Para Alta Disponibilidade)

```java
@Service
public class ResilientGeocodingService {
    
    @CircuitBreaker(name = "geocoding", fallbackMethod = "geocodeFallback")
    public Optional<Location> geocode(String address) {
        return geocodingClient.geocode(address);
    }
    
    private Optional<Location> geocodeFallback(String address, Exception e) {
        log.error("Geocoding circuit open, using fallback", e);
        return Optional.empty();
    }
}
```

---

## Boas Práticas

### 1. Cache

✅ **Implementar cache** para consultas frequentes
```java
@Cacheable("address-cache")
public AddressResponse findByCep(String cep) { ... }
```

### 2. Timeouts

✅ **Configurar timeouts** adequados (já implementado em RestTemplateConfig)
```java
factory.setConnectTimeout(3000);  // 3 segundos
factory.setReadTimeout(5000);     // 5 segundos
```

### 3. Logging

✅ **Logar erros** para troubleshooting
```java
log.error("Failed to geocode address: {}", address, e);
```

### 4. Validação

✅ **Validar entrada** antes de chamar APIs externas
```java
if (cep == null || !cep.matches("\\d{8}")) {
    throw new BadRequestException("CEP inválido");
}
```

### 5. Tratamento de Exceções

✅ **Tratar exceções** de forma apropriada
```java
try {
    // Chamada à API
} catch (ExternalServiceException e) {
    // Logar e decidir: retry, fallback ou propagar
}
```

### 6. Rate Limiting (Opcional)

✅ **Implementar rate limiting** para proteger APIs de uso excessivo
```java
@RateLimiter(name = "viaCep", fallbackMethod = "rateLimitFallback")
public ViaCepAddress findByCep(String cep) { ... }
```

### 7. Monitoramento

✅ **Monitorar** chamadas a APIs externas
```java
@Timed(value = "geocoding.requests", description = "Geocoding API calls")
public Optional<Location> geocode(String address) { ... }
```

### 8. Segurança

✅ **Proteger API keys**
- Não commitar no código
- Usar variáveis de ambiente
- Rotacionar periodicamente

✅ **Restringir API keys**
- Por IP (servidor)
- Por API (apenas APIs necessárias)
- Por cota (limite de uso)

---

## Testes

### Exemplo de Teste com Mock

```java
@ExtendWith(MockitoExtension.class)
class ViaCepClientTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private AddressLookupProperties properties;
    
    @InjectMocks
    private ViaCepClient viaCepClient;
    
    @Test
    void findByCep_Success() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        
        ViaCepAddress expectedAddress = new ViaCepAddress();
        expectedAddress.cep = "01310-100";
        expectedAddress.logradouro = "Avenida Paulista";
        
        when(properties.getViacepBaseUrl())
            .thenReturn("https://viacep.com.br/ws");
        when(restTemplate.getForEntity(url, ViaCepAddress.class))
            .thenReturn(ResponseEntity.ok(expectedAddress));
        
        // Act
        ViaCepAddress result = viaCepClient.findByCep(cep);
        
        // Assert
        assertNotNull(result);
        assertEquals("01310-100", result.cep);
        assertEquals("Avenida Paulista", result.logradouro);
    }
    
    @Test
    void findByCep_NotFound() {
        // Arrange
        String cep = "99999999";
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        
        ViaCepAddress errorAddress = new ViaCepAddress();
        errorAddress.erro = true;
        
        when(properties.getViacepBaseUrl())
            .thenReturn("https://viacep.com.br/ws");
        when(restTemplate.getForEntity(url, ViaCepAddress.class))
            .thenReturn(ResponseEntity.ok(errorAddress));
        
        // Act
        ViaCepAddress result = viaCepClient.findByCep(cep);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.erro);
    }
}
```

---

## Diagrama de Sequência

### Fluxo: Criar Contato com Geolocalização

```
Usuario -> Controller -> Service -> GoogleGeocodingClient -> Google API
                           |
                           v
                      ViaCepClient -> ViaCEP API
                           |
                           v
                       Repository
                           |
                           v
                        Database
```

**Sequência Detalhada**:

1. Usuário envia dados do contato (incluindo CEP)
2. Controller recebe e valida requisição
3. Service valida CEP via ViaCepClient
4. ViaCepClient busca endereço completo no ViaCEP
5. Service monta endereço completo
6. Service chama GoogleGeocodingClient para obter coordenadas
7. GoogleGeocodingClient consulta Google Geocoding API
8. Service cria entidade Contact com todos os dados
9. Repository salva no banco de dados
10. Service retorna ContactResponse ao Controller
11. Controller retorna resposta HTTP ao usuário

---

## Resumo

### ViaCepClient

- ✅ Busca endereços por CEP
- ✅ Busca endereços por UF/Cidade/Rua
- ✅ API gratuita e sem limites
- ✅ Apenas endereços brasileiros
- ❌ Não fornece coordenadas

### GoogleGeocodingClient

- ✅ Converte endereços em coordenadas (lat/lng)
- ✅ Cobertura global
- ✅ Alta precisão
- ❌ Requer API key
- ❌ Tem limites e custos

### Uso Combinado

A aplicação usa **ambos** os clientes:
1. **ViaCEP** - Para validar e completar endereços brasileiros
2. **Google Geocoding** - Para obter coordenadas geográficas

Essa combinação oferece a melhor experiência:
- Dados de endereço confiáveis (ViaCEP)
- Geolocalização precisa (Google)

---

**Última atualização**: Dezembro 2025  
**APIs Utilizadas**:
- [ViaCEP API](https://viacep.com.br) - v1
- [Google Geocoding API](https://developers.google.com/maps/documentation/geocoding) - v1
