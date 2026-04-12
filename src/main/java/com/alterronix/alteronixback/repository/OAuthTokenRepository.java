package com.alterronix.alteronixback.repository;

import com.alterronix.alteronixback.entity.OAuthToken;
import com.alterronix.alteronixback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByUser(User user);
}
