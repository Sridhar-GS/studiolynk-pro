package com.studiolynk.controller;

import com.studiolynk.model.dto.WsChatMessage;
import com.studiolynk.service.ConversationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket STOMP Message Handler (Phase 11: MSG-002, MSG-003, MSG-005).
 * Listens for client messages sent to /app/chat.sendMessage.
 */
@Controller
public class WsChatController {

    private static final Logger log = LoggerFactory.getLogger(WsChatController.class);

    private final ConversationService conversationService;

    public WsChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @MessageMapping("/chat.sendMessage")
    public void handleWsMessage(@Valid @Payload WsChatMessage message, Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated WebSocket message received for conversation [{}]", message.getConversationId());
            return;
        }

        String userEmail = principal.getName();
        log.debug("Received WebSocket message from [{}] for conversation [{}]", userEmail, message.getConversationId());
        conversationService.sendMessage(userEmail, message.getConversationId(), message.getContent());
    }
}
