package com.alterronix.alteronixback.controller;

import com.alterronix.alteronixback.entity.Email;
import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.exception.InvalidResourceException;
import com.alterronix.alteronixback.service.email.EmailService;
import com.alterronix.alteronixback.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/emails")
public class EmailController {
    private final EmailService emailService;
    private final UserService userService;

    private User getCurrentUser(String accessToken) {
        if (accessToken != null) {
            return userService.getMyAccount(accessToken);
        }
        throw new InvalidResourceException("Invalid token");
    }

    @GetMapping
    public List<Email> getEmails(@CookieValue(name = "access_token", required = false) String accessToken) {
        return emailService.getUserEmails(getCurrentUser(accessToken));
    }

    @GetMapping("/{id}")
    public Email getEmail(@PathVariable Long id) {
        return emailService.getEmailById(id);
    }
}
