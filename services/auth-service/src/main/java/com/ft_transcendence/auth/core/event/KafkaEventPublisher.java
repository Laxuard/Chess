package com.ft_transcendence.auth.core.event;

import com.ft_transcendence.common.event.UserSyncEvent;
import com.ft_transcendence.common.event.UserDeleteEvent;
import com.ft_transcendence.auth.core.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSyncEvent(UserSyncEvent event) {
        log.info("Transaction committed successfully. Publishing UserSyncEvent to Kafka: userId={}, username={}", 
                event.userId(), event.username());
        
        kafkaTemplate.send(KafkaTopicConfig.USER_SYNC_TOPIC, event.userId().toString(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeleteEvent(UserDeleteEvent event) {
        log.info("Transaction committed successfully. Publishing UserDeleteEvent to Kafka: userId={}", 
                event.userId());
        
        kafkaTemplate.send(KafkaTopicConfig.USER_DELETE_TOPIC, event.userId().toString(), event);
    }
}
