package com.alterronix.alteronixback.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    private final String apiKey;

    public WebClientConfig(@Value("${open.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Bean("openAiWebClient")
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean("gmailWebClient")
    public WebClient gmailWebClient() {
        return WebClient.builder()
                .baseUrl("https://gmail.googleapis.com")
                .build();
    }

    @Bean("oAuth2WebClient")
    public WebClient oAuth2WebClient() {
        return WebClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .build();
    }
}
