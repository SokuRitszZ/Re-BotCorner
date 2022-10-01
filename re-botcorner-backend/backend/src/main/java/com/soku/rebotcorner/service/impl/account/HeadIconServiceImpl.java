package com.soku.rebotcorner.service.impl.account;

import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.HeadIconService;
import com.soku.rebotcorner.utils.UserDAO;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
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

    // 获取文件，并命名
    String filename = user.getUsername() + ".png";

    // 获取项目类路径
//    String path = ResourceUtils.getURL("classpath:").getPath();
    String path = System.getProperty("user.dir");

    File uploadDir = new File(path + "/static/profile/");
    if (!uploadDir.exists()) uploadDir.mkdir();

    // 保存进来
    File localPath = new File(path + "/static/profile/", filename);
    if (!localPath.exists()) localPath.mkdirs();
    try {
      multipartFile.transferTo(localPath);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // 修改用户信息
    String[] url = new String[]{"http://localhost:8080/static/profile", "https://app3495.acapp.acwing.com.cn/static/profile"};
    String newHeadIcon = url[0] + "/" + filename;
    user.setHeadIcon(newHeadIcon);
    UserDAO.updateById(user);
    Map<String, String> map = new HashMap<>();
    map.put("result", "ok");
    map.put("url", newHeadIcon);
    return map;
  }
}
