package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.HeadIconService;
import com.soku.rebotcorner.utils.FileUtil;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDAO;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

@Service
public class HeadIconServiceImpl implements HeadIconService {

  @Override
  public JSONObject updateHeadIcon(MultipartFile multipartFile) throws FileNotFoundException {
    User user = null;
    try {
      UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) token.getPrincipal();
      user = loginUser.getUser();
    } catch (Exception e) {
      return NewRes.fail("上传失败，请重试");
    }

    String filename = user.getUsername() + ".png";

    JSONObject json = FileUtil.saveAndGetUrl(multipartFile, filename, "/user");

    // 修改用户信息
    String newUrl = json.getStr("newUrl");
    user.setHeadIcon(newUrl);
    UserDAO.mapper.updateById(user);
    return NewRes.ok(
      new JSONObject()
        .set("url", newUrl)
    );
  }
}
