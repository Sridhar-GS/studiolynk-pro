package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.ConversationDto;
import com.studiolynk.model.dto.MessageDto;
import com.studiolynk.model.dto.SendMessageRequest;
import com.studiolynk.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Messaging", description = "Endpoints for requirement-specific chat conversations and message history (MSG-001 - MSG-006)")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active conversations for the authenticated user")
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getUserConversations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ConversationDto> list = conversationService.getUserConversations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Conversations retrieved successfully.", list));
    }

    @GetMapping("/requirement/{requirementId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get or initiate a requirement-specific conversation (MSG-001, MSG-005)")
    public ResponseEntity<ApiResponse<ConversationDto>> getOrCreateConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requirementId,
            @RequestParam(required = false) Long freelancerId
    ) {
        ConversationDto conv = conversationService.getOrCreateConversation(
                userDetails.getUsername(), requirementId, freelancerId
        );
        return ResponseEntity.ok(ApiResponse.ok("Conversation ready.", conv));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get conversation details by conversation ID")
    public ResponseEntity<ApiResponse<ConversationDto>> getConversationById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        ConversationDto conv = conversationService.getConversationById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Conversation details retrieved.", conv));
    }

    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get message history for a conversation and mark incoming messages as read")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        List<MessageDto> messages = conversationService.getMessages(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Messages retrieved successfully.", messages));
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a text message in a conversation (MSG-003, MSG-004)")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageDto sent = conversationService.sendMessage(
                userDetails.getUsername(), id, request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message sent successfully.", sent));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all unread messages in conversation as read (MSG-004)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        conversationService.markMessagesAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Messages marked as read.", Map.of("conversationId", id, "read", true)));
    }
}
