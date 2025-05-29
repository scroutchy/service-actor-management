package com.scr.project.sam.domains.kafka.config

import com.scr.project.sam.config.Properties.KAFKA_ENABLING_PROPERTY
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
@ConditionalOnProperty(name = [KAFKA_ENABLING_PROPERTY], havingValue = "true", matchIfMissing = false)
class KafkaReactiveConfiguration(
    private val kafkaAvroProducerProperties: Map<String, Any>,
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, Any> = KafkaSender.create(SenderOptions.create(kafkaAvroProducerProperties))

}
