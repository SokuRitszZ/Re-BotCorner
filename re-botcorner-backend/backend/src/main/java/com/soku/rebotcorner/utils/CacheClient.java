package com.soku.rebotcorner.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.soku.rebotcorner.utils.RedisConstants.CACHE_NULL_TTL;
import static com.soku.rebotcorner.utils.RedisConstants.LOCK_BOT_KEY;

public class CacheClient {
  public static StringRedisTemplate redis;

  /**
   * 设置带时间的值
   *
   * @param key
   * @param value
   * @param time
   * @param unit
   */
  public static void set(String key, Object value, Long time, TimeUnit unit) {
    redis.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
  }

  /**
   * 设置带逻辑过期
   *
   * @param key
   * @param value
   * @param time
   * @param unit
   */
  public static void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
    // 设置逻辑过期
    RedisData data = new RedisData();
    data.setData(value);
    data.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
    // 写入
    redis.opsForValue().set(key, JSONUtil.toJsonStr(data));
  }

  /**
   * 写缓存解决缓存穿透的访问
   *
   * @param keyPrefix
   * @param id
   * @param type
   * @param dbFallback
   * @param time
   * @param unit
   * @return
   * @param <R>
   * @param <ID>
   */
  public static <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
    // 从redis获取缓存
    String key = keyPrefix + id;
    String json = redis.opsForValue().get(key);
    // 是否存在
    if (StrUtil.isNotBlank(json))
      // 存在则返回
      return JSONUtil.toBean(json, type);
    // 判断命中的是否为空值
    if (json != null) {
      System.out.println("Get Null.");
      // 返回错误信息
      return null;
    }
    // 根据id查找db
    R r = dbFallback.apply(id);
    if (r == null) {
      redis.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
      return null;
    }

    // 写缓存
    set(key, r, time, unit);

    return r;
  }

  private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

  /**
   * 逻辑过期解决缓存击穿
   *
   * @param keyPrefix
   * @param id
   * @param type
   * @param dbFallback
   * @param time
   * @param unit
   * @return
   * @param <R>
   * @param <ID>
   */
  public static <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
    String key = keyPrefix + id;
    String json = redis.opsForValue().get(key);
    if (StrUtil.isBlank(json))
      // 未命中
      return null;
    RedisData redisData = JSONUtil.toBean(json, RedisData.class);
    R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
    LocalDateTime expireTime = redisData.getExpireTime();
    if (!expireTime.isAfter(LocalDateTime.now())) {
      // 过期，缓存重建
      String lockKey = LOCK_BOT_KEY + id;
      boolean isLock = tryLock(lockKey);
      if (isLock){
        CACHE_REBUILD_EXECUTOR.submit(() -> {
          try {
            R r1 = dbFallback.apply(id);
            setWithLogicalExpire(key, r1, time, unit);
          } catch(Exception e) {
            System.out.println(e.getMessage());
          } finally {
            unlock(lockKey);
          }
        });
      }
    }
    return r;
  }

  /**
   * Redis实现互斥锁
   *
   * @param lockKey
   * @return
   */
  public static boolean tryLock(String lockKey) {
    Boolean flag = redis.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
    return BooleanUtil.isTrue(flag);
  }

  /**
   * 互斥锁解锁
   *
   * @param lockKey
   */
  public static void unlock(String lockKey) {
    redis.delete(lockKey);
  }

  /**
   * 检验key是否存在
   *
   * @param key
   * @return
   */
  public static boolean containsKey(String key) {
    return redis.hasKey(key);
  }

  /**
   * 使用通配符批量获取
   *
   * @param pattern
   * @return
   */
  public static List<String> getByPattern(String pattern) {
    Set<String> keys = redis.keys(pattern);
    return redis.opsForValue().multiGet(keys);
  }

  public static boolean deleteKey(String key) {
    return redis.delete(key);
  }
}
