package com.example.jwt.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.jwt.member.entity.Member;

import jakarta.transaction.Transactional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> { // 사용할 Entity, PK(ID) 데이터 타입

	// email 로 회원찾기는 기본 CURD에 없기때문에 직접 작성하기
	Optional<Member> findByEmail(String email); // 이메일로 회원 찾기

	// 비밀번호 변경하기
	@Modifying // 수정시 사용하는 쿼리라는 뜻. DML 작업일 때 반드시 붙여야 함.
    @Query("UPDATE Member m SET m.password = :newPassword WHERE m.memberNo = :memberNo") //실제 실행할 JPQL 쿼리를 작성
    // 여기서 Member는 엔티티 이름(DB 테이블이 아님).
	int updatePasswordByMemberNo(@Param("memberNo") Long memberNo, // 파라미터 바인딩
    							@Param("newPassword") String newPassword);
	//파라미터 바인딩 : @Query 안에서 사용된 :로 시작하는 파라미터 이름(:memberNo, :newPassword)에 실제 값을 연결해주는 역할
}
