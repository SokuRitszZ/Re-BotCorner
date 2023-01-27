package com.soku.rebotcorner.service.account;

import cn.hutool.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

public interface AvatarService {
  JSONObject updateAvatar(MultipartFile multipartFile) throws FileNotFoundException;
}
