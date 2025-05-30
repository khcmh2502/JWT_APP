package com.example.jwt.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	// 클라이언트에서 오는 Cross-Origin 요청(CORS)을 어떻게 처리할지 설정
	@Override
    public void addCorsMappings(CorsRegistry registry) { 
        registry.addMapping("/**") // 서버의 모든 API 경로(/**)에 대해 CORS 설정을 적용
            .allowedOrigins("http://localhost:5173") // 이 주소에서 오는 요청만 허용
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 클라이언트가 사용할 수 있는 HTTP 메서드를 지정
            .allowedHeaders("*") // 클라이언트가 보낼 수 있는 헤더를 모두 허용
            .allowCredentials(true) // 브라우저가 쿠키, 인증 정보 등을 포함해서 요청할 수 있도록 허용
            .maxAge(3600); // 브라우저가 CORS preflight 요청(OPTIONS)을 캐싱할 시간(초)
    }
	
}
