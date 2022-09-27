package com.soku.rebotcorner.utils;

import io.jsonwebtoken.Claims;

public class JwtAuthenticationUtil {
  public static Integer getUserId(String token) {
    String userid = "-1";
    try {
      Claims claims = JwtUtil.parseJWT(token);
      userid = claims.getSubject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return Integer.parseInt(userid);
  }
}
