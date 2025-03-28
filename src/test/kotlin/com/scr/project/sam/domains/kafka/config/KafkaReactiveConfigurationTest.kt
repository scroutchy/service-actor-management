package com.scr.project.sam.domains.kafka.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KafkaReactiveConfigurationTest {

    private val config = KafkaReactiveConfiguration("bootstrapServers", "schemaRegistryUrl", "PLAINTEXT", "", "username", "password")

    @Test
    fun `kafkaSender should succeed`() {
        val sender = config.kafkaSender()
        assertThat(sender).isNotNull
    }
}