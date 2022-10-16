package com.soku.rebotcorner.service.account;

import java.util.Map;

public interface PhoneAuthorizeService {
  Map<String, String> phoneAuthorize(String phone);
}
