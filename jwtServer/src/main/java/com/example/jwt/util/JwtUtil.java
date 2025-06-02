package com.example.jwt.util;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final Key key;
	//private final long accessTokenValidity = 1000L * 30;       // 30초
	//private final long refreshTokenValidity = 1000L * 60 * 1;  // 1분
    private final long accessTokenValidity = 1000L * 60 * 30; // 30분
    private final long refreshTokenValidity = 1000L * 60 * 60 * 24 * 7; // 7일

    // application.properties에 있는 jwt.secret값 얻어오기
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Access Token 생성
    public String generateAccessToken(Long memberNo, String email) {
        return Jwts.builder()
                .setSubject("AccessToken")
                .claim("memberNo", memberNo)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long memberNo, String email) {
        return Jwts.builder()
                .setSubject("RefreshToken")
                .claim("memberNo", memberNo)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 클레임 추출
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 유효한 토큰인지 검증(
    // 1. 서명 불일치
    // 2. 만료 시간 경과 (exp)
    // 3.구조적 오류
    //4. 기타 JWT 관련 오류)
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // 토큰에서 이메일 추출
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // 토큰에서 회원번호 추출
    public Long getMemberNo(String token) {
    	try {
            return getClaims(token).get("memberNo", Long.class);
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이라도 claims는 유효
            return e.getClaims().get("memberNo", Long.class);
        }
    }

    // 토큰 만료일자 추출
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }
}
