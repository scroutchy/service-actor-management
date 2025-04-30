package com.scr.project.sam.domains.actor.messaging.v1

import com.scr.project.sam.config.TopicProperties
import com.scr.project.sam.domains.actor.mapper.toRewardedKafka
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.srm.RewardedKafkaDto
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono

@Service
@EnableConfigurationProperties(TopicProperties::class)
class RewardedMessagingV1(private val kafkaSender: KafkaSender<String, RewardedKafkaDto>, private val topicProperties: TopicProperties) {

    private val logger = LoggerFactory.getLogger(RewardedMessagingV1::class.java)

    fun notify(actor: Actor): Mono<Actor> {
        val rewarded = actor.toRewardedKafka()
        val record = ProducerRecord(topicProperties.actorCreationNotification, actor.id?.toHexString(), rewarded)
        return kafkaSender.send(SenderRecord.create(record, null).toMono())
            .doOnSubscribe {
                logger.debug(
                    "Sending message {} through Kafka to topic {}",
                    rewarded,
                    topicProperties.actorCreationNotification
                )
            }
            .doOnError { e -> logger.warn("Error when sending message to Kafka : $e") }
            .doOnComplete { logger.info("Message $rewarded successfully sent to Kafka topic ${topicProperties.actorCreationNotification}") }
            .then(actor.toMono())
    }
}