package com.jj.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServletServerHttpRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:8080")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null) {
                    if (accessor.getUser() != null && accessor.getUser() instanceof Authentication) {
                        Authentication authentication = (Authentication) accessor.getUser();
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Set authentication in SecurityContextHolder for message: {}", authentication);
                    } else {
                        Object authenticationAttr = accessor.getSessionAttributes() != null ?
                            accessor.getSessionAttributes().get("SPRING_AUTHENTICATION") : null;
                        if (authenticationAttr != null && authenticationAttr instanceof Authentication) {
                            Authentication authentication = (Authentication) authenticationAttr;
                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Set authentication from SPRING_AUTHENTICATION attribute in SecurityContextHolder: {}", authentication);
                        } else {
                            Object securityContext = accessor.getSessionAttributes() != null ?
                                accessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT") : null;
                            if (securityContext != null) {
                                org.springframework.security.core.context.SecurityContext context =
                                    (org.springframework.security.core.context.SecurityContext) securityContext;
                                Authentication authentication = context.getAuthentication();
                                accessor.setUser(authentication);
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.debug("Set authentication from session attributes in SecurityContextHolder: {}", authentication);
                            }
                        }
                    }

                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        log.debug("Processing CONNECT command");

                        Object authenticationAttr = accessor.getSessionAttributes().get("SPRING_AUTHENTICATION");
                        if (authenticationAttr != null && authenticationAttr instanceof Authentication) {
                            Authentication authentication = (Authentication) authenticationAttr;
                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Set authentication from SPRING_AUTHENTICATION attribute in WebSocket session: {}", authentication);
                        } else {
                            Object securityContext = accessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT");
                            if (securityContext != null) {
                                org.springframework.security.core.context.SecurityContext context =
                                    (org.springframework.security.core.context.SecurityContext) securityContext;
                                Authentication authentication = context.getAuthentication();
                                accessor.setUser(authentication);
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.debug("Set authentication from HTTP session in WebSocket session: {}", authentication);
                            } else {
                                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                                if (authentication != null) {
                                    accessor.setUser(authentication);
                                    log.debug("Set authentication from SecurityContextHolder in WebSocket session: {}", authentication);
                                } else {
                                    log.warn("No authentication found for WebSocket connection");
                                }
                            }
                        }
                    } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                        SecurityContextHolder.clearContext();
                        log.debug("Cleared SecurityContextHolder on WebSocket disconnect");
                    }
                }

                return message;
            }

            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                // Don't clear the SecurityContextHolder here as it might be needed for @PreAuthorize checks
                // SecurityContextHolder.clearContext();
                log.debug("Message handling completed, keeping SecurityContext for authorization checks");
            }

            @Override
            public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && accessor.getUser() != null && accessor.getUser() instanceof Authentication) {
                    Authentication authentication = (Authentication) accessor.getUser();
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication in SecurityContextHolder in postSend: {}", authentication);
                }
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && accessor.getUser() != null && accessor.getUser() instanceof Authentication) {
                    Authentication authentication = (Authentication) accessor.getUser();
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication in SecurityContextHolder for outbound message: {}", authentication);
                } else if (accessor != null && accessor.getSessionAttributes() != null) {
                    Object authenticationAttr = accessor.getSessionAttributes().get("SPRING_AUTHENTICATION");
                    if (authenticationAttr != null && authenticationAttr instanceof Authentication) {
                        Authentication authentication = (Authentication) authenticationAttr;
                        accessor.setUser(authentication);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Set authentication from SPRING_AUTHENTICATION attribute in SecurityContextHolder for outbound message: {}", authentication);
                    } else {
                        Object securityContext = accessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT");
                        if (securityContext != null) {
                            org.springframework.security.core.context.SecurityContext context =
                                (org.springframework.security.core.context.SecurityContext) securityContext;
                            Authentication authentication = context.getAuthentication();
                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Set authentication from session attributes in SecurityContextHolder for outbound message: {}", authentication);
                        }
                    }
                }

                return message;
            }

            @Override
            public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && accessor.getUser() != null && accessor.getUser() instanceof Authentication) {
                    Authentication authentication = (Authentication) accessor.getUser();
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication in SecurityContextHolder in outbound postSend: {}", authentication);
                }
            }

            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                log.debug("Outbound message handling completed, keeping SecurityContext for authorization checks");
            }
        });
    }

    private static class HttpSessionHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response, 
                                      WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                HttpSession session = servletRequest.getServletRequest().getSession();
                attributes.put("HTTP_SESSION", session);

                Object securityContextAttr = session.getAttribute("SPRING_SECURITY_CONTEXT");
                if (securityContextAttr != null) {
                    attributes.put("SPRING_SECURITY_CONTEXT", securityContextAttr);

                    org.springframework.security.core.context.SecurityContext context =
                        (org.springframework.security.core.context.SecurityContext) securityContextAttr;
                    SecurityContextHolder.setContext(context);

                    attributes.put("SPRING_AUTHENTICATION", context.getAuthentication());

                    log.debug("Set authentication in SecurityContextHolder during handshake: {}", context.getAuthentication());
                } else {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null) {
                        attributes.put("SPRING_AUTHENTICATION", authentication);
                        log.debug("Stored current authentication in WebSocket attributes: {}", authentication);
                    } else {
                        log.warn("No authentication found during WebSocket handshake");
                    }
                }
            }
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response, 
                                  WebSocketHandler wsHandler, Exception exception) {
            log.debug("Handshake completed, keeping SecurityContext for subsequent operations");
        }
    }
}
