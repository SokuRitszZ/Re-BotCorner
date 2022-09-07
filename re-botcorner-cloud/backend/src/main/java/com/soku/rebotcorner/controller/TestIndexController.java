package com.soku.rebotcorner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestIndexController {
  @RequestMapping("/")
  public String index() {
    return "index.html";
  }
}
