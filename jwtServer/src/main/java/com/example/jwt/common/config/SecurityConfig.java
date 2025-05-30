package com.example.jwt.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
 *  @Configuration
 *  - 설정용 클래스임 명시
 *  + 객체로 생성해서 내부 코드를 서버 실행시 모두 수행
 *  
 *  @Bean
 *  - 개발자가 수동으로 생성한 객체의 관리를
 *    스프링에게 넘기는 어노테이션 (Bean 등록)
 * 
 * */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()) // CORS 기본 허용
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 🔥 OPTIONS
																											// 요청은 허용
						.requestMatchers("/auth/**").permitAll() // 로그인/회원가입 등도 허용
						.anyRequest().authenticated());

		return http.build();
	}
}
