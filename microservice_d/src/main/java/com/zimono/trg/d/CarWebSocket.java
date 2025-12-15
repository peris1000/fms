package com.zimono.trg.d;

import com.zimono.trg.shared.Heartbeat;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/car-heartbeats")
@ApplicationScoped
public class CarWebSocket {

    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();
    @Inject
    ObjectMapper mapper;

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    public void broadcast(Heartbeat heartbeat) {
        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(mapper.writeValueAsString(heartbeat));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
