package com.alterronix.alteronixback.service.ai;

import com.alterronix.alteronixback.dto.OpenAIRequest;
import com.alterronix.alteronixback.dto.OpenAIResponse;
import com.alterronix.alteronixback.entity.AIReply;
import com.alterronix.alteronixback.entity.Email;
import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.enums.StatusEmail;
import com.alterronix.alteronixback.exception.ResourceNotFoundException;
import com.alterronix.alteronixback.model.GmailMessage;
import com.alterronix.alteronixback.repository.AIReplyRepository;
import com.alterronix.alteronixback.repository.EmailRepository;
import com.alterronix.alteronixback.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIService {
    private final AIReplyRepository aiReplyRepository;
    private final WebClient webClient;
    private final EmailRepository emailRepository;
    private final String apiKey;

    public AIService(AIReplyRepository aiReplyRepository, @Qualifier("openAiWebClient") WebClient webClient, EmailRepository emailRepository, @Value("${open.api.key}") String apiKey) {
        this.aiReplyRepository = aiReplyRepository;
        this.webClient = webClient;
        this.emailRepository = emailRepository;
        this.apiKey = apiKey;
    }

    public AIReply generateReply(User user, GmailMessage gmail) {
        Email email = this.emailRepository.findByGmailId(gmail.getId()).orElse(new Email());
        String cleanSnippet = gmail.getSnippet()
                .replaceAll("[\\r\\n]+", " ")
                .replace("\"", "'")
                .trim();

        String prompt = "You are the user. Write a concise reply:\n\n" + cleanSnippet;

        OpenAIRequest request = new OpenAIRequest(
                "gpt-5.4-mini",
                List.of(new OpenAIRequest.Message("user", prompt))
        );

        String aiResponse = webClient.post()
                .uri("/v1/chat/completions")
                .headers(h -> h.setBearerAuth(apiKey))
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.isError(), res ->
                        res.bodyToMono(String.class)
                                .map(body -> new RuntimeException("ERROR: " + body))
                )
                .bodyToMono(String.class)
                .block();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(aiResponse);
        log.info("Got AI response {}", root.get("choices"));
        String body = extractBody(root.get("choices"));

        email.setGmailId(gmail.getId());
        email.setFromEmail(gmail.getFromEmail());
        email.setToEmail(gmail.getFromEmail());
        email.setSubject(gmail.getSubject());
        email.setBody(gmail.getSnippet());
        email.setUser(user);
        Email savedEmail = emailRepository.save(email);

        AIReply aiReply = new AIReply();
        aiReply.setUser(user);
        aiReply.setEmail(savedEmail);
        aiReply.setGeneratedReply(body); // simplifié (à parser proprement plus tard)
        aiReply.setStatus(StatusEmail.GENERATED);
        aiReply.setCreatedAt(Instant.now());

        return aiReplyRepository.save(aiReply);
    }

    public AIReply getAIReplyByGmailId(String gmailId) {
        Email email = this.emailRepository.findByGmailId(gmailId).orElse(null);
        if (email == null) {
            throw new ResourceNotFoundException("Gmail id " + gmailId + " not found");
        }
        return aiReplyRepository.findByEmailOrderByCreatedAtDesc(email).get(0);
    }

    private String extractBody(JsonNode payload) {
        log.info("Extracting AI response body {}", payload.get(0));
        // cas simple
        if (payload.get(0).has("message") && payload.get(0).get("message").has("content")) {
            return payload.get(0).get("message").get("content").asText();
        }
        return "";
    }

    public List<AIReply> getAIRepliesByEmail(Email email) {
        return aiReplyRepository.findByEmailOrderByCreatedAtDesc(email);
    }
}
