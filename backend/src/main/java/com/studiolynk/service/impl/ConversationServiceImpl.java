package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.ConversationDto;
import com.studiolynk.model.dto.MessageDto;
import com.studiolynk.model.entity.Conversation;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.Message;
import com.studiolynk.model.entity.Studio;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.entity.WorkRequirement;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.ConversationRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.MessageRepository;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.repository.WorkRequestRepository;
import com.studiolynk.repository.WorkRequirementRepository;
import com.studiolynk.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final WorkRequirementRepository workRequirementRepository;
    private final WorkRequestRepository workRequestRepository;
    private final StudioRepository studioRepository;
    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ConversationServiceImpl(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            WorkRequirementRepository workRequirementRepository,
            WorkRequestRepository workRequestRepository,
            StudioRepository studioRepository,
            FreelancerRepository freelancerRepository,
            UserRepository userRepository,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.workRequirementRepository = workRequirementRepository;
        this.workRequestRepository = workRequestRepository;
        this.studioRepository = studioRepository;
        this.freelancerRepository = freelancerRepository;
        this.userRepository = userRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    @Transactional
    public ConversationDto getOrCreateConversation(String userEmail, Long requirementId, Long freelancerId) {
        User caller = getUserByEmail(userEmail);
        WorkRequirement requirement = workRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Work requirement not found with id: " + requirementId));

        Studio studio = requirement.getStudio();
        Freelancer freelancer;

        if (caller.getRole() == UserRole.STUDIO) {
            Studio callerStudio = studioRepository.findByUserId(caller.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Studio profile not found for user"));
            if (!requirement.getStudio().getId().equals(callerStudio.getId())) {
                throw new AccessDeniedException("You do not own the requirement for this conversation.");
            }
            if (freelancerId == null) {
                throw new BadRequestException("Freelancer ID is required to initiate a conversation.");
            }
            freelancer = freelancerRepository.findById(freelancerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + freelancerId));
        } else if (caller.getRole() == UserRole.FREELANCER) {
            freelancer = freelancerRepository.findByUserId(caller.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user"));

            // MSG-001 & MSG-005: Verify a request was sent to this freelancer before opening chat
            boolean hasRequest = workRequestRepository.existsByRequirementIdAndFreelancerId(requirementId, freelancer.getId());
            if (!hasRequest) {
                throw new AccessDeniedException("You have not received a work request for this requirement.");
            }
        } else if (caller.getRole() == UserRole.ADMIN) {
            if (freelancerId == null) {
                throw new BadRequestException("Freelancer ID is required for admin initiation.");
            }
            freelancer = freelancerRepository.findById(freelancerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + freelancerId));
        } else {
            throw new AccessDeniedException("Unsupported role for conversations: " + caller.getRole());
        }

        Optional<Conversation> existing = conversationRepository.findByRequirementIdAndFreelancerId(requirementId, freelancer.getId());
        if (existing.isPresent()) {
            return mapToConversationDto(existing.get(), caller);
        }

        Conversation newConv = new Conversation(requirement, studio, freelancer);
        Conversation saved = conversationRepository.save(newConv);
        log.info("Created new conversation [{}] for requirement [{}] between studio [{}] and freelancer [{}]",
                saved.getId(), requirement.getId(), studio.getStudioName(), freelancer.getFullName());

        return mapToConversationDto(saved, caller);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getUserConversations(String userEmail) {
        User caller = getUserByEmail(userEmail);
        List<Conversation> conversations;

        if (caller.getRole() == UserRole.STUDIO) {
            Studio studio = studioRepository.findByUserId(caller.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Studio profile not found for user"));
            conversations = conversationRepository.findByStudioIdOrderByCreatedAtDesc(studio.getId());
        } else if (caller.getRole() == UserRole.FREELANCER) {
            Freelancer freelancer = freelancerRepository.findByUserId(caller.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user"));
            conversations = conversationRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancer.getId());
        } else {
            conversations = conversationRepository.findAll();
        }

        List<ConversationDto> results = new ArrayList<>();
        for (Conversation conv : conversations) {
            results.add(mapToConversationDto(conv, caller));
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversationById(String userEmail, Long conversationId) {
        User caller = getUserByEmail(userEmail);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        checkParticipant(conversation, caller);
        return mapToConversationDto(conversation, caller);
    }

    @Override
    @Transactional
    public List<MessageDto> getMessages(String userEmail, Long conversationId) {
        User caller = getUserByEmail(userEmail);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        checkParticipant(conversation, caller);

        // Mark incoming messages as read (MSG-004)
        messageRepository.markMessagesAsRead(conversationId, caller.getId());

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
        List<MessageDto> dtos = new ArrayList<>();
        for (Message msg : messages) {
            dtos.add(mapToMessageDto(msg));
        }
        return dtos;
    }

    @Override
    @Transactional
    public MessageDto sendMessage(String userEmail, Long conversationId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be blank.");
        }
        if (content.length() > 4000) {
            throw new BadRequestException("Message content exceeds maximum allowed length (4000 characters).");
        }

        User caller = getUserByEmail(userEmail);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        checkParticipant(conversation, caller);

        Message message = new Message(conversation, caller, content.trim());
        Message saved = messageRepository.save(message);

        MessageDto dto = mapToMessageDto(saved);

        // Broadcast to WebSocket subscribers for this conversation (MSG-002)
        try {
            simpMessagingTemplate.convertAndSend("/topic/conversation." + conversationId, dto);
        } catch (Exception e) {
            log.warn("Failed to broadcast message [{}] over WebSocket: {}", saved.getId(), e.getMessage());
        }

        log.info("User [{}] sent message [{}] in conversation [{}]", caller.getEmail(), saved.getId(), conversationId);
        return dto;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String userEmail, Long conversationId) {
        User caller = getUserByEmail(userEmail);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        checkParticipant(conversation, caller);
        int updated = messageRepository.markMessagesAsRead(conversationId, caller.getId());
        if (updated > 0) {
            try {
                simpMessagingTemplate.convertAndSend(
                        "/topic/conversation." + conversationId + ".read",
                        Map.of("conversationId", conversationId, "readByUserId", caller.getId())
                );
            } catch (Exception e) {
                log.warn("Failed to broadcast read status over WebSocket: {}", e.getMessage());
            }
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private void checkParticipant(Conversation conversation, User user) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.STUDIO) {
            if (conversation.getStudio().getUser() != null &&
                    conversation.getStudio().getUser().getId().equals(user.getId())) {
                return;
            }
        }

        if (user.getRole() == UserRole.FREELANCER) {
            if (conversation.getFreelancer().getUser() != null &&
                    conversation.getFreelancer().getUser().getId().equals(user.getId())) {
                return;
            }
        }

        throw new AccessDeniedException("You are not a participant in this conversation.");
    }

    private ConversationDto mapToConversationDto(Conversation conversation, User caller) {
        ConversationDto dto = new ConversationDto();
        dto.setId(conversation.getId());
        dto.setCreatedAt(conversation.getCreatedAt());

        WorkRequirement req = conversation.getRequirement();
        if (req != null) {
            dto.setRequirementId(req.getId());
            dto.setEventName(req.getEventName());
            dto.setEventType(req.getEventType());
            dto.setEventDate(req.getEventDate());
            dto.setRequirementStatus(req.getStatus());
        }

        Studio studio = conversation.getStudio();
        if (studio != null) {
            dto.setStudioId(studio.getId());
            dto.setStudioName(studio.getStudioName());
            dto.setStudioLogoUrl(studio.getLogoUrl());
        }

        Freelancer fl = conversation.getFreelancer();
        if (fl != null) {
            dto.setFreelancerId(fl.getId());
            dto.setFreelancerName(fl.getFullName());
            dto.setFreelancerPhotoUrl(fl.getProfilePhotoUrl());
        }

        // Fetch last message
        Optional<Message> lastMsg = messageRepository.findTopByConversationIdOrderBySentAtDesc(conversation.getId());
        if (lastMsg.isPresent()) {
            dto.setLastMessage(lastMsg.get().getContent());
            dto.setLastMessageAt(lastMsg.get().getSentAt());
            dto.setLastMessageSenderId(lastMsg.get().getSender().getId());
        }

        // Count unread messages for caller
        long unread = messageRepository.countByConversationIdAndSenderIdNotAndIsReadFalse(conversation.getId(), caller.getId());
        dto.setUnreadCount(unread);

        return dto;
    }

    private MessageDto mapToMessageDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setContent(message.getContent());
        dto.setRead(message.isRead());
        dto.setSentAt(message.getSentAt());

        User sender = message.getSender();
        if (sender != null) {
            dto.setSenderId(sender.getId());
            dto.setSenderEmail(sender.getEmail());
            dto.setSenderRole(sender.getRole());

            if (sender.getRole() == UserRole.STUDIO) {
                studioRepository.findByUserId(sender.getId())
                        .ifPresent(st -> dto.setSenderName(st.getStudioName()));
            } else if (sender.getRole() == UserRole.FREELANCER) {
                freelancerRepository.findByUserId(sender.getId())
                        .ifPresent(fl -> dto.setSenderName(fl.getFullName()));
            } else {
                dto.setSenderName(sender.getEmail());
            }
        }

        return dto;
    }
}
