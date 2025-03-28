package com.scr.project.sam.domains.kafka.config

import com.scr.project.sam.RewardedKafkaDto
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaReactiveConfiguration(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.producer.properties.schema.registry.url}") private val schemaRegistryUrl: String,
    @Value("\${spring.kafka.security-protocol}") private val securityProtocol: String,
    @Value("\${spring.kafka.sasl.mechanism}") private val saslMechanism: String,
    @Value("\${spring.kafka.sasl.username}") private val saslUsername: String?,
    @Value("\${spring.kafka.sasl.password}") private val saslPassword: String?,
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, RewardedKafkaDto> {
        val producerProps: Map<String, Any> = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java.name,
            "schema.registry.url" to schemaRegistryUrl,
            "security.protocol" to securityProtocol,
            "sasl.mechanism" to saslMechanism,
            "sasl.jaas.config" to "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$saslUsername\" password=\"$saslPassword\";",
        )
        val senderOptions = SenderOptions.create<String, RewardedKafkaDto>(producerProps)

        return KafkaSender.create(senderOptions)
    }
}
