package com.example.jwt.common.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.jwt.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {
	
	private final RefreshTokenRepository repository;

    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    public void cleanupExpiredRefreshTokens() {
    	try {
            int deletedCount = repository.deleteAllByExpirationDateBefore(LocalDateTime.now());
            log.info("만료된 Refresh Token {}개 삭제 완료", deletedCount);
        } catch (Exception e) {
            log.error("만료 토큰 정리 중 예외 발생", e);
        }
    }
}
