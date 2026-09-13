package org.makson.tasktrackersummarization.services;

import lombok.RequiredArgsConstructor;
import org.makson.tasktrackersummarization.PromptLoader;
import org.makson.tasktrackersummarization.dto.LLMRequestDto;
import org.makson.tasktrackersummarization.dto.LLMResponseDto;
import org.makson.tasktrackersummarization.dto.MessageDto;
import org.makson.tasktrackersummarization.dto.MessageRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LLMGenerationService {
    private final WebClient apiWebClient;
    private final LLMAuthService authService;
    private final PromptLoader promptLoader;
    @Value("${llm.model}")
    private String model;

    public LLMResponseDto generateSummary(String request) {
        String authToken = authService.getToken();
        LLMRequestDto requestBody = createRequestBody(request);

        return apiWebClient
                .post()
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(LLMResponseDto.class)
                .block();
    }

    private LLMRequestDto createRequestBody(String request) {
        String prompt = promptLoader.getReportPrompt();

        return new LLMRequestDto(
                model,
                List.of(
                        new MessageDto(MessageRole.SYSTEM, prompt),
                        new MessageDto(MessageRole.USER, request)
                )
        );
    }
}
