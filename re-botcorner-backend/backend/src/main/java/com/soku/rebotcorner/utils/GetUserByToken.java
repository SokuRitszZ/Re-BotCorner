package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.pojo.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class GetUserByToken {
  public static User get() {
    UsernamePasswordAuthenticationToken authenticate =
      (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
    User user = loginUser.getUser();
    return user;
  }
}
