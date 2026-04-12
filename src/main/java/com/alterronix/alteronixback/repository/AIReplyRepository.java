package com.alterronix.alteronixback.repository;

import com.alterronix.alteronixback.entity.AIReply;
import com.alterronix.alteronixback.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIReplyRepository extends JpaRepository<AIReply, Long> {
    List<AIReply> findByEmailOrderByCreatedAtDesc(Email email);
}
