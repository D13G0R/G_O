package com.core.G_O.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class PaymentWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public PaymentWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");

        if (userId == null) {
            return;
        }

        sessionsByUser.computeIfAbsent(userId, key -> new CopyOnWriteArraySet<>()).add(session);
        LOG.info("@PaymentWebSocketHandler > User {} connected to payment realtime channel", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");

        if (userId == null) {
            return;
        }

        Set<WebSocketSession> sessions = sessionsByUser.get(userId);

        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                sessionsByUser.remove(userId);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (IOException ignored) {
            }
        }
    }

    public void sendToUser(Long userId, Object payload) {
        if (userId == null) {
            return;
        }

        Set<WebSocketSession> sessions = sessionsByUser.get(userId);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(payload);

            for (WebSocketSession session : sessions) {
                if (!session.isOpen()) {
                    continue;
                }

                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception exception) {
            LOG.warn("@PaymentWebSocketHandler > Could not send realtime payment event to user {}", userId, exception);
        }
    }
}
