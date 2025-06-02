# JWT(토큰) / JPA(ORM) 기반 애플리케이션
### React + Spring Boot 조합으로 JWT 기반 기능을 구현하는 건 실무에서도 많이 사용되는 패턴이며, Access Token과 Refresh Token을 함께 사용하는 구조도 안전하고 권장되는 방식임.
[![Notion Page](https://img.shields.io/badge/참고문서-Notion페이지로_가기-blue?logo=notion)]([https://www.notion.so/your-notion-page-id](https://spark-meteor-5e2.notion.site/CSR-Client-Side-Rendering-2036e2b2e5ec80c694b9d0bcec13f07a))

## 주요 기능 흐름

### 1. **로그인**

- React: 로그인 폼에서 이메일/비밀번호 입력 → `/auth/login`으로 요청
- Spring:
    - 사용자 정보 확인
    - Access Token + Refresh Token 생성
    - Access Token → 응답 Body + localStorage 저장
    - Refresh Token → `Set-Cookie` (httpOnly, Secure 옵션으로)

---

### 2. **Access Token 인증 요청 예시**

```
Authorization: Bearer <access_token>
```

Spring에서 `Authorization` 헤더의 토큰을 검증해서 사용자 정보 제공

---

### 3. **Access Token 만료 시**

- React가 요청 → 401 Unauthorized 응답
- React는 서버에 `/auth/refresh` 요청
- 서버는 쿠키의 Refresh Token으로 재발급 처리
    - 유효하면 새 Access Token 생성 및 전달
    - 유효하지 않으면 로그아웃 유도 (새로 로그인하여 토큰 재발급)
