package com.soku.rebotcorner.service.impl.account;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.account.acwing.WebService;
import org.springframework.stereotype.Service;

@Service
public class WebServiceImpl implements WebService {
  @Override
  public JSONObject applyCode() {
    return null;
  }

  @Override
  public JSONObject receiveCode(String code, String state) {
    return null;
  }
}
