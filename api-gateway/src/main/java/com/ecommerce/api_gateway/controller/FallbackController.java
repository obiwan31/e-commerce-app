package com.ecommerce.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FallbackController {

  @GetMapping("/fallback")
  public ResponseEntity<String> fallback() {
    return ResponseEntity.ok("Service is temporarily unavailable, please try again later.");
  }
}
