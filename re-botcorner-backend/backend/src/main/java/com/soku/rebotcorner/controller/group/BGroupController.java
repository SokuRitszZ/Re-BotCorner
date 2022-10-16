package com.soku.rebotcorner.controller.group;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.group.BGroupService;
import com.soku.rebotcorner.utils.Res;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequestMapping("/api/group")
@RestController
public class BGroupController {
  @Autowired
  private BGroupService groupService;

  @PostMapping("/create")
  Res createGroup(@RequestPart("file") MultipartFile file, @RequestPart("title") String title, @RequestPart("description") String description) {
    JSONObject json = new JSONObject();
    json.put("title", title);
    json.put("description", description);
    return groupService.createGroup(file, json);
  }

  @PostMapping("/delete")
  Res deleteGroup(@RequestBody JSONObject json) {
    Integer id = json.getInt("id");
    return groupService.deleteGroup(id);
  }

  @GetMapping("/list")
  Res groupList() {
    return groupService.groupList();
  }

  @GetMapping("/getById")
  Res getById(@RequestParam Map<String, String> data) {
    Integer id = Integer.valueOf(data.get("id"));
    return groupService.getById(id);
  }
}
