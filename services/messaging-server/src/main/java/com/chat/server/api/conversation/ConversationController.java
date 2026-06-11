package com.chat.server.api.conversation;

import com.chat.server.api.conversation.dto.CreateConversationRequest;
import com.chat.server.api.conversation.dto.CreateConversationResponse;
import com.chat.server.core.conversation.ConversationService;
import com.chat.server.network.session.UserId;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

/**
 * REST controller for conversation-related endpoints.
 * Handles creating conversations and managing members.
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * POST /api/conversations/direct
     * Create or get existing direct conversation between authenticated user and target user.
     *
     * Request body:
     * {
     *   "targetUserId": "550e8400-e29b-41d4-a716-446655440002"
     * }
     *
     * Response:
     * {
     *   "conversationId": "dm_550e8400-e29b-41d4-a716-446655440002_550e8400-e29b-41d4-a716-446655440003"
     * }
     */
    @PostMapping("/direct")
    public ResponseEntity<CreateConversationResponse>
            createDirectConversation(Authentication authentication, @RequestBody CreateConversationRequest request) {

            UserId currentUser = new UserId(authentication.getName());
            UserId targetUser = new UserId(request.getTargetUserId());

            String conversationId = conversationService.getOrCreateDirectConversation(currentUser, targetUser);

            return ResponseEntity.ok(new CreateConversationResponse(conversationId));
    }
}
