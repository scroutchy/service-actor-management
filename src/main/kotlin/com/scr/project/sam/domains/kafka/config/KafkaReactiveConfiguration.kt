package com.scr.project.sam.domains.kafka.config

import com.scr.project.commons.cinema.outbox.config.Properties.SPRING_KAFKA_ENABLED
import org.apache.avro.specific.SpecificRecord
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
@ConditionalOnProperty(name = [SPRING_KAFKA_ENABLED], havingValue = "true", matchIfMissing = false)
class KafkaReactiveConfiguration(
    private val kafkaAvroProducerProperties: Map<String, Any>,
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, SpecificRecord> = KafkaSender.create(SenderOptions.create(kafkaAvroProducerProperties))

}
