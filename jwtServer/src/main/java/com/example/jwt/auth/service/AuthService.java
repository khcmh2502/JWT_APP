package com.example.jwt.auth.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import com.example.jwt.auth.dto.LoginRequest;
import com.example.jwt.auth.dto.LoginResponse;
import com.example.jwt.member.entity.Member;

public interface AuthService {
	Map<String, Object> login(LoginRequest request);

	Member findMemberByEmail(String userEmail);

	String findRefreshToken(Long memberNo);

	LocalDateTime findRefreshTokenExpiration(Long memberNo);

	void logout(Long memberNo);

	int updatePasswordByMemberNo(Long memberNo, String string);
}
