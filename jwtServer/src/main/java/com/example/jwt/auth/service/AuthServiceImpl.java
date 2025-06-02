package com.example.jwt.auth.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.jwt.auth.dto.LoginRequest;
import com.example.jwt.auth.dto.LoginResponse;
import com.example.jwt.auth.entity.RefreshToken;
import com.example.jwt.auth.repository.RefreshTokenRepository;
import com.example.jwt.member.entity.Member;
import com.example.jwt.member.repository.MemberRepository;
import com.example.jwt.util.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final MemberRepository memberRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final BCryptPasswordEncoder bcrypt;
	private final JwtUtil jwtUtil;

	@Override
	public Map<String, Object> login(LoginRequest request) {

		Member member = memberRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

		if (!bcrypt.matches(request.getPassword(), member.getPassword())) {
			throw new RuntimeException("비밀번호가 일치하지 않습니다.");
		}

		String accessToken = jwtUtil.generateAccessToken(member.getMemberNo(), member.getEmail());
		String refreshToken = jwtUtil.generateRefreshToken(member.getMemberNo(), member.getEmail());

		Date expirationDate = jwtUtil.getExpirationDate(refreshToken);
		LocalDateTime localExpirationDate = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		// 1. 영속성 컨텍스트 내 기존 토큰 조회 (읽기용)
		RefreshToken tokenEntity = refreshTokenRepository.findById(member.getMemberNo()).orElse(null);

		if (tokenEntity == null) {
			// 2. 없으면 새로 생성해서 저장 (persist)
			tokenEntity = RefreshToken.builder().memberNo(member.getMemberNo()).token(refreshToken)
					.expirationDate(localExpirationDate).build();

			refreshTokenRepository.save(tokenEntity);
		} else {
			// 3. 있으면 업데이트 (영속 상태이므로 트랜잭션 커밋 시 자동 반영)
			tokenEntity.update(refreshToken, localExpirationDate);
			// save() 호출하지 않아도 됨, 이미 영속 상태라 변경 감지됨
		}

		Map<String, Object> map = new HashMap<>();
		map.put("refreshToken", refreshToken);
		map.put("loginResponse", new LoginResponse(accessToken, member.getName()));

		return map;
	}

	@Override
	public void logout(Long memberNo) {
		refreshTokenRepository.deleteById(memberNo); // REFRESH_TOKEN의 ID는(PK) memberNo
	}
	
	@Override
	public Member findMemberByEmail(String userEmail) {
		Member member = memberRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

		return member;
	}

	@Override
	public String findRefreshToken(Long memberNo) {
		return refreshTokenRepository.findByMember_MemberNo((long) memberNo) // Optional<RefreshToken> 객체를 반환함 (해당 회원의 토큰이 있을 수도, 없을 수도 있음)
				.map(RefreshToken::getToken) // Optional<RefreshToken>을 Optional<String>으로 변환 (토큰 문자열만 추출)
				.orElse(null); // 값이 없을 경우 null 반환
	}
	
	@Override
	public LocalDateTime findRefreshTokenExpiration(Long memberNo) {
	    return refreshTokenRepository
	        .findByMember_MemberNo(memberNo)
	        .map(RefreshToken::getExpirationDate)
	        .orElse(null);
	}

	
	@Override
	public int updatePasswordByMemberNo(Long memberNo, String pw) {
		
		String encPw = bcrypt.encode(pw);
		
		return memberRepository.updatePasswordByMemberNo(memberNo, encPw);
	}
}
