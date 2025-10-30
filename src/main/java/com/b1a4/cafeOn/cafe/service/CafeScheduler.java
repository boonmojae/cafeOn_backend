package com.b1a4.cafeOn.cafe.service;

import com.b1a4.cafeOn.cafe.repository.CafeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CafeScheduler {
    private final CafeRepository cafeRepository;

//    매일 새벽 3시 실행
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateWeeklyViews() {
        cafeRepository.updateViewsLast7d();
        log.info("✅ [Scheduler] views_last7d updated successfully");
    }

//    @PostConstruct
    public void testNow() {
        updateWeeklyViews();    // 서버를 재시작할 때마다 updateWeeklyViews()가 즉시 한 번 실행됨
    }
}