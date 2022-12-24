package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.HeadIconService;
import com.soku.rebotcorner.utils.FileUtil;
import com.soku.rebotcorner.utils.UserDAO;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Service
public class HeadIconServiceImpl implements HeadIconService {

  @Override
  public Map<String, String> updateHeadIcon(MultipartFile multipartFile) throws FileNotFoundException {
    User user = null;
    try {
      UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) token.getPrincipal();
      user = loginUser.getUser();
    } catch (Exception e) {
      Map<String, String> map = new HashMap<>();
      map.put("result", "failed");
      return map;
    }

    String filename = user.getUsername() + ".png";

    JSONObject json = FileUtil.saveAndGetIcon(multipartFile, filename, "/user");

    // 修改用户信息
    String newUrl = json.getStr("newUrl");
    user.setHeadIcon(newUrl);
    UserDAO.mapper.updateById(user);
    Map<String, String> map = new HashMap<>();
    map.put("result", "ok");
    map.put("url", newUrl);
    return map;
  }
}
