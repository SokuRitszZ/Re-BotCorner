package com.soku.rebotcorner.service.account;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.Map;

public interface HeadIconService {
  Map<String, String> updateHeadIcon(MultipartFile multipartFile) throws FileNotFoundException;
}
