package com.uex.contacts.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PhoneUtils Tests")
class PhoneUtilsTest {

  @Test
  @DisplayName("Deve remover todos os caracteres não numéricos")
  void shouldRemoveNonDigitCharacters() {
    assertThat(PhoneUtils.onlyDigits("(11) 98765-4321")).isEqualTo("11987654321");
    assertThat(PhoneUtils.onlyDigits("11 9 8765-4321")).isEqualTo("11987654321");
    assertThat(PhoneUtils.onlyDigits("+55 11 98765-4321")).isEqualTo("5511987654321");
    assertThat(PhoneUtils.onlyDigits("abc123def456")).isEqualTo("123456");
  }

  @Test
  @DisplayName("Deve retornar string vazia para null")
  void shouldReturnEmptyStringForNull() {
    assertThat(PhoneUtils.onlyDigits(null)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "5511987654321",
      "005511987654321",
      "+5511987654321",
      "11987654321"
  })
  @DisplayName("Deve normalizar telefones para formato nacional")
  void shouldNormalizeToNationalFormat(String phone) {
    String result = PhoneUtils.normalizeToNational(phone);
    assertThat(result).matches("^11\\d{9}$");
  }

  @ParameterizedTest
  @CsvSource({
      "11987654321, true",
      "1134567890, true",
      "21987654321, true",
      "123456789, false",
      "119876543210, false",
      "1187654321, false",
      "0187654321, false"
  })
  @DisplayName("Deve validar telefones brasileiros")
  void shouldValidateBrazilianPhones(String phone, boolean expected) {
    assertThat(PhoneUtils.isValidBrazilianPhone(phone)).isEqualTo(expected);
  }

  @Test
  @DisplayName("Deve converter para formato E.164")
  void shouldConvertToE164Format() {
    assertThat(PhoneUtils.toE164("11987654321")).isEqualTo("+5511987654321");
    assertThat(PhoneUtils.toE164("(11) 98765-4321")).isEqualTo("+5511987654321");
    assertThat(PhoneUtils.toE164("1134567890")).isEqualTo("+551134567890");
  }

  @Test
  @DisplayName("Deve lançar exceção para telefone inválido no E.164")
  void shouldThrowExceptionForInvalidPhoneInE164() {
    assertThatThrownBy(() -> PhoneUtils.toE164("123456789"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Telefone inválido para formato E.164");
  }

  @ParameterizedTest
  @CsvSource({
      "11987654321, (11) 98765-4321",
      "1134567890, (11) 3456-7890",
      "987654321, 98765-4321",
      "34567890, 3456-7890"
  })
  @DisplayName("Deve formatar telefone para leitura")
  void shouldFormatReadable(String phone, String expected) {
    assertThat(PhoneUtils.formatReadable(phone)).isEqualTo(expected);
  }

  @Test
  @DisplayName("Deve retornar apenas dígitos se formato não reconhecido")
  void shouldReturnDigitsForUnrecognizedFormat() {
    assertThat(PhoneUtils.formatReadable("12345")).isEqualTo("12345");
    assertThat(PhoneUtils.formatReadable("123")).isEqualTo("123");
  }

  @Test
  @DisplayName("Deve validar DDD válido")
  void shouldValidateValidDdd() {
    assertThat(PhoneUtils.isValidBrazilianPhone("11987654321")).isTrue();
    assertThat(PhoneUtils.isValidBrazilianPhone("85987654321")).isTrue();
    assertThat(PhoneUtils.isValidBrazilianPhone("47987654321")).isTrue();
  }

  @Test
  @DisplayName("Deve invalidar DDD inválido")
  void shouldInvalidateInvalidDdd() {
    assertThat(PhoneUtils.isValidBrazilianPhone("00987654321")).isFalse();
    assertThat(PhoneUtils.isValidBrazilianPhone("01987654321")).isFalse();
    assertThat(PhoneUtils.isValidBrazilianPhone("09987654321")).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "   ", "\t", "\n" })
  @DisplayName("Deve tratar entradas vazias ou nulas")
  void shouldHandleEmptyOrNullInputs(String phone) {
    assertThat(PhoneUtils.onlyDigits(phone)).isEmpty();
    assertThat(PhoneUtils.isValidBrazilianPhone(phone)).isFalse();
  }

  @Test
  @DisplayName("Deve remover prefixos 00 repetidos")
  void shouldRemoveRepeatedZeroPrefix() {
    String phone = "00005511987654321";
    String normalized = PhoneUtils.normalizeToNational(phone);
    assertThat(normalized).doesNotStartWith("00");
  }
}
