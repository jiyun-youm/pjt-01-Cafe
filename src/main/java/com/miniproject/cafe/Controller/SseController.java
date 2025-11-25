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

    // 관리자 SSE 연결
    @GetMapping(value = "/sse/admin/{storeName:.+}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeAdmin(@PathVariable String storeName) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterStore.addAdminEmitter(storeName, emitter);

        try {
            String dummyData = "admin-connected" + " ".repeat(1000);
            emitter.send(SseEmitter.event().name("connect").data(dummyData));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    // 사용자 SSE 연결
    @GetMapping(value = "/sse/user/{userId:.+}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeUser(@PathVariable String userId) {
        SseEmitter emitter = new SseEmitter(3600_000L);
        emitterStore.addUserEmitter(userId, emitter);

        try {
            String dummyData = "user-connect" + " ".repeat(1000); // 공백 1000개 추가
            emitter.send(SseEmitter.event().name("connect").data(dummyData));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}