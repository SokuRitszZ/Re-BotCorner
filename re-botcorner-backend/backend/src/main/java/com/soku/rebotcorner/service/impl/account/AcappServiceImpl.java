package com.soku.rebotcorner.service.impl.account;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.acwing.AcappService;
import com.soku.rebotcorner.utils.HttpClientUtil;
import com.soku.rebotcorner.utils.JwtUtil;
import com.soku.rebotcorner.utils.UserDAO;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.*;

@Service
public class AcappServiceImpl implements AcappService {
  private final static String appId = "3495";
  private final static String appSecret = "869c5e44bc4848409dc8a67727037e69";
  private final static String redirectUri = "https://app3495.acapp.acwing.com.cn/api/user/account/acwing/acapp/receive_code/";
  private final static String applyAccessToken = "https://www.acwing.com/third_party/api/oauth2/access_token/";
  private final static String getUserInfoUrl = "https://www.acwing.com/third_party/api/meta/identity/getinfo/";
  private final static Random random = new Random();

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Override
  public JSONObject applyCode() {
    JSONObject resp = new JSONObject();
    resp.put("appid", appId);
    try {
      resp.put("redirect_uri", URLEncoder.encode(redirectUri, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      resp.put("result", "failed");
      return resp;
    }
    resp.put("scope", "userinfo");
    StringBuilder state = new StringBuilder("");
    for (int i = 0; i < 10; ++i) state.append((char) (random.nextInt(10) + '0'));
    resp.put("state", state.toString());
    resp.put("result", "success");
    redisTemplate.opsForValue().set(state.toString(), "true");
    redisTemplate.expire(state.toString(), Duration.ofMinutes(10)); // 10 min
    return resp;
  }

  @Override
  public JSONObject receiveCode(String code, String state) {
    JSONObject resp = new JSONObject();
    resp.put("result", "failed");
    if (code == null || state == null) {
      return resp;
    }
    if (Boolean.FALSE.equals(redisTemplate.hasKey(state))) {
      return resp;
    }
    redisTemplate.delete(state);
    List<NameValuePair> nameValuePairs = new LinkedList<>();
    nameValuePairs.add(new BasicNameValuePair("appid", appId));
    nameValuePairs.add(new BasicNameValuePair("secret", appSecret));
    nameValuePairs.add(new BasicNameValuePair("code", code));

    String getString = HttpClientUtil.get(applyAccessToken, nameValuePairs);
    if (getString == null) return resp;
    JSONObject getResp = JSONObject.parseObject(getString);
    String accessToken = getResp.getString("access_token");
    String openid = getResp.getString("openid");
    if (accessToken == null || openid == null) return resp;

    QueryWrapper<User> qw = new QueryWrapper<>();
    qw.eq("openid", openid);
    User user = UserDAO.selectOne(qw);
    if (user != null) {
      String jwt = JwtUtil.createJWT(user.getId().toString()); // 通过工具类创建JWT
      resp.put("result", "success");
      resp.put("jwt_token", jwt);
      return resp;
    }

    nameValuePairs = new LinkedList<>();
    nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
    nameValuePairs.add(new BasicNameValuePair("openid", openid));
    getString = HttpClientUtil.get(getUserInfoUrl, nameValuePairs);
    if (getString == null) return resp;
    getResp = JSONObject.parseObject(getString);
    StringBuilder username = new StringBuilder(getResp.getString("username"));
    for (int i = 0; i < 100; ++i) {
      QueryWrapper<User> qwx = new QueryWrapper<>();
      qwx.eq("username", username.toString());
      if (UserDAO.selectList(qwx).isEmpty()) break;
      username.append(random.nextInt(10));
      if (i == 99) return resp;
    }
    String headIcon = getResp.getString("photo");
    user = new User(
      null,
      username.toString(),
      null,
      headIcon,
      1500,
      openid,
      null
    );
    UserDAO.insert(user);
    String jwt = JwtUtil.createJWT(user.getId().toString());
    resp.put("result", "success");
    resp.put("jwt_token", jwt);
    return resp;
  }
}
