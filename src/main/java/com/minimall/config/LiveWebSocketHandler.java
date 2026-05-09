package com.minimall.config;

import com.minimall.dto.LiveCommentResponse;
import com.minimall.service.LiveService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Sinks;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {
    private final LiveService liveService;
    private final ObjectMapper objectMapper;
    private final Map<String, Sinks.Many<LiveCommentResponse>> roomSinks = new ConcurrentHashMap<>();

    public LiveWebSocketHandler(LiveService liveService, ObjectMapper objectMapper) {
        this.liveService = liveService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = extractRoomId(session);
        if (roomId != null) {
            Sinks.Many<LiveCommentResponse> sink = roomSinks.computeIfAbsent(roomId,
                k -> Sinks.many().multicast().onBackpressureBuffer());
            sink.asFlux().subscribe(message -> {
                try {
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                        }
                    }
                } catch (Exception e) {
                    // Ignore send errors
                }
            });
            session.getAttributes().put("roomId", roomId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = (String) session.getAttributes().get("roomId");
        if (roomId != null) {
            CommentMessage commentMessage = objectMapper.readValue(message.getPayload(), CommentMessage.class);
            LiveCommentResponse response = liveService.addComment(
                roomId,
                commentMessage.userId(),
                commentMessage.nickname(),
                commentMessage.avatar(),
                commentMessage.content()
            );

            Sinks.Many<LiveCommentResponse> sink = roomSinks.get(roomId);
            if (sink != null) {
                sink.tryEmitNext(response);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Cleanup if needed
    }

    private String extractRoomId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals("lives") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null;
    }

    public record CommentMessage(String userId, String nickname, String avatar, String content) {}
}