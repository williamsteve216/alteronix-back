package com.alterronix.alteronixback.service.email;

import com.alterronix.alteronixback.entity.Email;
import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.exception.ResourceNotFoundException;
import com.alterronix.alteronixback.repository.EmailRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;

    public List<Email> getUserEmails(User user) {
        return emailRepository.findByUser(user);
    }

    public Email getEmailById(Long id) {
        return emailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));
    }

    public Email saveEmail(Email email) {
        return emailRepository.save(email);
    }

    public Email getEmailByGmailId(String gmailId) {
        return emailRepository.findByGmailId(gmailId).orElseThrow(() -> new ResourceNotFoundException("Email not found"));
    }
}
