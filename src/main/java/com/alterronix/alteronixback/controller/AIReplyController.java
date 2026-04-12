package com.alterronix.alteronixback.controller;

import com.alterronix.alteronixback.entity.AIReply;
import com.alterronix.alteronixback.entity.Email;
import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.exception.InvalidResourceException;
import com.alterronix.alteronixback.model.GmailMessage;
import com.alterronix.alteronixback.service.ai.AIService;
import com.alterronix.alteronixback.service.email.EmailService;
import com.alterronix.alteronixback.service.email.GmailService;
import com.alterronix.alteronixback.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@AllArgsConstructor
public class AIReplyController {
    private final AIService aiService;
    private final EmailService emailService;
    private final GmailService gmailService;
    private final UserService userService;

    private User getCurrentUser(String accessToken) {
        if (accessToken != null) {
            return userService.getMyAccount(accessToken);
        }
        throw new InvalidResourceException("Invalid token");
    }

    @PostMapping("/generate/{emailId}")
    public ResponseEntity<AIReply> generateReply(@PathVariable String emailId, @CookieValue(name = "access_token", required = false) String accessToken) {
        User user = getCurrentUser(accessToken);
        GmailMessage gmail = gmailService.getMessageDetail(user, emailId);

        AIReply aiReply = aiService.generateReply(getCurrentUser(accessToken), gmail);
        return ResponseEntity.ok(aiReply);
    }

    @GetMapping("/{emailId}")
    public ResponseEntity<AIReply> getReply(@PathVariable String emailId, @CookieValue(name = "access_token", required = false) String accessToken) {
        AIReply aiReply = aiService.getAIReplyByGmailId(emailId);
        return ResponseEntity.ok(aiReply);
    }
}
