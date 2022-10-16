package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.pojo.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

  public static User getCurrentUser() {
    UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
    return loginUser.getUser();
  }
}
