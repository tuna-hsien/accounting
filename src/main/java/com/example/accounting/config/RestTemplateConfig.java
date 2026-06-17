package com.example.accounting.config;

// 記得補上這個 import
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30)); 
        return new RestTemplate(factory);
    }

    // 👇 就是加了這一段！手動生一個 ObjectMapper 給 Spring Boot 系統用
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}