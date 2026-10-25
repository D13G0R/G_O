package com.core.G_O.websocket;

import com.core.G_O.repositories.UserRepository;
import com.core.G_O.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtHandshakeInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");

        if (token == null || token.isBlank()) {
            LOG.warn("@JwtHandshakeInterceptor > WebSocket connection rejected: missing token");
            return false;
        }

        try {
            String username = jwtService.extractUsername(token);

            if (username == null || !jwtService.isTokenValid(token, username)) {
                return false;
            }

            boolean enabled = userRepository.findByEmail(username)
                    .map(user -> user.isEnabled())
                    .orElse(false);

            if (!enabled) {
                LOG.warn("@JwtHandshakeInterceptor > WebSocket connection rejected: usuario inactivo o inexistente");
                return false;
            }

            attributes.put("userId", jwtService.getUserId(token));
            attributes.put("businessId", jwtService.getBusinessId(token));
            attributes.put("technicianId", jwtService.getTechnicianId(token));
            attributes.put("role", jwtService.getRole(token));

            return true;
        } catch (Exception exception) {
            LOG.warn("@JwtHandshakeInterceptor > WebSocket connection rejected: {}", exception.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
