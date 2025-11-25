package com.miniproject.cafe.Scheduler;

import com.miniproject.cafe.Emitter.SseEmitterStore;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final SseEmitterStore emitterStore;

    // 15초마다 빈 데이터를 보내서 연결이 살아있음을 알림 (공유기 절전 모드 방지)
    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        try {
            emitterStore.sendToAllUsers("ping", "alive");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}