package com.soku.rebotcorner.controller.bot;

import com.soku.rebotcorner.service.bot.DeleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeleteController {
  @Autowired
  private DeleteService deleteService;

  @DeleteMapping("/bot/delete")
  public Map<String, String> deleteBot(@RequestParam Map<String, String> data) {
    return deleteService.deleteBot(data);
  }
}
