package com.twentyfive.apaapilayer.configurations;

import org.springframework.kafka.core.KafkaTemplate;

public class ProducerPool {
    private final KafkaTemplate<Integer, String> template;

    public ProducerPool(KafkaTemplate<Integer, String> template) {
        this.template = template;
    }

    public void send(String toSend, int key, String topic) {
        this.template.send(topic, key, toSend);
    }
}
