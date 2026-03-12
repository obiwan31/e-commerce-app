package com.ecommerce.product.config;

import com.ecommerce.product.exception.BadRequestException;
import com.ecommerce.product.exception.ConflictException;
import com.ecommerce.product.exception.EventSchemaValidationException;
import com.ecommerce.product.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class ProductConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductConfig.class);
  private static final String ORDER_PLACED_TOPIC = "order.placed.v1";
  private static final String ORDER_PLACED_DLT_TOPIC = "order.placed.v1.dlt";

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return template;
  }

  @Bean
  public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
    ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
    backOff.setInitialInterval(1000L);
    backOff.setMultiplier(2.0);
    backOff.setMaxInterval(30000L);

    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(ORDER_PLACED_DLT_TOPIC, record.partition()));

    DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
    errorHandler.addNotRetryableExceptions(
        IllegalArgumentException.class,
        BadRequestException.class,
        EventSchemaValidationException.class,
        NotFoundException.class,
        ConflictException.class,
        ConstraintViolationException.class);
    errorHandler.setRetryListeners(
        (record, ex, attempt) ->
            LOGGER.warn(
                "Retrying kafka record topic={} partition={} offset={} attempt={} reason={}",
                record.topic(),
                record.partition(),
                record.offset(),
                attempt,
                ex.getMessage()));
    return errorHandler;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {

    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

    return factory;
  }

  @Bean
  public NewTopic orderPlacedTopic() {
    return TopicBuilder.name(ORDER_PLACED_TOPIC).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic orderPlacedDltTopic() {
    return TopicBuilder.name(ORDER_PLACED_DLT_TOPIC).partitions(3).replicas(1).build();
  }
}
