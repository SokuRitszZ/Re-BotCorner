package com.soku.rebotcorner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestIndexController {
  @RequestMapping("/")
  public String index() {
    return "index.html";
  }
}
