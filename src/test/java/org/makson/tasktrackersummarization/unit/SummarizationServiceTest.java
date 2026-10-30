package org.makson.tasktrackersummarization.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.makson.tasktrackersummarization.dto.*;
import org.makson.tasktrackersummarization.exceptions.ReportGenerationException;
import org.makson.tasktrackersummarization.services.KafkaService;
import org.makson.tasktrackersummarization.services.LLMGenerationService;
import org.makson.tasktrackersummarization.services.SummarizationService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class SummarizationServiceTest {
    @Mock
    KafkaService kafkaService;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    LLMGenerationService llmGenerationService;
    @InjectMocks
    SummarizationService summarizationService;

    @Test
    @DisplayName("If the token limit is exceeded, throw an exception")
    void ifTokenLimitThenException() {
        LLMResponseDto llmResponse = getLLMResponse(FinishReason.LENGTH);

        Mockito.doReturn(llmResponse).when(llmGenerationService).generateSummary(Mockito.anyString());
        Mockito.doReturn("").when(objectMapper).writeValueAsString(Mockito.any(UserTasksDto.class));

        Assertions.assertThrows(ReportGenerationException.class, () ->
                summarizationService.handleGenerationRequest(
                        new UserTasksDto("test", LocalDate.now(), Collections.emptyList()),
                        new byte[0],
                        new byte[0]
                )
        );
        Mockito.verify(kafkaService, Mockito.never()).sendMessage(Mockito.any());
    }

    LLMResponseDto getLLMResponse(FinishReason finishReason) {
        return new LLMResponseDto(Collections.singletonList(
                new LLMResponseDto.Choices(
                        new MessageDto(MessageRole.ASSISTANT, "content"),
                        0,
                        finishReason
                )
        ));
    }
}
