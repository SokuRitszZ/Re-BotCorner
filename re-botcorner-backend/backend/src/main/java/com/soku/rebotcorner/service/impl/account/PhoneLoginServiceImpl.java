package com.soku.rebotcorner.service.impl.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.PhoneLoginService;
import com.soku.rebotcorner.utils.JwtUtil;
import com.soku.rebotcorner.utils.RandomUtil;
import com.soku.rebotcorner.utils.RegexUtils;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.soku.rebotcorner.utils.RedisConstants.LOGIN_CODE_KEY;

@Service
public class PhoneLoginServiceImpl implements PhoneLoginService {
  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public Map<String, String> phoneLogin(String phone, String authCode) {
    Map<String, String> map = new HashMap<>();
    map.put("result", "fail");
    // 1. 验证手机号
    if (!RegexUtils.isValidPhone(phone)) {
      map.put("message", "手机号格式错误");
      return map;
    }

    // 2. 校验验证码
    // 从Redis获取手机号对应的验证码，然后比对
    String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
    if (cacheCode == null || !cacheCode.equals(authCode)) {
      // 3. 不一致，报错
      map.put("message", "验证码不一致");
      return map;
    }

    // 4. 一致，通过手机号查询用户
    User user = UserDAO.selectOne(new QueryWrapper<User>().eq("phone", phone));

    // 5. 判断用户是否存在
    if (user == null)
      // 6. 不存在则创建
      user = createUserWithPhone(phone);

    // 7. 返回JWT
    String jwt = JwtUtil.createJWT(user.getId().toString());
    map.put("result", "success");
    map.put("jwt", jwt);
    return map;
  }

  private User createUserWithPhone(String phone) {
    User user = new User(
      null,
      String.format("用户%s", phone),
      passwordEncoder.encode("123456"),
      "https://sdfsdf.dev/500x500.jpg,0000ff,ffff00",
      1500,
      null,
      phone
    );
    while (UserDAO.selectList(new QueryWrapper<User>().eq("username", user.getUsername())).size() > 0)
      user.setUsername(user.getUsername() + RandomUtil.randomNumbers(1));
    UserDAO.insert(user);
    return user;
  }
}
