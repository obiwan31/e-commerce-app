package com.ecommerce.user.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  @Bean
  public RequestInterceptor correlationIdInterceptor() {
    return requestTemplate -> {
      String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
      if (correlationId != null && !correlationId.isBlank()) {
        requestTemplate.header(CORRELATION_ID_HEADER, correlationId);
      }

      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs != null) {
        HttpServletRequest request = attrs.getRequest();
        String requestCorrelationId = request.getHeader(CORRELATION_ID_HEADER);
        if (requestCorrelationId != null && !requestCorrelationId.isBlank()) {
          requestTemplate.header(CORRELATION_ID_HEADER, requestCorrelationId);
        }
      }
    };
  }
}
