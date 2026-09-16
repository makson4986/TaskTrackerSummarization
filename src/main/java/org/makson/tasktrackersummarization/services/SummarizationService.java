package org.makson.tasktrackersummarization.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.makson.tasktrackersummarization.dto.FinishReason;
import org.makson.tasktrackersummarization.dto.LLMResponseDto;
import org.makson.tasktrackersummarization.dto.ReportDto;
import org.makson.tasktrackersummarization.dto.UserTasksDto;
import org.makson.tasktrackersummarization.exceptions.ReportGenerationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummarizationService {
    private final LLMGenerationService llmGenerationService;
    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topics.summarization-requests}")
    private void handleGenerationRequest(
            UserTasksDto tasks,
            @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId,
            @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTopic
    ) {
        LLMResponseDto llmResponse = llmGenerationService.generateSummary(objectMapper.writeValueAsString(tasks));
        ReportDto report = extractText(llmResponse);

        ProducerRecord<String, ReportDto> producerRecord = new ProducerRecord<>(new String(replyTopic), report);
        producerRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId);
        kafkaService.sendMessage(producerRecord);
        log.info("The report for user '{}' was successfully generated and sent back", tasks.userEmail());
    }

    private ReportDto extractText(LLMResponseDto llmResponse) {
        LLMResponseDto.Choices response = llmResponse.choices().getFirst();
        FinishReason finishReason = response.finishReason();

        return switch (finishReason) {
            case STOP, FUNCTION_CALL -> objectMapper.readValue(response.message().content(), ReportDto.class);
            case BLACKLIST -> throw new ReportGenerationException("The request falls under thematic restrictions");
            case LENGTH -> throw new ReportGenerationException("Token limit reached in the message");
            case ERROR ->
                    throw new ReportGenerationException("The model response contains invalid arguments for the user function");
        };
    }
}
