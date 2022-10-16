package com.soku.rebotcorner.service.impl.account;

import com.soku.rebotcorner.service.account.PhoneAuthorizeService;
import com.soku.rebotcorner.service.account.PhoneLoginService;
import com.soku.rebotcorner.utils.RandomUtil;
import com.soku.rebotcorner.utils.RegexUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.soku.rebotcorner.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.soku.rebotcorner.utils.RedisConstants.LOGIN_CODE_TTL;

@Service
public class PhoneAuthorizeServiceImpl implements PhoneAuthorizeService {
  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Map<String, String> phoneAuthorize(String phone) {
    Map<String, String> map = new HashMap<>();
    // 1. 验证手机号格式
    if (!RegexUtils.isValidPhone(phone)) {
      // 2. 不符合则返回错误
      map.put("result", "fail");
      map.put("message", "手机号格式错误");
      return map;
    }

    // 3. 符合则生成验证码
    String code = RandomUtil.randomNumbers(6);

    // 4. 保存验证码到Redis
    // 保存2分钟有效
    stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

    // 5. 发送验证码
    map.put("result", "success");
    map.put("message", "发送成功");
    return map;
  }
}
