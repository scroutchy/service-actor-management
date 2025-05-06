package com.scr.project.sam.domains.kafka.config

import com.scr.project.srm.RewardedKafkaDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaReactiveConfiguration(
    private val kafkaAvroProducerProperties: Map<String, Any>,
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, RewardedKafkaDto> = KafkaSender.create(SenderOptions.create(kafkaAvroProducerProperties))

}
