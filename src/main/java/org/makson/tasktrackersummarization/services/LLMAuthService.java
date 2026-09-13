package org.makson.tasktrackersummarization.services;

import lombok.RequiredArgsConstructor;
import org.makson.tasktrackersummarization.dto.AccessTokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LLMAuthService {
    private final WebClient authWebClient;
    private volatile AccessTokenDto currentToken;
    @Value("${llm.authorization-key}")
    private String authorizationKey;

    public String getToken() {
        if (isCorrectToken()) {
            return currentToken.accessToken();
        }

        synchronized (this) {
            if (isCorrectToken()) {
                return currentToken.accessToken();
            }

            refreshToken();
            return currentToken.accessToken();
        }
    }

    private boolean isCorrectToken() {
        return currentToken != null && System.currentTimeMillis() < currentToken.expiresAt();
    }

    private void refreshToken() {
        currentToken = authWebClient
                .post()
                .header("Authorization", "Basic " + authorizationKey)
                .header("RqUID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("scope=GIGACHAT_API_PERS")
                .retrieve()
                .bodyToMono(AccessTokenDto.class)
                .block();
    }
}
