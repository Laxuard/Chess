package com.ft_transcendence.auth.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String USER_SYNC_TOPIC = "user-sync-topic";
    public static final String USER_DELETE_TOPIC = "user-delete-topic";

    @Bean
    public NewTopic userSyncTopic() {
        return TopicBuilder.name(USER_SYNC_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userDeleteTopic() {
        return TopicBuilder.name(USER_DELETE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
