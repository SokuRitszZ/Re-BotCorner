package com.soku.rebotcorner.utils;

import java.util.Random;

public class RandomUtil {
  private static Random random = new Random();

  public static String randomNumbers(int num) {
    String result = "";
    while (num-- > 0) result += random.nextInt(10);
    return result;
  }
}
