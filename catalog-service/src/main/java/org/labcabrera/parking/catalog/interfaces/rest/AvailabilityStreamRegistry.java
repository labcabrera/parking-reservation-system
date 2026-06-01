package org.labcabrera.parking.catalog.interfaces.rest;

import org.labcabrera.parking.catalog.domain.port.outbound.AvailabilityBroadcastPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AvailabilityStreamRegistry implements AvailabilityBroadcastPort {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityStreamRegistry.class);

    private final Map<String, List<SseEmitter>> emittersByFacility = new ConcurrentHashMap<>();

    public SseEmitter register(String facilityId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout — rely on heartbeat
        List<SseEmitter> list = emittersByFacility.computeIfAbsent(facilityId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        Runnable cleanup = () -> {
            List<SseEmitter> emitters = emittersByFacility.get(facilityId);
            if (emitters != null) emitters.remove(emitter);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    public void send(String facilityId, Object payload) {
        List<SseEmitter> emitters = emittersByFacility.getOrDefault(facilityId, List.of());
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("availability-update")
                        .data(payload));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    @Scheduled(fixedDelay = 30_000)
    public void sendHeartbeats() {
        emittersByFacility.forEach((facilityId, emitters) -> {
            List<SseEmitter> dead = new CopyOnWriteArrayList<>();
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException e) {
                    dead.add(emitter);
                }
            }
            emitters.removeAll(dead);
        });
    }
}
