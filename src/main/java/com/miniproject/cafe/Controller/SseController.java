package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Emitter.SseEmitterStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterStore emitterStore;

    // 관리자 전용 SSE 구독
    @GetMapping("/sse/admin/{storeName}")
    public SseEmitter subscribeAdmin(@PathVariable String storeName) {
        SseEmitter emitter = new SseEmitter(1000L * 60 * 30);
        emitterStore.addAdminEmitter(storeName, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("admin-connected"));
        } catch (Exception ignored) {}

        return emitter;
    }

    // 사용자 전용 SSE 구독
    @GetMapping("/sse/user/{userId}")
    public SseEmitter subscribeUser(@PathVariable String userId) {
        SseEmitter emitter = new SseEmitter(1000L * 60 * 30);
        emitterStore.addUserEmitter(userId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("user-connected"));
        } catch (Exception ignored) {}

        return emitter;
    }
}
