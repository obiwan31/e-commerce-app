package com.ecommerce.product.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaIdempotencyService {
  private static final String PROCESSED_PREFIX = "kafka:event:processed:";
  private static final String PROCESSING_PREFIX = "kafka:event:processing:";

  private final RedisTemplate<String, Object> redisTemplate;
  private final Duration processedTtl;
  private final Duration processingLockTtl;

  public KafkaIdempotencyService(
      RedisTemplate<String, Object> redisTemplate,
      @Value("${app.kafka.idempotency.processed-ttl:7d}") Duration processedTtl,
      @Value("${app.kafka.idempotency.processing-lock-ttl:5m}") Duration processingLockTtl) {
    this.redisTemplate = redisTemplate;
    this.processedTtl = processedTtl;
    this.processingLockTtl = processingLockTtl;
  }

  public boolean isAlreadyProcessed(String eventId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(PROCESSED_PREFIX + eventId));
  }

  public boolean acquireProcessingLock(String eventId) {
    Boolean acquired =
        redisTemplate.opsForValue().setIfAbsent(PROCESSING_PREFIX + eventId, "LOCKED", processingLockTtl);
    return Boolean.TRUE.equals(acquired);
  }

  public void markProcessed(String eventId) {
    redisTemplate.opsForValue().set(PROCESSED_PREFIX + eventId, "PROCESSED", processedTtl);
    redisTemplate.delete(PROCESSING_PREFIX + eventId);
  }

  public void releaseProcessingLock(String eventId) {
    redisTemplate.delete(PROCESSING_PREFIX + eventId);
  }
}
