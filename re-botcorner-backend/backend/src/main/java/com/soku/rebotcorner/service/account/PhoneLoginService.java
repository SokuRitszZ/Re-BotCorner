package com.soku.rebotcorner.service.account;

import java.util.Map;

public interface PhoneLoginService {
  Map<String, String> phoneLogin(String phone, String authCode);
}
