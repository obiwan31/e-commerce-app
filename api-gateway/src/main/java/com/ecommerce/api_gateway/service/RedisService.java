package com.ecommerce.api_gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class RedisService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * Check if recently deleted user exists in the cache to block login
   *
   * @param userId User Id
   * @return boolean
   */
  public Mono<Boolean> isUserDeleted(Long userId) {
    String key = "deletedUser:" + userId;
    return Mono.fromCallable(() -> redisTemplate.hasKey(key))
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<Boolean> hasLoginAttemptExceeded(Long userId) {
    String key = "loginAttempts:" + userId;

    return Mono.fromCallable(
            () -> {
              Object attemptsObj = redisTemplate.opsForValue().get(key);
              int attempts = 0;

              if (attemptsObj instanceof Integer val) {
                attempts = val;
              } else if (attemptsObj instanceof String val) {
                try {
                  attempts = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                  LOGGER.error(e.getMessage());
                }
              }
              return attempts > 10;
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<Boolean> isTokenAvailable(Long userId) {
    String key = "refreshToken:" + userId;
    return Mono.fromCallable(() -> redisTemplate.hasKey(key))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
