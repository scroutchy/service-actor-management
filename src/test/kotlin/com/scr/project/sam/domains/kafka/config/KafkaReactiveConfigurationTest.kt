package com.scr.project.sam.domains.kafka.config

import com.scr.project.commons.cinema.kafka.config.KafkaAvroProducerConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KafkaReactiveConfigurationTest {

    private val producerConfiguration =
        KafkaAvroProducerConfiguration("bootstrapServers", "schemaRegistryUrl", "PLAINTEXT", "", "username", "password")
    private val config = KafkaReactiveConfiguration(producerConfiguration.kafkaAvroProducerProperties())

    @Test
    fun `kafkaSender should succeed`() {
        val sender = config.kafkaSender()
        assertThat(sender).isNotNull
    }
}