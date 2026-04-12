package com.alterronix.alteronixback.entity;

import com.alterronix.alteronixback.enums.StatusEmail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_replies")
public class AIReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String generatedReply;

    @Column(columnDefinition = "TEXT")
    private String editedReply;

    @Enumerated(EnumType.STRING)
    private StatusEmail status; // generated, edited, sent

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}
