package com.alterronix.alteronixback.service.email;

import com.alterronix.alteronixback.entity.AIReply;
import com.alterronix.alteronixback.entity.Email;
import com.alterronix.alteronixback.entity.OAuthToken;
import com.alterronix.alteronixback.entity.User;
import com.alterronix.alteronixback.exception.ResourceNotFoundException;
import com.alterronix.alteronixback.model.GmailMessage;
import com.alterronix.alteronixback.repository.OAuthTokenRepository;
import com.alterronix.alteronixback.service.ai.AIService;
import com.alterronix.alteronixback.service.oauth2.GoogleOAuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class GmailService {

    private final WebClient webClient;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final GoogleOAuthService oauthService;
    private final EmailService emailService;
    private final AIService aiService;

    public GmailService(OAuthTokenRepository oAuthTokenRepository,
                        GoogleOAuthService oauthService,
                        @Qualifier("gmailWebClient") WebClient webClient, EmailService emailService, AIService aiService) {
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.webClient = webClient;
        this.oauthService = oauthService;
        this.emailService = emailService;
        this.aiService = aiService;
    }
    public List<GmailMessage> getMessages(User user) {

        OAuthToken token = oAuthTokenRepository.findByUser(user)
                .orElseThrow();

        String accessToken = oauthService.refreshAccessToken(token);

        String response =  webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/gmail/v1/users/me/messages")
                        .queryParam("maxResults", 10)
                        .build())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // parse JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            List<GmailMessage> messages = new ArrayList<>();

            for (JsonNode msg : root.get("messages")) {

                String id = msg.get("id").asText();
                String threadId = msg.get("threadId").asText();

                // 🔥 appel metadata
                GmailMessage full = fetchMetadata(accessToken, id, threadId);

                messages.add(full);
            }

            return messages;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing Gmail response", e);
        }
    }

    public GmailMessage getMessageDetail(User user, String messageId) {

        OAuthToken token = oAuthTokenRepository.findByUser(user)
                .orElseThrow();

        String accessToken = oauthService.refreshAccessToken(token);

        String response = webClient.get()
                .uri("/gmail/v1/users/me/messages/" + messageId)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            String id = root.get("id").asText();
            String threadId = root.get("threadId").asText();
            String snippet = root.get("snippet").asText();

            JsonNode headers = root.get("payload").get("headers");

            String subject = "";
            String from = "";
            String date = "";

            for (JsonNode header : headers) {

                String name = header.get("name").asText();

                if (name.equalsIgnoreCase("Subject")) subject = header.get("value").asText();
                if (name.equalsIgnoreCase("From")) from = header.get("value").asText();
                if (name.equalsIgnoreCase("Date")) date = header.get("value").asText();
            }

            String fromName = "";
            String fromEmail = "";

            if (from.contains("<")) {
                fromName = from.substring(0, from.indexOf("<")).trim();
                fromEmail = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
            } else {
                fromEmail = from;
            }

            String body = extractBody(root.get("payload"));

            boolean isRead = !root.get("labelIds").toString().contains("UNREAD");
            boolean isPriority = root.get("labelIds").toString().contains("IMPORTANT");
            GmailMessage gmailMessage = new GmailMessage();
            gmailMessage.setId(id);
            gmailMessage.setThreadId(threadId);
            gmailMessage.setSubject(subject);
            gmailMessage.setFromName(fromName);
            gmailMessage.setFromEmail(fromEmail);
            gmailMessage.setSnippet(body);
            gmailMessage.setPriority(isPriority);
            gmailMessage.setDate(date);
            gmailMessage.setRead(isRead);
            List<AIReply> aiReplies = getAIReplies(id);
            gmailMessage.setReply(aiReplies.isEmpty());
            gmailMessage.setReplyList(aiReplies);
            return gmailMessage;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing full message", e);
        }
    }

    public void sendEmail(User user, String to, String subject, String body) {

        OAuthToken token = oAuthTokenRepository.findByUser(user)
                .orElseThrow();

        String accessToken = oauthService.refreshAccessToken(token);

        String rawMessage = "To: " + to + "\r\n" +
                "Subject: " + subject + "\r\n\r\n" +
                body;

        String encoded = Base64.getUrlEncoder()
                .encodeToString(rawMessage.getBytes());

        webClient.post()
                .uri("/gmail/v1/users/me/messages/send")
                .headers(h -> h.setBearerAuth(accessToken))
                .bodyValue("{\"raw\":\"" + encoded + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private GmailMessage fetchMetadata(String accessToken, String messageId, String threadId) {

        String response = webClient.get()
                .uri("/gmail/v1/users/me/messages/" + messageId + "?format=metadata")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            String snippet = root.has("snippet") ? root.get("snippet").asText() : "";

            JsonNode headers = root.get("payload").get("headers");

            String subject = "";
            String from = "";
            String date = "";

            for (JsonNode header : headers) {

                String name = header.get("name").asText();

                if (name.equalsIgnoreCase("Subject")) {
                    subject = header.get("value").asText();
                }

                if (name.equalsIgnoreCase("From")) {
                    from = header.get("value").asText();
                }

                if (name.equalsIgnoreCase("Date")) {
                    date = header.get("value").asText();
                }
            }

            // 🔥 parsing From
            String fromName = "";
            String fromEmail = "";

            if (from.contains("<")) {
                fromName = from.substring(0, from.indexOf("<")).trim();
                fromEmail = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
            } else {
                fromEmail = from;
            }

            boolean isRead = !root.get("labelIds").toString().contains("UNREAD");
            boolean isPriority = root.get("labelIds").toString().contains("IMPORTANT");

            GmailMessage gmailMessage = new GmailMessage();
            gmailMessage.setThreadId(threadId);
            gmailMessage.setSnippet(snippet);
            gmailMessage.setSubject(subject);
            gmailMessage.setFromName(fromName);
            gmailMessage.setFromEmail(fromEmail);
            gmailMessage.setPriority(isPriority);
            gmailMessage.setDate(date);
            gmailMessage.setRead(isRead);
            gmailMessage.setId(messageId);
            return gmailMessage;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing metadata", e);
        }
    }

    private String extractBody(JsonNode payload) {

        // cas simple
        if (payload.has("body") && payload.get("body").has("data")) {
            return decodeBase64(payload.get("body").get("data").asText());
        }

        // cas multipart (le plus fréquent)
        if (payload.has("parts")) {
            for (JsonNode part : payload.get("parts")) {

                if ("text/plain".equals(part.get("mimeType").asText())) {
                    return decodeBase64(part.get("body").get("data").asText());
                }

                // fallback HTML
                if ("text/html".equals(part.get("mimeType").asText())) {
                    return decodeBase64(part.get("body").get("data").asText());
                }
            }
        }

        return "";
    }

    private String decodeBase64(String encoded) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        return new String(decodedBytes);
    }

    private List<AIReply> getAIReplies(String gmailId) throws ResourceNotFoundException {
        try{
            Email email = emailService.getEmailByGmailId(gmailId);
            return aiService.getAIRepliesByEmail(email);
        } catch (Exception e) {
            return List.of();
        }
    }
}
