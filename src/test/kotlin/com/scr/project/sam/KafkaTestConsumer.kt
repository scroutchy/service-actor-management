package com.scr.project.sam

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import java.util.UUID.randomUUID

class KafkaTestConsumer<V>(
    bootstrapServers: String,
    schemaRegistryUrl: String,
    topic: String,
    groupId: String = "test-group-${randomUUID()}",
) {

    private val consumer: KafkaConsumer<String, V>

    init {
        val props = Properties().apply {
            put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(GROUP_ID_CONFIG, groupId)
            put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java)
            put(AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ENABLE_AUTO_COMMIT_CONFIG, "false")
            put("schema.registry.url", schemaRegistryUrl)
            put(SPECIFIC_AVRO_READER_CONFIG, true)
        }
        consumer = KafkaConsumer(props)
        consumer.subscribe(listOf(topic))
    }

    fun poll(timeout: Duration = Duration.ofSeconds(1)) = consumer.poll(timeout).map { it.value() }

    fun clearTopic() {
        consumer.poll(Duration.ZERO)
        consumer.seekToEnd(consumer.assignment())
    }

    fun close() = consumer.close()
}