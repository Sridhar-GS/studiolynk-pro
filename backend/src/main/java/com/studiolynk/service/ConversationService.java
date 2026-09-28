package com.studiolynk.service;

import com.studiolynk.model.dto.ConversationDto;
import com.studiolynk.model.dto.MessageDto;

import java.util.List;

public interface ConversationService {

    ConversationDto getOrCreateConversation(String userEmail, Long requirementId, Long freelancerId);

    List<ConversationDto> getUserConversations(String userEmail);

    ConversationDto getConversationById(String userEmail, Long conversationId);

    List<MessageDto> getMessages(String userEmail, Long conversationId);

    MessageDto sendMessage(String userEmail, Long conversationId, String content);

    void markMessagesAsRead(String userEmail, Long conversationId);
}
