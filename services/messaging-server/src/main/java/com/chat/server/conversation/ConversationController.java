package com.chat.server.conversation;

import com.chat.server.conversation.dto.CreateConversationRequest;
import com.chat.server.conversation.dto.CreateConversationResponse;
import org.springframework.security.core.Authentication;
import com.chat.server.service.ConversationService;
import com.chat.server.session.UserId;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService = ConversationService.getInstance();

    @PostMapping("/direct")
    public ResponseEntity<CreateConversationResponse>
            createDirectConversation(Authentication authentication, @RequestBody CreateConversationRequest request) {

            UserId currentUser = new UserId(authentication.getName());
            UserId targetUser = new UserId(request.getTargetUserId());

            String conversationId = conversationService.getOrCreateDirectConversation(currentUser, targetUser);

            return ResponseEntity.ok(new CreateConversationResponse(conversationId));
    }
}