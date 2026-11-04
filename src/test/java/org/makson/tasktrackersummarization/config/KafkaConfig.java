package org.makson.tasktrackersummarization.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.makson.tasktrackersummarization.dto.UserTasksDto;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@TestConfiguration
public class KafkaConfig {
    @Bean
    public KafkaTemplate<String, UserTasksDto> jsonKafkaTemplate(ProducerFactory<String, UserTasksDto> jsonProducerFactory) {
        return new KafkaTemplate<>(jsonProducerFactory);
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate(ProducerFactory<String, String> stringProducerFactory) {
        return new KafkaTemplate<>(stringProducerFactory);
    }

    @Bean
    public ProducerFactory<String, UserTasksDto> jsonProducerFactory(KafkaProperties kafkaProperties, KafkaConnectionDetails connectionDetails) {
        return new DefaultKafkaProducerFactory<>(
                getProducerProperties(kafkaProperties, connectionDetails),
                StringSerializer::new,
                JacksonJsonSerializer::new
        );
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory(KafkaProperties kafkaProperties, KafkaConnectionDetails connectionDetails) {
        return new DefaultKafkaProducerFactory<>(
                getProducerProperties(kafkaProperties, connectionDetails),
                StringSerializer::new,
                StringSerializer::new
        );
    }

    private Map<String, Object> getProducerProperties(KafkaProperties kafkaProperties, KafkaConnectionDetails connectionDetails) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, connectionDetails.getBootstrapServers());
        return properties;
    }
}
