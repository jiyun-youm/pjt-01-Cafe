package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Emitter.SseEmitterStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterStore emitterStore;

    // 관리자 전용 SSE 구독
    @GetMapping(value = "/sse/admin/{storeName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeAdmin(@PathVariable String storeName) {

        // 1. URL 인코딩된 한글 매장명을 원래대로 복구 (예: %EC%B9%B4 -> 카)
        String decodedStoreName = URLDecoder.decode(storeName, StandardCharsets.UTF_8);

        // 2. 타임아웃 설정 (30분)
        SseEmitter emitter = new SseEmitter(1000L * 60 * 30);

        // 3. 저장소에 "디코딩된 이름"으로 저장
        emitterStore.addAdminEmitter(decodedStoreName, emitter);

        // 4. 연결 즉시 더미 데이터 전송 (503 에러 방지용)
        try {
            emitter.send(SseEmitter.event().name("connect").data("admin-connected"));
        } catch (Exception ignored) {}

        return emitter;
    }

    // 사용자 전용 SSE 구독
    @GetMapping(value = "/sse/user/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeUser(@PathVariable String userId) {

        SseEmitter emitter = new SseEmitter(1000L * 60 * 30);
        emitterStore.addUserEmitter(userId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("user-connected"));
        } catch (Exception ignored) {}

        return emitter;
    }
}