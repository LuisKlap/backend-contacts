# Documentação - Pacote Util

Este documento detalha todas as classes utilitárias do projeto, localizadas em `src/main/java/com/dev/contacts/util/`.

## Índice

1. [Visão Geral](#visão-geral)
2. [CpfValidator](#cpfvalidator)
3. [PhoneUtils](#phoneutils)
4. [Casos de Uso](#casos-de-uso)
5. [Testes](#testes)

---

## Visão Geral

O pacote `util` contém classes utilitárias estáticas para validação e formatação de dados. Essas classes fornecem métodos reutilizáveis para manipulação de CPF e telefones.

### Características Comuns

- **Classes Finais**: Não podem ser herdadas
- **Construtor Privado**: Não podem ser instanciadas
- **Métodos Estáticos**: Todos os métodos são estáticos
- **Thread-Safe**: Métodos são stateless e thread-safe
- **Sem Dependências**: Não dependem do Spring ou outras bibliotecas

### Utilitários Disponíveis

| Classe           | Propósito                                                     | Principais Métodos                                                                 |
| ---------------- | ------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| **CpfValidator** | Validação e limpeza de CPF                                    | `clean()`, `isValid()`                                                             |
| **PhoneUtils**   | Validação, normalização e formatação de telefones brasileiros | `normalizeToNational()`, `isValidBrazilianPhone()`, `formatReadable()`, `toE164()` |

---

## CpfValidator

**Arquivo**: `CpfValidator.java`

### Descrição

Utilitário para validação e limpeza de números de CPF (Cadastro de Pessoas Físicas) brasileiro. Implementa o algoritmo oficial de validação de dígitos verificadores.

### Estrutura da Classe

```java
public final class CpfValidator {
    // Construtor privado - impede instanciação
    private CpfValidator() {}
    
    // Métodos públicos estáticos
    public static String clean(String cpf) { ... }
    public static boolean isValid(String cpf) { ... }
    
    // Método privado auxiliar
    private static int calcularDigito(String cpf, int pesoInicial) { ... }
}
```

---

### Métodos

#### clean(String cpf)

**Descrição**: Remove todos os caracteres não numéricos de uma string de CPF.

**Parâmetros**:
- `cpf` - CPF em qualquer formato

**Retorno**: `String`
- String contendo apenas dígitos
- `null` se o parâmetro for `null`

**Uso**: Preparar CPF para validação ou armazenamento.

---

**Exemplos**:

```java
// Remove pontos e hífen
CpfValidator.clean("123.456.789-00");
// Retorna: "12345678900"

// Remove espaços e caracteres especiais
CpfValidator.clean("123 456 789-00");
// Retorna: "12345678900"

// Já limpo
CpfValidator.clean("12345678900");
// Retorna: "12345678900"

// CPF null
CpfValidator.clean(null);
// Retorna: null

// Apenas caracteres especiais
CpfValidator.clean("...-");
// Retorna: ""
```

---

**Implementação**:

```java
public static String clean(String cpf) {
    if (cpf == null) {
        return null;
    }
    return cpf.replaceAll("\\D", "");  // Remove tudo que não for dígito
}
```

**Regex Explicada**:
- `\\D` - Qualquer caractere que NÃO seja dígito (0-9)
- `replaceAll()` - Substitui todas as ocorrências por string vazia

---

#### isValid(String cpf)

**Descrição**: Valida se um CPF é válido de acordo com as regras oficiais da Receita Federal do Brasil.

**Parâmetros**:
- `cpf` - CPF em qualquer formato (será limpo automaticamente)

**Retorno**: `boolean`
- `true` - CPF válido
- `false` - CPF inválido ou `null`

**Regras de Validação**:

1. ✅ **Não pode ser null**
2. ✅ **Deve ter exatamente 11 dígitos** (após limpeza)
3. ✅ **Não pode ter todos os dígitos iguais** (ex: "111.111.111-11")
4. ✅ **Primeiro dígito verificador** deve ser válido
5. ✅ **Segundo dígito verificador** deve ser válido

---

**Algoritmo de Validação de CPF**:

O CPF tem formato: `XXX.XXX.XXX-YZ`
- `XXX.XXX.XXX` - 9 primeiros dígitos (número base)
- `Y` - Primeiro dígito verificador
- `Z` - Segundo dígito verificador

**Cálculo do Primeiro Dígito Verificador (Y)**:

```
Posição:   1   2   3   4   5   6   7   8   9
CPF:       1   2   3   4   5   6   7   8   9
Peso:     10   9   8   7   6   5   4   3   2

Soma = (1×10) + (2×9) + (3×8) + (4×7) + (5×6) + (6×5) + (7×4) + (8×3) + (9×2)
Soma = 10 + 18 + 24 + 28 + 30 + 30 + 28 + 24 + 18 = 210

Resto = Soma % 11 = 210 % 11 = 1

Se Resto < 2: Dígito = 0
Senão: Dígito = 11 - Resto

Dígito = 11 - 1 = 10 (mas como deve ser 1 dígito, considera-se 0)
```

**Cálculo do Segundo Dígito Verificador (Z)**:

```
Posição:   1   2   3   4   5   6   7   8   9   Y
CPF:       1   2   3   4   5   6   7   8   9   0
Peso:     11  10   9   8   7   6   5   4   3   2

Soma = (1×11) + (2×10) + ... + (9×3) + (0×2)

Resto = Soma % 11

Se Resto < 2: Dígito = 0
Senão: Dígito = 11 - Resto
```

---

**Implementação**:

```java
public static boolean isValid(String cpf) {
    if (cpf == null) {
        return false;
    }

    // Limpa CPF (remove formatação)
    String digits = clean(cpf);

    // Verifica se tem 11 dígitos
    if (digits.length() != 11) {
        return false;
    }

    // Verifica se todos os dígitos são iguais (CPF inválido conhecido)
    if (digits.chars().distinct().count() == 1) {
        return false;
    }

    try {
        // Calcula e valida primeiro dígito verificador
        int d1 = calcularDigito(digits, 10);
        
        // Calcula e valida segundo dígito verificador
        int d2 = calcularDigito(digits, 11);

        // Compara com os dígitos verificadores do CPF
        return d1 == Character.getNumericValue(digits.charAt(9))
            && d2 == Character.getNumericValue(digits.charAt(10));
    } catch (Exception e) {
        return false;
    }
}
```

---

**Exemplos**:

```java
// CPF válido (com formatação)
CpfValidator.isValid("123.456.789-09");
// Retorna: true

// CPF válido (sem formatação)
CpfValidator.isValid("12345678909");
// Retorna: true

// CPF inválido (dígito verificador errado)
CpfValidator.isValid("123.456.789-00");
// Retorna: false

// CPF inválido (todos dígitos iguais)
CpfValidator.isValid("111.111.111-11");
// Retorna: false

// CPF inválido (menos de 11 dígitos)
CpfValidator.isValid("123.456.789");
// Retorna: false

// CPF null
CpfValidator.isValid(null);
// Retorna: false

// String vazia
CpfValidator.isValid("");
// Retorna: false
```

---

**CPFs Inválidos Conhecidos** (todos dígitos iguais):

```java
CpfValidator.isValid("000.000.000-00");  // false
CpfValidator.isValid("111.111.111-11");  // false
CpfValidator.isValid("222.222.222-22");  // false
CpfValidator.isValid("333.333.333-33");  // false
CpfValidator.isValid("444.444.444-44");  // false
CpfValidator.isValid("555.555.555-55");  // false
CpfValidator.isValid("666.666.666-66");  // false
CpfValidator.isValid("777.777.777-77");  // false
CpfValidator.isValid("888.888.888-88");  // false
CpfValidator.isValid("999.999.999-99");  // false
```

---

#### calcularDigito(String cpf, int pesoInicial) [PRIVADO]

**Descrição**: Método auxiliar privado que calcula um dígito verificador do CPF.

**Parâmetros**:
- `cpf` - CPF limpo (11 dígitos)
- `pesoInicial` - Peso inicial (10 para primeiro dígito, 11 para segundo)

**Retorno**: `int` - Dígito verificador calculado (0-9)

**Implementação**:

```java
private static int calcularDigito(String cpf, int pesoInicial) {
    int soma = 0;
    int peso = pesoInicial;

    // Multiplica cada dígito pelo seu peso e soma
    for (int i = 0; i < pesoInicial - 1; i++) {
        soma += Character.getNumericValue(cpf.charAt(i)) * peso;
        peso--;
    }

    // Calcula o resto da divisão por 11
    int resto = soma % 11;
    
    // Aplica regra: se resto < 2, dígito é 0, senão é 11 - resto
    return (resto < 2) ? 0 : 11 - resto;
}
```

---

### Uso no Projeto

#### No Service (Validação)

```java
@Service
public class ContactService {
    
    public ContactResponse createContact(User owner, ContactRequest request) {
        // Validar CPF
        if (!CpfValidator.isValid(request.cpf())) {
            throw new BadRequestException("CPF inválido");
        }
        
        // Limpar CPF antes de salvar
        String cleanCpf = CpfValidator.clean(request.cpf());
        
        // Verificar se CPF já existe para este usuário
        if (contactRepository.existsByOwnerAndCpf(owner, cleanCpf)) {
            throw new ConflictException("CPF já cadastrado");
        }
        
        // Criar contato
        Contact contact = new Contact();
        contact.setCpf(cleanCpf);
        // ... outros campos ...
        
        return contactRepository.save(contact);
    }
}
```

#### No DTO (Validação Customizada)

```java
@RecordValidation
public record ContactRequest(
    @NotBlank String name,
    
    @ValidCpf  // Anotação customizada
    String cpf,
    
    // ... outros campos ...
) {}

// Validador customizado
@Constraint(validatedBy = CpfValidator.class)
public @interface ValidCpf {
    String message() default "CPF inválido";
    // ...
}
```

---

### Observações

- **Thread-Safe**: Métodos estáticos e sem estado
- **Performance**: Algoritmo O(n) onde n é o tamanho do CPF (sempre 11)
- **Padrão Oficial**: Implementa algoritmo da Receita Federal
- **Armazenamento**: Recomenda-se armazenar CPF sem formatação (apenas dígitos)

---

## PhoneUtils

**Arquivo**: `PhoneUtils.java`

### Descrição

Utilitário abrangente para manipulação de números de telefone brasileiros. Oferece funcionalidades de limpeza, normalização, validação e formatação de telefones fixos e móveis.

### Estrutura da Classe

```java
public final class PhoneUtils {
    // Construtor privado - impede instanciação
    private PhoneUtils() {}
    
    // Métodos públicos
    public static String onlyDigits(String phone) { ... }
    public static String normalizeToNational(String phone) { ... }
    public static boolean isValidBrazilianPhone(String phone) { ... }
    public static String toE164(String phone) { ... }
    public static String formatReadable(String phone) { ... }
    
    // Métodos privados auxiliares
    private static boolean isAllDigits(String s) { ... }
    private static boolean validDdd(String ddd) { ... }
}
```

---

### Conceitos - Telefones Brasileiros

#### Formato Nacional

**Telefone Fixo** (10 dígitos):
```
(11) 3456-7890
 ^^  ^^^^^^^^^
 |   |
 DDD Número (8 dígitos)
      2-5: primeiro dígito
```

**Telefone Móvel** (11 dígitos):
```
(11) 98765-4321
 ^^  ^^^^^^^^^^
 |   |
 DDD Número (9 dígitos)
      9: primeiro dígito obrigatório
```

#### DDD (Código de Área)

- **Valores válidos**: 11 a 99
- **Exemplos**: 11 (São Paulo), 21 (Rio de Janeiro), 47 (Joinville)

#### Formato E.164 (Internacional)

Padrão internacional de telefonia:
```
+5511987654321
 ^^ ^^ ^^^^^^^^^
 |  |  |
 |  |  Número (9 dígitos)
 |  DDD
 Código do país (Brasil = 55)
```

---

### Métodos

---

#### 1. onlyDigits(String phone)

**Descrição**: Remove todos os caracteres não numéricos de um telefone.

**Parâmetros**:
- `phone` - Telefone em qualquer formato

**Retorno**: `String`
- String contendo apenas dígitos
- String vazia `""` se o parâmetro for `null`

**Exemplos**:

```java
// Remove formatação
PhoneUtils.onlyDigits("(11) 98765-4321");
// Retorna: "11987654321"

// Remove espaços e símbolos
PhoneUtils.onlyDigits("+55 11 9 8765-4321");
// Retorna: "5511987654321"

// Já limpo
PhoneUtils.onlyDigits("11987654321");
// Retorna: "11987654321"

// Null retorna vazio
PhoneUtils.onlyDigits(null);
// Retorna: ""
```

**Implementação**:

```java
public static String onlyDigits(String phone) {
    if (phone == null)
        return "";
    return phone.replaceAll("\\D", "");  // Remove não-dígitos
}
```

---

#### 2. normalizeToNational(String phone)

**Descrição**: Normaliza um telefone para o formato nacional brasileiro (sem código de país). Remove prefixos internacionais e códigos de operadora.

**Parâmetros**:
- `phone` - Telefone em qualquer formato

**Retorno**: `String` - Telefone normalizado (10 ou 11 dígitos)

**Regras de Normalização**:

1. Remove todos os caracteres não numéricos
2. Remove prefixo internacional "00"
3. Remove código do país "+55" ou "55"
4. Mantém apenas DDD + número

**Exemplos**:

```java
// Formato internacional E.164
PhoneUtils.normalizeToNational("+5511987654321");
// Retorna: "11987654321"

// Com código de país
PhoneUtils.normalizeToNational("5511987654321");
// Retorna: "11987654321"

// Com 00 (discagem internacional antiga)
PhoneUtils.normalizeToNational("005511987654321");
// Retorna: "11987654321"

// Já no formato nacional
PhoneUtils.normalizeToNational("(11) 98765-4321");
// Retorna: "11987654321"

// Telefone fixo
PhoneUtils.normalizeToNational("(11) 3456-7890");
// Retorna: "1134567890"
```

**Implementação**:

```java
public static String normalizeToNational(String phone) {
    String digits = onlyDigits(phone);

    // Remove prefixo "00" (discagem internacional)
    while (digits.startsWith("00")) {
        digits = digits.substring(2);
    }

    // Remove código do país "55" se houver mais de 11 dígitos
    if (digits.startsWith("55") && digits.length() > 11) {
        digits = digits.substring(2);
    }

    // Remove "55" de números com 12 ou 13 dígitos (55 + 10/11 dígitos)
    if (digits.startsWith("55") && 
        (digits.length() == 12 || digits.length() == 13)) {
        digits = digits.substring(2);
    }

    return digits;
}
```

---

#### 3. isValidBrazilianPhone(String phone)

**Descrição**: Valida se um telefone brasileiro é válido de acordo com as regras da ANATEL.

**Parâmetros**:
- `phone` - Telefone em qualquer formato (será normalizado)

**Retorno**: `boolean`
- `true` - Telefone válido
- `false` - Telefone inválido

**Regras de Validação**:

**Telefone Fixo (10 dígitos)**:
- ✅ DDD válido (11-99)
- ✅ Primeiro dígito: 2, 3, 4 ou 5
- ✅ Total: 10 dígitos

**Telefone Móvel (11 dígitos)**:
- ✅ DDD válido (11-99)
- ✅ Primeiro dígito: 9 (obrigatório)
- ✅ Total: 11 dígitos

**Exemplos**:

```java
// Celular válido
PhoneUtils.isValidBrazilianPhone("(11) 98765-4321");
// Retorna: true

PhoneUtils.isValidBrazilianPhone("+5511987654321");
// Retorna: true

// Telefone fixo válido
PhoneUtils.isValidBrazilianPhone("(11) 3456-7890");
// Retorna: true

// Celular inválido (não começa com 9)
PhoneUtils.isValidBrazilianPhone("(11) 88765-4321");
// Retorna: false

// Fixo inválido (começa com 9)
PhoneUtils.isValidBrazilianPhone("(11) 9876-5432");
// Retorna: false

// DDD inválido
PhoneUtils.isValidBrazilianPhone("(00) 98765-4321");
// Retorna: false

// Poucos dígitos
PhoneUtils.isValidBrazilianPhone("(11) 9876-543");
// Retorna: false
```

**Implementação**:

```java
public static boolean isValidBrazilianPhone(String phone) {
    String nat = normalizeToNational(phone);
    
    // Valida telefone fixo (10 dígitos)
    if (nat.length() == 10) {
        if (!validDdd(nat.substring(0, 2)) || !isAllDigits(nat)) {
            return false;
        }
        char firstDigit = nat.charAt(2);
        return firstDigit >= '2' && firstDigit <= '5';
    } 
    // Valida telefone móvel (11 dígitos)
    else if (nat.length() == 11) {
        if (!validDdd(nat.substring(0, 2)) || !isAllDigits(nat)) {
            return false;
        }
        return nat.charAt(2) == '9';
    }
    
    return false;
}
```

---

#### 4. toE164(String phone)

**Descrição**: Converte um telefone brasileiro para o formato E.164 (padrão internacional).

**Parâmetros**:
- `phone` - Telefone brasileiro em qualquer formato

**Retorno**: `String` - Telefone no formato E.164 (`+55DDNNNNNNNNN`)

**Exceções**:
- `IllegalArgumentException` - Se o telefone não for válido

**Formato E.164**:
```
+55 11 987654321
 ^^ ^^ ^^^^^^^^^
 |  |  Número
 |  DDD
 Código do país
```

**Exemplos**:

```java
// Celular
PhoneUtils.toE164("(11) 98765-4321");
// Retorna: "+5511987654321"

// Fixo
PhoneUtils.toE164("(11) 3456-7890");
// Retorna: "+551134567890"

// Já normalizado
PhoneUtils.toE164("11987654321");
// Retorna: "+5511987654321"

// Telefone inválido
PhoneUtils.toE164("123");
// Lança: IllegalArgumentException
```

**Implementação**:

```java
public static String toE164(String phone) {
    String nat = normalizeToNational(phone);
    
    if (!isValidBrazilianPhone(nat)) {
        throw new IllegalArgumentException(
            "Telefone inválido para formato E.164: " + phone);
    }
    
    return "+55" + nat;
}
```

**Uso**: Armazenar telefones em banco de dados ou integração com APIs internacionais.

---

#### 5. formatReadable(String phone)

**Descrição**: Formata um telefone para uma representação legível com parênteses, espaços e hífen.

**Parâmetros**:
- `phone` - Telefone em qualquer formato

**Retorno**: `String` - Telefone formatado de forma legível

**Formatos de Saída**:

| Dígitos              | Formato           | Exemplo           |
| -------------------- | ----------------- | ----------------- |
| 11 (Celular com DDD) | `(DD) NNNNN-NNNN` | `(11) 98765-4321` |
| 10 (Fixo com DDD)    | `(DD) NNNN-NNNN`  | `(11) 3456-7890`  |
| 9 (Celular sem DDD)  | `NNNNN-NNNN`      | `98765-4321`      |
| 8 (Fixo sem DDD)     | `NNNN-NNNN`       | `3456-7890`       |
| Outros               | Sem formatação    | `123`             |

**Exemplos**:

```java
// Celular com DDD (11 dígitos)
PhoneUtils.formatReadable("11987654321");
// Retorna: "(11) 98765-4321"

// Fixo com DDD (10 dígitos)
PhoneUtils.formatReadable("1134567890");
// Retorna: "(11) 3456-7890"

// Celular sem DDD (9 dígitos)
PhoneUtils.formatReadable("987654321");
// Retorna: "98765-4321"

// Fixo sem DDD (8 dígitos)
PhoneUtils.formatReadable("34567890");
// Retorna: "3456-7890"

// Já formatado (normaliza)
PhoneUtils.formatReadable("+55 11 98765-4321");
// Retorna: "(11) 98765-4321"

// Poucos dígitos (retorna como está)
PhoneUtils.formatReadable("123");
// Retorna: "123"
```

**Implementação**:

```java
public static String formatReadable(String phone) {
    String digits = onlyDigits(phone);
    String nat = normalizeToNational(digits);

    // Celular com DDD (11 dígitos): (11) 98765-4321
    if (nat.length() == 11) {
        String ddd = nat.substring(0, 2);
        String part1 = nat.substring(2, 7);
        String part2 = nat.substring(7);
        return String.format("(%s) %s-%s", ddd, part1, part2);
    } 
    // Fixo com DDD (10 dígitos): (11) 3456-7890
    else if (nat.length() == 10) {
        String ddd = nat.substring(0, 2);
        String part1 = nat.substring(2, 6);
        String part2 = nat.substring(6);
        return String.format("(%s) %s-%s", ddd, part1, part2);
    } 
    // Celular sem DDD (9 dígitos): 98765-4321
    else if (nat.length() == 9) {
        String part1 = nat.substring(0, 5);
        String part2 = nat.substring(5);
        return String.format("%s-%s", part1, part2);
    } 
    // Fixo sem DDD (8 dígitos): 3456-7890
    else if (nat.length() == 8) {
        String part1 = nat.substring(0, 4);
        String part2 = nat.substring(4);
        return String.format("%s-%s", part1, part2);
    }

    // Retorna normalizado se não se encaixar nos formatos
    return nat;
}
```

---

#### validDdd(String ddd) [PRIVADO]

**Descrição**: Valida se um DDD (código de área) é válido no Brasil.

**Parâmetros**:
- `ddd` - Código de área (2 dígitos)

**Retorno**: `boolean`

**DDDs Válidos**: 11 a 99

**Implementação**:

```java
private static boolean validDdd(String ddd) {
    if (ddd == null || ddd.length() != 2 || !ddd.matches("\\d{2}"))
        return false;
    
    int value = Integer.parseInt(ddd);
    return value >= 11 && value <= 99;
}
```

**Exemplos de DDDs**:

| DDD | Cidade Principal |
| --- | ---------------- |
| 11  | São Paulo        |
| 21  | Rio de Janeiro   |
| 27  | Vitória          |
| 31  | Belo Horizonte   |
| 41  | Curitiba         |
| 47  | Joinville        |
| 48  | Florianópolis    |
| 51  | Porto Alegre     |
| 61  | Brasília         |
| 85  | Fortaleza        |

---

#### isAllDigits(String s) [PRIVADO]

**Descrição**: Verifica se uma string contém apenas dígitos.

**Parâmetros**:
- `s` - String a verificar

**Retorno**: `boolean`

**Implementação**:

```java
private static boolean isAllDigits(String s) {
    return s != null && s.matches("\\d+");
}
```

---

### Uso no Projeto

#### No Service (Validação e Armazenamento)

```java
@Service
public class ContactService {
    
    public ContactResponse createContact(User owner, ContactRequest request) {
        // Validar telefone
        if (!PhoneUtils.isValidBrazilianPhone(request.phone())) {
            throw new BadRequestException("Telefone inválido");
        }
        
        // Normalizar telefone (remover formatação)
        String normalizedPhone = PhoneUtils.normalizeToNational(request.phone());
        
        // Criar contato
        Contact contact = new Contact();
        contact.setPhone(normalizedPhone);
        // ... outros campos ...
        
        return contactRepository.save(contact);
    }
}
```

#### No DTO (Response com Formatação)

```java
public record ContactResponse(
    Long id,
    String name,
    String cpf,
    String phone,
    // ... outros campos ...
) {
    public static ContactResponse fromEntity(Contact contact) {
        return new ContactResponse(
            contact.getId(),
            contact.getName(),
            formatCpf(contact.getCpf()),  // Formatar CPF
            PhoneUtils.formatReadable(contact.getPhone()),  // Formatar telefone
            // ... outros campos ...
        );
    }
}
```

#### Em Integração (Conversão para E.164)

```java
@Service
public class SmsService {
    
    public void sendSms(String phone, String message) {
        // Converter para formato internacional antes de enviar
        String e164Phone = PhoneUtils.toE164(phone);
        
        // Enviar SMS via API externa
        smsClient.send(e164Phone, message);
    }
}
```

---

### Estratégias de Armazenamento

#### Opção 1: Armazenar Normalizado (Recomendado)

```java
// Armazenar: "11987654321"
contact.setPhone(PhoneUtils.normalizeToNational(inputPhone));

// Exibir: "(11) 98765-4321"
response.setPhone(PhoneUtils.formatReadable(contact.getPhone()));
```

**Vantagens**:
- Facilita buscas e comparações
- Economiza espaço
- Consistência no banco

#### Opção 2: Armazenar em E.164

```java
// Armazenar: "+5511987654321"
contact.setPhone(PhoneUtils.toE164(inputPhone));

// Exibir: "(11) 98765-4321"
response.setPhone(PhoneUtils.formatReadable(contact.getPhone()));
```

**Vantagens**:
- Padrão internacional
- Pronto para APIs externas
- Identifica país automaticamente

---

### Observações

- **Thread-Safe**: Todos os métodos são estáticos e stateless
- **ANATEL**: Regras de validação seguem padrões da ANATEL
- **9º Dígito**: Celulares brasileiros sempre têm 9 como primeiro dígito (desde 2016)
- **Performance**: Operações de string são otimizadas
- **Flexibilidade**: Aceita qualquer formato de entrada

---

## Casos de Uso

### Caso 1: Cadastro de Contato

```java
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    
    @PostMapping
    public ContactResponse create(@RequestBody ContactRequest request) {
        // 1. Validar CPF
        if (!CpfValidator.isValid(request.cpf())) {
            throw new BadRequestException("CPF inválido");
        }
        
        // 2. Validar telefone
        if (!PhoneUtils.isValidBrazilianPhone(request.phone())) {
            throw new BadRequestException("Telefone inválido");
        }
        
        // 3. Normalizar dados
        String cleanCpf = CpfValidator.clean(request.cpf());
        String normalizedPhone = PhoneUtils.normalizeToNational(request.phone());
        
        // 4. Verificar duplicatas
        if (contactService.existsByCpf(cleanCpf)) {
            throw new ConflictException("CPF já cadastrado");
        }
        
        // 5. Criar contato
        Contact contact = Contact.builder()
            .name(request.name())
            .cpf(cleanCpf)
            .phone(normalizedPhone)
            .build();
        
        // 6. Salvar e retornar
        return contactService.save(contact);
    }
}
```

### Caso 2: Busca por CPF ou Telefone

```java
@Service
public class ContactService {
    
    public List<Contact> searchByCpfOrPhone(String query) {
        // Limpar query
        String cleanQuery = query.replaceAll("\\D", "");
        
        // Tentar como CPF (11 dígitos)
        if (cleanQuery.length() == 11 && CpfValidator.isValid(cleanQuery)) {
            return contactRepository.findByCpf(cleanQuery);
        }
        
        // Tentar como telefone (10 ou 11 dígitos)
        if (cleanQuery.length() >= 10 && PhoneUtils.isValidBrazilianPhone(cleanQuery)) {
            String normalizedPhone = PhoneUtils.normalizeToNational(cleanQuery);
            return contactRepository.findByPhone(normalizedPhone);
        }
        
        // Query inválida
        return List.of();
    }
}
```

### Caso 3: Exportação com Formatação

```java
@Service
public class ExportService {
    
    public void exportContactsToCsv(List<Contact> contacts, OutputStream output) {
        PrintWriter writer = new PrintWriter(output);
        
        // Header
        writer.println("Nome,CPF,Telefone");
        
        // Dados formatados
        contacts.forEach(contact -> {
            String formattedCpf = formatCpf(contact.getCpf());
            String formattedPhone = PhoneUtils.formatReadable(contact.getPhone());
            
            writer.printf("%s,%s,%s%n", 
                contact.getName(), 
                formattedCpf, 
                formattedPhone);
        });
        
        writer.flush();
    }
    
    private String formatCpf(String cpf) {
        if (cpf.length() != 11) return cpf;
        return String.format("%s.%s.%s-%s",
            cpf.substring(0, 3),
            cpf.substring(3, 6),
            cpf.substring(6, 9),
            cpf.substring(9, 11));
    }
}
```

### Caso 4: Integração com API de SMS

```java
@Service
public class NotificationService {
    private final SmsApiClient smsClient;
    
    public void notifyContact(Contact contact, String message) {
        // Validar telefone antes de enviar
        if (!PhoneUtils.isValidBrazilianPhone(contact.getPhone())) {
            log.warn("Telefone inválido, notificação não enviada: {}", 
                contact.getPhone());
            return;
        }
        
        // Converter para formato E.164 (necessário para API internacional)
        String e164Phone = PhoneUtils.toE164(contact.getPhone());
        
        // Enviar SMS
        smsClient.send(e164Phone, message);
        
        log.info("SMS enviado para {}", 
            PhoneUtils.formatReadable(contact.getPhone()));
    }
}
```

---

## Testes

### Testes para CpfValidator

```java
@Test
class CpfValidatorTest {
    
    @Test
    void clean_shouldRemoveFormatting() {
        assertEquals("12345678909", CpfValidator.clean("123.456.789-09"));
        assertEquals("12345678909", CpfValidator.clean("123 456 789-09"));
        assertEquals("", CpfValidator.clean("...---"));
        assertNull(CpfValidator.clean(null));
    }
    
    @Test
    void isValid_shouldAcceptValidCpf() {
        assertTrue(CpfValidator.isValid("123.456.789-09"));
        assertTrue(CpfValidator.isValid("12345678909"));
    }
    
    @Test
    void isValid_shouldRejectInvalidCpf() {
        assertFalse(CpfValidator.isValid("123.456.789-00"));  // Dígito errado
        assertFalse(CpfValidator.isValid("111.111.111-11"));  // Todos iguais
        assertFalse(CpfValidator.isValid("123.456.789"));     // Poucos dígitos
        assertFalse(CpfValidator.isValid(null));
        assertFalse(CpfValidator.isValid(""));
    }
}
```

### Testes para PhoneUtils

```java
@Test
class PhoneUtilsTest {
    
    @Test
    void onlyDigits_shouldRemoveNonDigits() {
        assertEquals("11987654321", PhoneUtils.onlyDigits("(11) 98765-4321"));
        assertEquals("11987654321", PhoneUtils.onlyDigits("+55 11 98765-4321"));
        assertEquals("", PhoneUtils.onlyDigits(null));
    }
    
    @Test
    void normalizeToNational_shouldRemoveCountryCode() {
        assertEquals("11987654321", PhoneUtils.normalizeToNational("+5511987654321"));
        assertEquals("11987654321", PhoneUtils.normalizeToNational("5511987654321"));
        assertEquals("11987654321", PhoneUtils.normalizeToNational("005511987654321"));
    }
    
    @Test
    void isValidBrazilianPhone_shouldValidateMobile() {
        assertTrue(PhoneUtils.isValidBrazilianPhone("(11) 98765-4321"));
        assertTrue(PhoneUtils.isValidBrazilianPhone("11987654321"));
        assertTrue(PhoneUtils.isValidBrazilianPhone("+5511987654321"));
    }
    
    @Test
    void isValidBrazilianPhone_shouldValidateLandline() {
        assertTrue(PhoneUtils.isValidBrazilianPhone("(11) 3456-7890"));
        assertTrue(PhoneUtils.isValidBrazilianPhone("1134567890"));
    }
    
    @Test
    void isValidBrazilianPhone_shouldRejectInvalid() {
        assertFalse(PhoneUtils.isValidBrazilianPhone("(11) 88765-4321"));  // Não começa com 9
        assertFalse(PhoneUtils.isValidBrazilianPhone("(00) 98765-4321"));  // DDD inválido
        assertFalse(PhoneUtils.isValidBrazilianPhone("123"));               // Poucos dígitos
    }
    
    @Test
    void toE164_shouldConvertToInternational() {
        assertEquals("+5511987654321", PhoneUtils.toE164("(11) 98765-4321"));
        assertEquals("+551134567890", PhoneUtils.toE164("(11) 3456-7890"));
    }
    
    @Test
    void toE164_shouldThrowOnInvalid() {
        assertThrows(IllegalArgumentException.class, 
            () -> PhoneUtils.toE164("123"));
    }
    
    @Test
    void formatReadable_shouldFormatCorrectly() {
        assertEquals("(11) 98765-4321", PhoneUtils.formatReadable("11987654321"));
        assertEquals("(11) 3456-7890", PhoneUtils.formatReadable("1134567890"));
        assertEquals("98765-4321", PhoneUtils.formatReadable("987654321"));
        assertEquals("3456-7890", PhoneUtils.formatReadable("34567890"));
    }
}
```

---

## Resumo

### CpfValidator

**Funcionalidades**:
- ✅ Limpeza de CPF (remoção de formatação)
- ✅ Validação completa (algoritmo oficial)
- ✅ Detecção de CPFs inválidos conhecidos

**Uso Principal**:
- Validação de entrada de usuário
- Normalização antes de armazenar no banco
- Verificação de duplicatas

### PhoneUtils

**Funcionalidades**:
- ✅ Limpeza de telefone
- ✅ Normalização para formato nacional
- ✅ Validação de telefones brasileiros
- ✅ Conversão para E.164 (internacional)
- ✅ Formatação legível

**Uso Principal**:
- Validação de entrada de usuário
- Normalização para armazenamento
- Formatação para exibição
- Integração com APIs externas (SMS, WhatsApp)

---

**Última atualização**: Dezembro 2025  
**Padrões Seguidos**:
- Algoritmo oficial de CPF (Receita Federal)
- Regras ANATEL para telefones
- Formato E.164 (ITU-T)
