package com.soku.rebotcorner.utils;

public class RedisConstants {
  public static final String LOGIN_CODE_KEY = "login:code:";
  public static final Long LOGIN_CODE_TTL = 2L;
  public static final String GAME_KEY = "rebc:game";
  public static final Long GAME_TTL = 2L;
  public static final String LANG_KEY = "rebc:lang";
  public static final Long LANG_TTL = 2L;
  public static final String CACHE_USER_KEY = "rebc:lang";
  public static final Long CACHE_NULL_TTL = 10L;
  public static final String CACHE_BOT_KEY = "rebc:bot:";
  public static final Long CACHE_BOT_TTL = 2L;
  public static final String LOCK_BOT_KEY = "rebc:lock:bot:";
  public static final String CACHE_APPLICATION_KEY = "rebc:application:";
}