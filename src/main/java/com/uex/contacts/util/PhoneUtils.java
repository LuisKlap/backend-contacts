package com.uex.contacts.util;

public final class PhoneUtils {

  private PhoneUtils() {
  }

  public static String onlyDigits(String phone) {
    if (phone == null)
      return "";
    return phone.replaceAll("\\D", "");
  }

  public static String normalizeToNational(String phone) {
    String digits = onlyDigits(phone);

    while (digits.startsWith("00")) {
      digits = digits.substring(2);
    }

    if (digits.startsWith("55") && digits.length() > 11) {
      digits = digits.substring(2);
    }

    if (digits.startsWith("55") && (digits.length() == 12 || digits.length() == 13)) {
      digits = digits.substring(2);
    }

    return digits;
  }

  public static boolean isValidBrazilianPhone(String phone) {
    String nat = normalizeToNational(phone);
    if (nat.length() == 10) {
      return validDdd(nat.substring(0, 2)) && isAllDigits(nat);
    } else if (nat.length() == 11) {
      return validDdd(nat.substring(0, 2)) && isAllDigits(nat);
    }
    return false;
  }

  public static String toE164(String phone) {
    String nat = normalizeToNational(phone);
    if (!isValidBrazilianPhone(nat)) {
      throw new IllegalArgumentException("Telefone inválido para formato E.164: " + phone);
    }
    return "+55" + nat;
  }

  public static String formatReadable(String phone) {
    String digits = onlyDigits(phone);
    String nat = normalizeToNational(digits);

    if (nat.length() == 11) {
      String ddd = nat.substring(0, 2);
      String part1 = nat.substring(2, 7);
      String part2 = nat.substring(7);
      return String.format("(%s) %s-%s", ddd, part1, part2);
    } else if (nat.length() == 10) {
      String ddd = nat.substring(0, 2);
      String part1 = nat.substring(2, 6);
      String part2 = nat.substring(6);
      return String.format("(%s) %s-%s", ddd, part1, part2);
    } else if (nat.length() == 9) {
      String part1 = nat.substring(0, 5);
      String part2 = nat.substring(5);
      return String.format("%s-%s", part1, part2);
    } else if (nat.length() == 8) {
      String part1 = nat.substring(0, 4);
      String part2 = nat.substring(4);
      return String.format("%s-%s", part1, part2);
    }

    return nat;
  }

  private static boolean isAllDigits(String s) {
    return s != null && s.matches("\\d+");
  }

  private static boolean validDdd(String ddd) {
    if (ddd == null || ddd.length() != 2 || !ddd.matches("\\d{2}"))
      return false;
    int value = Integer.parseInt(ddd);
    return value >= 11 && value <= 99;
  }
}
