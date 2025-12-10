package com.dev.contacts.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CpfValidator Tests")
class CpfValidatorTest {

  @Test
  @DisplayName("Deve limpar CPF removendo caracteres não numéricos")
  void shouldCleanCpf() {
    assertThat(CpfValidator.clean("123.456.789-09")).isEqualTo("12345678909");
    assertThat(CpfValidator.clean("123 456 789 09")).isEqualTo("12345678909");
    assertThat(CpfValidator.clean("123-456-789-09")).isEqualTo("12345678909");
    assertThat(CpfValidator.clean("12345678909")).isEqualTo("12345678909");
  }

  @Test
  @DisplayName("Deve retornar null ao limpar CPF null")
  void shouldReturnNullWhenCleaningNullCpf() {
    assertThat(CpfValidator.clean(null)).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "97843149082",
      "11144477735",
      "52998224725",
      "12345678909"
  })
  @DisplayName("Deve validar CPFs válidos")
  void shouldValidateValidCpfs(String cpf) {
    assertThat(CpfValidator.isValid(cpf)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "123.456.789-09",
      "111.444.777-35"
  })
  @DisplayName("Deve validar CPFs válidos com formatação")
  void shouldValidateFormattedValidCpfs(String cpf) {
    assertThat(CpfValidator.isValid(cpf)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "00000000000",
      "11111111111",
      "22222222222",
      "33333333333",
      "44444444444",
      "55555555555",
      "66666666666",
      "77777777777",
      "88888888888",
      "99999999999"
  })
  @DisplayName("Deve invalidar CPFs com todos os dígitos iguais")
  void shouldInvalidateCpfWithAllSameDigits(String cpf) {
    assertThat(CpfValidator.isValid(cpf)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "97843149081", // CPF válido seria 97843149082
      "12345678900", // CPF válido seria 12345678909
      "11144477736" // CPF válido seria 11144477735
  })
  @DisplayName("Deve invalidar CPFs com dígitos verificadores incorretos")
  void shouldInvalidateCpfWithIncorrectCheckDigits(String cpf) {
    assertThat(CpfValidator.isValid(cpf)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "123456789",
      "12345678901234",
      "123"
  })
  @DisplayName("Deve invalidar CPFs com tamanho incorreto")
  void shouldInvalidateCpfWithIncorrectLength(String cpf) {
    assertThat(CpfValidator.isValid(cpf)).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "   ", "\t", "\n" })
  @DisplayName("Deve invalidar CPFs vazios ou nulos")
  void shouldInvalidateEmptyOrNullCpf(String cpf) {
    assertThat(CpfValidator.isValid(cpf)).isFalse();
  }

  @Test
  @DisplayName("Deve calcular corretamente o primeiro dígito verificador")
  void shouldCalculateFirstCheckDigitCorrectly() {
    // CPF: 111.444.777-35
    // Primeiros 9 dígitos: 111444777
    // Primeiro dígito verificador deve ser 3
    assertThat(CpfValidator.isValid("11144477735")).isTrue();
  }

  @Test
  @DisplayName("Deve calcular corretamente o segundo dígito verificador")
  void shouldCalculateSecondCheckDigitCorrectly() {
    // CPF: 111.444.777-35
    // Primeiros 10 dígitos: 1114447773
    // Segundo dígito verificador deve ser 5
    assertThat(CpfValidator.isValid("11144477735")).isTrue();
  }

  @Test
  @DisplayName("Deve validar CPF com zeros no início")
  void shouldValidateCpfWithLeadingZeros() {
    // CPF válido com zeros no início
    assertThat(CpfValidator.isValid("00000000192")).isFalse(); // Dígito verificador incorreto
    assertThat(CpfValidator.isValid("00000000191")).isTrue(); // CPF válido com zeros
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "abc.def.ghi-jk",
      "###.###.###-##",
      "xxx-xxx-xxx-xx"
  })
  @DisplayName("Deve invalidar CPFs com caracteres inválidos")
  void shouldInvalidateCpfWithInvalidCharacters(String cpf) {
    assertThat(CpfValidator.isValid(cpf)).isFalse();
  }

  @Test
  @DisplayName("Deve limpar CPF com espaços")
  void shouldCleanCpfWithSpaces() {
    assertThat(CpfValidator.clean("  123.456.789-09  ")).isEqualTo("12345678909");
  }

  @Test
  @DisplayName("Deve validar CPF limpo versus não limpo")
  void shouldValidateCleanedVsUncleanedCpf() {
    String cpfFormatted = "111.444.777-35";
    String cpfCleaned = CpfValidator.clean(cpfFormatted);

    assertThat(CpfValidator.isValid(cpfFormatted)).isTrue();
    assertThat(CpfValidator.isValid(cpfCleaned)).isTrue();
  }
}
