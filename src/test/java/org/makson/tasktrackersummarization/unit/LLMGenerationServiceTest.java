package org.makson.tasktrackersummarization.unit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.makson.tasktrackersummarization.PromptLoader;
import org.makson.tasktrackersummarization.dto.*;
import org.makson.tasktrackersummarization.services.LLMAuthService;
import org.makson.tasktrackersummarization.services.LLMGenerationService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LLMGenerationServiceTest {
    private @Mock LLMAuthService authService;
    private @Mock PromptLoader promptLoader;
    private MockWebServer mockWebServer;
    private LLMGenerationService llmGenerationService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        llmGenerationService = new LLMGenerationService(
                WebClient.builder()
                        .baseUrl(mockWebServer.url("/").toString())
                        .build(),
                authService,
                promptLoader
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("You must obtain a token before sending a request")
    void shouldGetAuthToken() throws InterruptedException {
        when(promptLoader.getReportPrompt()).thenReturn("prompt");
        when(authService.getToken()).thenReturn("token");

        setResponse();
        llmGenerationService.generateSummary("request");

        String authorizationHeader = mockWebServer.takeRequest().getHeader("Authorization");
        verify(authService, times(1)).getToken();
        assertNotNull(authorizationHeader);
        assertTrue(authorizationHeader.contains("Bearer"));
    }

    @Test
    @DisplayName("The request must include a prompt and a message from the user")
    void shouldRequestIncludePromptAndUserMessage() throws InterruptedException {
        when(promptLoader.getReportPrompt()).thenReturn("prompt");
        when(authService.getToken()).thenReturn("token");

        setResponse();
        llmGenerationService.generateSummary("request");

        String stringPrompt = mockWebServer.takeRequest().getBody().readUtf8();
        LLMRequestDto prompt = new ObjectMapper().readValue(stringPrompt, LLMRequestDto.class);

        assertThat(prompt.messages())
                .extracting(MessageDto::role)
                .containsExactly(MessageRole.SYSTEM, MessageRole.USER);
    }

    LLMResponseDto getLLMResponse() {
        return new LLMResponseDto(Collections.singletonList(
                new LLMResponseDto.Choices(
                        new MessageDto(MessageRole.ASSISTANT, "content"),
                        0,
                        FinishReason.STOP
                )
        ));
    }

    void setResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody(
                        new ObjectMapper().writeValueAsString(getLLMResponse())
                )
        );
    }
}