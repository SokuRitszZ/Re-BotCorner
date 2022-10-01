package com.soku.rebotcorner.controller.account;

import com.soku.rebotcorner.service.account.HeadIconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.Map;

@RestController
public class HeadIconController {
  @Autowired
  private HeadIconService headIconService;

  @PostMapping("/api/updateHeadIcon")
  public Map<String, String> updateHeadIcon(@RequestPart("file") MultipartFile multipartFile) throws FileNotFoundException {
    return headIconService.updateHeadIcon(multipartFile);
  }
}
