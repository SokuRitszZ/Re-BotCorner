package com.soku.rebotcorner.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class RT {
  @Autowired
  public static RestTemplate restTemplate;

  public static String GET(String url, MultiValueMap<String, String> data) {
    HttpHeaders headers = new HttpHeaders();
    HttpMethod method = HttpMethod.GET;
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(data, headers);
    ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
    return response.getBody();
  }

  public static String POST(String url, MultiValueMap<String, String> data) {
    HttpHeaders headers = new HttpHeaders();
    HttpMethod method = HttpMethod.POST;
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(data, headers);
    ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
    return response.getBody();
  }
}
