package com.example.jwt.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jwt.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> { // 사용할 Entity, PK(ID) 데이터 타입

	// email 로 회원찾기는 기본 CURD에 없기때문에 직접 작성하기
	Optional<Member> findByEmail(String email); // 이메일로 회원 찾기

}
