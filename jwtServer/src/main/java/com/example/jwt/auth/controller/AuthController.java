package com.example.jwt.auth.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jwt.auth.dto.LoginRequest;
import com.example.jwt.auth.dto.LoginResponse;
import com.example.jwt.auth.service.AuthService;
import com.example.jwt.member.entity.Member;
import com.example.jwt.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	
	private final AuthService authService;
	private final JwtUtil jwtUtil;

	/** 로그인
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping("login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {

		// 1. 로그인 처리 및 토큰 생성 (서비스에서 Access + Refresh 생성 및 DB 저장)
		Map<String, Object> map = authService.login(request);
		LoginResponse loginResponse = (LoginResponse) map.get("loginResponse");

		// 2. Refresh Token 쿠키로 전달(보안상 절대 Body에 보내면 안됨(XSS 공격에 취약)
		Cookie cookie = new Cookie ("refreshToken", (String) map.get("refreshToken")); // 나중에 분리해서 관리
		cookie.setHttpOnly(true); //  HttpOnly 쿠키 (JS에서 접근 불가 -> XSS에 안전)
		cookie.setPath("/"); // 전역 경로에서 사용 가능
		cookie.setMaxAge(60 * 60 * 24 * 7); // 7일
		//cookie.setSecure(true); // HTTPS에서만 전송하도록 제한(개발모드에서 false 괜찮음, 배포모드 반드시 true)
		//cookie.setSameSite("Strict"); // 어떤 요청 상황에서 브라우저가 서버에 전송할지를 제한
		// Strict : 자기 사이트에서만 전송됨
		// Lax : 기본값. GET 방식 같은 일부 외부 요청엔 쿠키 전송 가능
		// None	: 모든 요청에 쿠키 전송, 단 Secure=true도 반드시 함께 설정해야 함 (특히 CORS 상황에서 사용)
		response.addCookie(cookie);

		 // 3. Access Token은 본문으로 반환 (localStorage에 저장될 수 있도록)
		return ResponseEntity.ok(loginResponse); // 여기서 accessToken, 사용자 정보 등을 포함
	}
	
	/** 로그아웃
	 * @param authorizationHeader
	 * @param response
	 * @return
	 */
	@DeleteMapping("logout")
	public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader, HttpServletResponse response) {
		
		String accessToken = authorizationHeader.replace("Bearer ", "");
        Long memberNo = jwtUtil.getMemberNo(accessToken);
        
        log.debug("here memberNo : {} ", memberNo);
        
        authService.logout(memberNo);
      
        // refresh 쿠키 제거
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("로그아웃 완료");
		
	}
	

	/**
	 * 리프레시 토큰을 사용한 Access Token 재발급 또는 로그아웃 유도
	 * @param request
	 * @return
	 * 
	 * 주의 : /auth/refresh에서는 절대로 401을 반환하지 않기
	 *        무한루프에 빠질 수 있기 때문에(클라이언트의 axios 응답 interceptor 확인)
	 */
	@PostMapping("/refresh")
	public ResponseEntity<Map<String, Object>> refreshAccessToken(HttpServletRequest request) {
	    
	    Map<String, Object> responseBody = new HashMap<>();

	    // 1. 쿠키에서 refreshToken 추출
	    Cookie[] cookies = request.getCookies();
	    if (cookies == null) {
	        responseBody.put("success", false);
	        responseBody.put("message", "쿠키 없음");
	        return ResponseEntity.ok(responseBody);
	    }

	    String refreshToken = null;
	    for (Cookie cookie : cookies) {
	    	// refreshToken이 쿠키에 있으면 꺼내오기
	        if ("refreshToken".equals(cookie.getName())) {
	            refreshToken = cookie.getValue();
	            break;
	        }
	    }

	    // refreshToken이 없다면
	    if (refreshToken == null) {
	        responseBody.put("success", false);
	        responseBody.put("message", "isEmpty");
	        return ResponseEntity.ok(responseBody);
	    }

	    // 2. 토큰 유효성 검사
	    // 클라이언트가 보낸 토큰 자체를 검사하기 위해 작성
	    if (!jwtUtil.isTokenValid(refreshToken)) {
	        responseBody.put("success", false);
	        responseBody.put("message", "expired");
	        return ResponseEntity.ok(responseBody);
	    }

	    // 3. 사용자 이메일 추출 및 사용자 번호 조회
	    String userEmail = jwtUtil.getEmail(refreshToken);
	    Member member = authService.findMemberByEmail(userEmail);

	    // refreshToken에 해당하는 사용자 정보 없음
	    if (member == null) {
	        responseBody.put("success", false);
	        responseBody.put("message", "nobody");
	        return ResponseEntity.ok(responseBody);
	    }

	    long memberNo = member.getMemberNo();

	    // 4. DB에 저장된 refreshToken과 요청 시 쿠키에 담겨온 refreshToken이 일치하는지 확인
	    String savedToken = authService.findRefreshToken(memberNo);
	    if (!refreshToken.equals(savedToken)) {
	        responseBody.put("success", false);
	        responseBody.put("message", "invalid");  // 저장된 토큰과 일치하지 않음
	        return ResponseEntity.ok(responseBody);
	    }

	    // 5. DB에 저장된 RefreshToken만료 여부 확인
	    // - 클라이언트가 만료된 토큰을 일부러 수정하거나 변조해서 보내는 것을 막기 위해
	    // - 또는 토큰이 탈취되어도 서버에서 수동 만료 처리하거나 기간을 단축할 수 있도록 하기 위해
	    // -> 보안 목적에서 토큰 자체의 만료 + DB 만료시간을 이중 체크
	    LocalDateTime expirationDate = authService.findRefreshTokenExpiration(memberNo);
	    if (expirationDate.isBefore(LocalDateTime.now())) {
	        responseBody.put("success", false);
	        responseBody.put("message", "expired"); // 리프레시 토큰 만료
	        return ResponseEntity.ok(responseBody);
	    }

	    // 6. 위 모든 경우가 아니라면 새로운 Access Token 발급
	    String newAccessToken = jwtUtil.generateAccessToken(memberNo, userEmail);

	    // 7. 성공 응답
	    responseBody.put("success", true);
	    responseBody.put("accessToken", newAccessToken);
	    return ResponseEntity.ok(responseBody);
	}

	
	/** 마이페이지 출력할 내 정보 조회
	 * @return
	 */
	@GetMapping("getMemberInfo")
	public ResponseEntity<?> getMemberInfo(@RequestHeader("Authorization") String authorizationHeader) {
		try {
	        String accessToken = authorizationHeader.replace("Bearer ", "");
	        // 1. 헤더에 담긴 토큰 꺼내오기
	        String email = jwtUtil.getEmail(accessToken);

	        // 2. 토큰 검증
	        if (!jwtUtil.isTokenValid(accessToken)) {
	            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
	        }

	        Member member = authService.findMemberByEmail(email);
	        return ResponseEntity.ok(member);
	        
	    } catch (Exception e) {
	        return ResponseEntity.status(401).body("토큰 검증 실패");
	    }
	}
	
	
	/** 비밀번호 변경
	 * @param map
	 * @return
	 */
	@PutMapping("changePw")
	public ResponseEntity<?> changePw(@RequestHeader("Authorization") String authorizationHeader,
										@RequestBody Map<String, String> map) {
		try {
			
			String accessToken = authorizationHeader.replace("Bearer ", "");
	        // 1. 헤더에 담긴 토큰 꺼내오기
	        Long memberNo = jwtUtil.getMemberNo(accessToken);
	        log.debug("memberNo {}", memberNo);

	        // 2. 토큰 검증
	        if (!jwtUtil.isTokenValid(accessToken)) {
	            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
	        }
			
	        
			int result = authService.updatePasswordByMemberNo(memberNo, map.get("password"));
			
			if(result > 0) {
				return ResponseEntity.status(HttpStatus.OK).body("비밀번호 변경 성공");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 사용자 없음");
				
			}
			
		} catch (Exception e) {
			log.debug("error : {}" ,e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("비밀번호 변경 중 오류 발생");
					
		}
	}
	
	// 헤더에서 accessToken 꺼내오고 검증하는 절차도 따로 분리하여 만들기 권장
	
	
	
	
	

}
