package com.example.jwt.auth.entity;

import java.time.LocalDateTime;

import com.example.jwt.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

	@Id
	@Column(name = "MEMBER_NO")
	private Long memberNo;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MEMBER_NO", insertable = false, updatable = false)
	private Member member;

	@Column(name = "REFRESH_TOKEN", nullable = false, length = 500)
	private String token;

	@Column(name = "EXPIRATION_DATE", nullable = false)
	private LocalDateTime expirationDate;

	// 수정 메서드
	public void update(String token, LocalDateTime expirationDate) {
		this.token = token;
		this.expirationDate = expirationDate;
	}
}
