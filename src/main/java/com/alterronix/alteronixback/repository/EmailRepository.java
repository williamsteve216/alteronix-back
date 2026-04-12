package com.alterronix.alteronixback.repository;

import com.alterronix.alteronixback.entity.Email;
import com.alterronix.alteronixback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findByUser(User user);

    Optional<Email> findByGmailId(String gmailId);
}
