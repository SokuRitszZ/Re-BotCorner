package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


/** 用于实现UserDetails接口 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

  private User user;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    /** 账户是否已过期 */
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    /** 是否没有被锁定 */
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    /** 授权是否过期 */
    return true;
  }

  @Override
  public boolean isEnabled() {
    /** 代表用户是否被启用了 */
    return true;
  }
}
