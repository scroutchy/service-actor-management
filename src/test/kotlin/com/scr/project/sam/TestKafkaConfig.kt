package com.scr.project.sam

import com.scr.project.sam.config.TopicProperties
import com.scr.project.srm.RewardedKafkaDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

typealias RewardedKafkaTestConsumer = KafkaTestConsumer<RewardedKafkaDto>

@TestConfiguration
class TestKafkaConfig {

    @Bean
    fun testRewardedConsumer(
        @Value("\${spring.kafka.bootstrap-servers}") bootstrapServers: String,
        @Value("\${spring.kafka.schema.registry.url}") schemaRegistryUrl: String,
        topicProperties: TopicProperties
    ) = RewardedKafkaTestConsumer(bootstrapServers, schemaRegistryUrl, topicProperties.actorCreationNotification)
}