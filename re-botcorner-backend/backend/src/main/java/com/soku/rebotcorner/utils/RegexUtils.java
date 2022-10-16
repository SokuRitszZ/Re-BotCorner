package com.soku.rebotcorner.utils;

import java.util.regex.Pattern;

public class RegexUtils {
  private static Pattern phoneRegex = Pattern.compile("^1[0-9]{10}$");

  /**
   * 验证手机号格式
   * @param {String} phone
   * @return {boolean}
   */
  public static boolean isValidPhone(String phone) {
    return phoneRegex.matcher(phone).matches();
  }
}
