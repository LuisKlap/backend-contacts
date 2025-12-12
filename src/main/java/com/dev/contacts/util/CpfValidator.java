package com.dev.contacts.util;

public final class CpfValidator {

  private CpfValidator() {
  }

  public static String clean(String cpf) {
    if (cpf == null) {
      return null;
    }
    return cpf.replaceAll("\\D", "");
  }

  public static boolean isValid(String cpf) {
    if (cpf == null) {
      return false;
    }

    String digits = clean(cpf);

    if (digits.length() != 11) {
      return false;
    }

    if (digits.chars().distinct().count() == 1) {
      return false;
    }

    try {
      int d1 = calcularDigito(digits, 10);
      int d2 = calcularDigito(digits, 11);

      return d1 == Character.getNumericValue(digits.charAt(9))
          && d2 == Character.getNumericValue(digits.charAt(10));
    } catch (Exception e) {
      return false;
    }
  }

  private static int calcularDigito(String cpf, int pesoInicial) {
    int soma = 0;
    int peso = pesoInicial;

    for (int i = 0; i < pesoInicial - 1; i++) {
      soma += Character.getNumericValue(cpf.charAt(i)) * peso;
      peso--;
    }

    int resto = soma % 11;
    return (resto < 2) ? 0 : 11 - resto;
  }
}
