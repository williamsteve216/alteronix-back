package com.alterronix.alteronixback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "emails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "gmail_id", unique = true)
    private String gmailId;
    private String threadId;
    private String fromEmail;
    private String toEmail;
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String body;
    private String snippet;
    private Instant receivedAt;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
