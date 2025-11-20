package com.miniproject.cafe.Emitter;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterStore {

    private final Map<String, List<SseEmitter>> storeEmitters = new ConcurrentHashMap<>();
    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public void addAdminEmitter(String storeName, SseEmitter emitter) {
        storeEmitters
                .computeIfAbsent(storeName, k -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(storeEmitters, storeName, emitter));
        emitter.onTimeout(() -> removeEmitter(storeEmitters, storeName, emitter));
        emitter.onError(e -> removeEmitter(storeEmitters, storeName, emitter));
    }

    public void addUserEmitter(String userId, SseEmitter emitter) {
        userEmitters
                .computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(userEmitters, userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userEmitters, userId, emitter));
        emitter.onError(e -> removeEmitter(userEmitters, userId, emitter));
    }

    private void removeEmitter(Map<String, List<SseEmitter>> map,
                               String key, SseEmitter emitter) {
        List<SseEmitter> list = map.get(key);
        if (list != null) list.remove(emitter);
    }

    public void sendToStore(String storeName, String eventName, Object data) {
        send(storeEmitters, storeName, eventName, data);
    }

    public void sendToUser(String userId, String eventName, Object data) {
        send(userEmitters, userId, eventName, data);
    }

    private void send(Map<String, List<SseEmitter>> map,
                      String key, String eventName, Object data) {

        List<SseEmitter> emitters = map.get(key);
        if (emitters == null) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                emitter.complete();
            }
        }
    }
}