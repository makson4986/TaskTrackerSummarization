package org.makson.tasktrackersummarization.services;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(ProducerRecord<String, String> producerRecord) {
        kafkaTemplate.send(producerRecord);
    }
}
