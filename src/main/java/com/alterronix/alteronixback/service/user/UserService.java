package com.alterronix.alteronixback.service.user;

import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.enums.StatusCode;
import com.alterronix.alteronixback.exception.InvalidResourceException;
import com.alterronix.alteronixback.exception.ResourceNotFoundException;
import com.alterronix.alteronixback.repository.UserRepository;
import com.alterronix.alteronixback.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public User getMyAccount(String token) {
        if(!this.jwtUtil.validateToken(token)) {
            throw new InvalidResourceException("Invalid token", StatusCode.INVALID_TOKEN.name());
        }
        String email = this.jwtUtil.extractEmail(token);
        return getUserByEmail(email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
