package com.alterronix.alteronixback.controller;

import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.exception.InvalidResourceException;
import com.alterronix.alteronixback.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("me")
    public ResponseEntity<User> getMyAccount(@CookieValue(name = "access_token", required = false) String accessToken){
        if (accessToken != null) {
            User userResponseDto = userService.getMyAccount(accessToken);
            return ResponseEntity.ok(userResponseDto);
        }
        throw new InvalidResourceException("Invalid token");
    }
}
