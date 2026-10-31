package org.makson.tasktrackersummarization.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.makson.tasktrackersummarization.dto.UserTasksDto;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@Configuration
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
    public ProducerFactory<String, UserTasksDto> jsonProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                StringSerializer::new,
                JacksonJsonSerializer::new
        );
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                StringSerializer::new,
                StringSerializer::new
        );
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties();
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(properties, StringDeserializer::new, StringDeserializer::new);
    }
}
