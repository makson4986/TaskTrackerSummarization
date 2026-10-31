package org.makson.tasktrackersummarization.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.makson.tasktrackersummarization.dto.*;
import org.makson.tasktrackersummarization.services.KafkaService;
import org.makson.tasktrackersummarization.services.LLMGenerationService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class SummarizationServiceTest {
    @Container
    @ServiceConnection
    private static final KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka-native:4.1.2");
    private static final UserTasksDto USER_TASKS;
    private static final byte[] CORRELATION_ID;
    private static final byte[] REPLY_TOPIC;
    private @MockitoSpyBean KafkaService kafkaService;
    private @MockitoBean LLMGenerationService llmGenerationService;
    private final KafkaTemplate<String, UserTasksDto> jsonKafkaTemplate;
    private final ConsumerFactory<String, String> consumerFactory;

    @Autowired
    public SummarizationServiceTest(KafkaTemplate<String, UserTasksDto> jsonKafkaTemplate, ConsumerFactory<String, String> consumerFactory) {
        this.jsonKafkaTemplate = jsonKafkaTemplate;
        this.consumerFactory = consumerFactory;
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                kafkaContainer::getBootstrapServers
        );
    }

    @ParameterizedTest(name = "Should successfully retrieve tasks from the topic and send the generated report")
    @EnumSource(value = FinishReason.class, names = {"STOP", "FUNCTION_CALL"})
    void shouldSuccessfullySentReport(
            FinishReason finishReason,
            @Value("${spring.kafka.topics.summarization-requests}") String summarizationRequestsTopic,
            @Value("${spring.kafka.topics.summarization-responses}") String summarizationResponsesTopic
    ) throws ExecutionException, InterruptedException, TimeoutException {
        Mockito.when(llmGenerationService.generateSummary(Mockito.anyString())).thenReturn(getLLMResponse(finishReason));

        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(Collections.singleton(summarizationResponsesTopic));
            sendUserTasks(summarizationRequestsTopic);

            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, summarizationResponsesTopic, Duration.ofSeconds(10));

            byte[] currentCorrelationId = record.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value();
            String value = record.value();

            Assertions.assertArrayEquals(CORRELATION_ID, currentCorrelationId);
        }
    }

    @ParameterizedTest(name = "Throw an exception if the reason for finishing: {0}")
    @EnumSource(value = FinishReason.class, names = {"LENGTH", "BLACKLIST", "ERROR"})
    void shouldNotSentReport_IfFinishReasonIsError(
            FinishReason finishReason,
            @Value("${spring.kafka.topics.summarization-requests}") String summarizationRequestsTopic,
            @Value("${spring.kafka.topics.summarization-responses}") String summarizationResponsesTopic
    ) throws ExecutionException, InterruptedException, TimeoutException {
        Mockito.when(llmGenerationService.generateSummary(Mockito.anyString())).thenReturn(getLLMResponse(finishReason));

        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(Collections.singleton(summarizationResponsesTopic));
            sendUserTasks(summarizationRequestsTopic);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            Assertions.assertEquals(0, records.count());
            Mockito.verifyNoInteractions(kafkaService);
        }
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

    void sendUserTasks(String summarizationRequestsTopic) throws ExecutionException, InterruptedException, TimeoutException {
        ProducerRecord<String, UserTasksDto> producerRecord = new ProducerRecord<>(summarizationRequestsTopic, USER_TASKS);
        producerRecord.headers().add(KafkaHeaders.CORRELATION_ID, CORRELATION_ID);
        producerRecord.headers().add(KafkaHeaders.REPLY_TOPIC, REPLY_TOPIC);
        jsonKafkaTemplate.send(producerRecord).get(10, TimeUnit.SECONDS);
    }

    static {
        USER_TASKS = new UserTasksDto("test", LocalDate.now(), Collections.emptyList());
        CORRELATION_ID = UUID.randomUUID().toString().getBytes();
        REPLY_TOPIC = "SUMMARIZATION_RESPONSES".getBytes();
    }
}
