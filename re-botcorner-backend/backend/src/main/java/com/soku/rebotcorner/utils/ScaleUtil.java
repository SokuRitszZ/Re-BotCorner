package com.soku.rebotcorner.utils;

public class ScaleUtil {
  // max_scale = 36(number 10 + char 26)
  public static String transform(int num, int scale) {
    StringBuilder sb = new StringBuilder();
    do {
      int apd = num % scale;
      if (apd < 10) sb.append(Character.toChars('0' + apd));
      else sb.append(Character.toChars('A' + apd - 10));
      num /= scale;
    } while (num > 0);
    return sb.toString();
  }

  public static int parse(String str, int scale) {
    int result = 0;
    int n = str.length();
    for (int i = 0; i < n; ++i) {
      result *= scale;
      char c = str.charAt(i);
      if (c <= '9') result += c - '0';
      else result += c - 'A';
    }
    return result;
  }
}
