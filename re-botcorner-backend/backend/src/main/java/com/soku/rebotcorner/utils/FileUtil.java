package com.soku.rebotcorner.utils;

import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static com.soku.rebotcorner.utils.Constancts.MODE;

@Component
public class FileUtil {
  public static JSONObject saveAndGetIcon(MultipartFile file, String filename, String path) {
    JSONObject result = new JSONObject();
    result.put("result", "fail");

    // 项目类路径
    String localUrl = System.getProperty("user.dir") + "/static/profile" + path;

    File localPath = new File(localUrl, filename);
    if (!localPath.exists()) localPath.mkdirs();

    try {
      file.transferTo(localPath);
    } catch (Exception e) {
      System.out.println("err = " + e.getMessage());
      return result;
    }
    String[] url = new String[]{"http://localhost:8080/static/profile", "https://app3495.acapp.acwing.com.cn/static/profile"};
    result.put("result", "success");
    result.put("newUrl", url[MODE] + path + "/" + filename);

    return result;
  }
}
