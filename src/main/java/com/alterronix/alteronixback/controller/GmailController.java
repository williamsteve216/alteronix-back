package com.alterronix.alteronixback.controller;

import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.model.GmailMessage;
import com.alterronix.alteronixback.repository.UserRepository;
import com.alterronix.alteronixback.service.email.GmailService;
import com.alterronix.alteronixback.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gmail")
@AllArgsConstructor
public class GmailController {

    private final GmailService gmailService;
    private final UserRepository userRepository;
    private final UserService userService;

    private User getUser(String accessToken) {
        return userService.getMyAccount(accessToken);
    }

    @GetMapping("messages")
    public ResponseEntity<List<GmailMessage>> getMessages(@CookieValue(name = "access_token", required = false) String accessToken) {
        User user = getUser(accessToken);
        return ResponseEntity.ok(gmailService.getMessages(user));
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<GmailMessage> getMessage(@PathVariable String id, @CookieValue(name = "access_token", required = false) String accessToken) {
        return ResponseEntity.ok(gmailService.getMessageDetail(getUser(accessToken), id));
    }
}
