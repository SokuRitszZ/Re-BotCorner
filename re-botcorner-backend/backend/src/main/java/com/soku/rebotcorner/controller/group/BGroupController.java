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

  @PostMapping("/apply")
  Res apply(@RequestBody JSONObject json) {
    Integer groupId = json.getInt("groupId");
    String application = json.getStr("application");
    return groupService.apply(groupId, application);
  }

  @GetMapping("/application")
  Res getApplication() {
    return groupService.getApplication();
  }

  @PostMapping("/handleApp")
  Res handleApplication(@RequestBody JSONObject json) {
    Integer groupId = json.getInt("groupId");
    Integer applicantId = json.getInt("applicantId");
    Boolean state = json.getBool("state");
    return groupService.handleApplication(groupId, applicantId, state);
  }

  @GetMapping("/members")
  Res getMembers(@RequestParam Map<String, String> data) {
    Integer groupId = Integer.parseInt(data.get("groupId"));
    return groupService.getMembers(groupId);
  }

  @PostMapping("/resign")
  Res resign(@RequestBody JSONObject json) {
    Integer groupId = json.getInt("groupId");
    return groupService.resign(groupId);
  }
}