package com.example.jwt.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jwt.auth.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>  { // 사용할 Entity, PK(ID) 데이터 타입
	// 기본 CRUD 기능 포함되어 있음
	// JpaRepository<RefreshToken, Long>를 상속했으면 
	// 기본적인 CRUD 메서드들은 모두 자동으로 제공되기 때문에, 
	// {} 안에 아무 메서드도 작성하지 않아도 됨
	
	// Spring Data JPA의 메서드 이름 기반 쿼리 생성 기능을 활용해서 개발자가 직접 선언하는 커스텀 메서드
	// 메서드 이름을 해석해서 자동으로 쿼리를 만들어줌
	/*
	 * Spring Data JPA가 자동 인식하는 규칙 패턴 : 
	 * findByExpirationDateBefore(LocalDateTime time)
	 * deleteByStatus(String status)
	 * countByUserEmail(String email)
	 * 
	 * 즉, By 뒤에 필드명을 붙이고, 
	 * 조건으로 Before, After, Like, Containing, IsNull 등을 조합
	 * 
	 */
	Optional<RefreshToken> findByMember_MemberNo(Long memberNo);

	// expirationDate가 특정 시각 이전인 토큰들을 삭제
	int deleteAllByExpirationDateBefore(LocalDateTime now);

}
