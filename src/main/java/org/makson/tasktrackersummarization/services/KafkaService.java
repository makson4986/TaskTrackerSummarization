package org.makson.tasktrackersummarization.services;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.makson.tasktrackersummarization.dto.ReportDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {
    private final KafkaTemplate<String, ReportDto> kafkaTemplate;

    public void sendMessage(ProducerRecord<String, ReportDto> producerRecord) {
        kafkaTemplate.send(producerRecord);
    }
}
