package com.scr.project.sam.domains.actor.messaging.v1

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.service.OutboxService
import com.scr.project.sam.config.TopicProperties
import com.scr.project.sam.domains.actor.mapper.toHexString
import com.scr.project.sam.domains.actor.mapper.toRewardedKafka
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.srm.RewardedKafkaDto
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@EnableConfigurationProperties(TopicProperties::class)
class RewardedMessagingV1(private val producer: OutboxService, private val topicProperties: TopicProperties) {

    private val logger = LoggerFactory.getLogger(RewardedMessagingV1::class.java)

    fun notify(actor: Actor): Mono<Actor> {
        return producer.send(
            Outbox(
                RewardedKafkaDto::class.java.name,
                actor.id.toHexString(),
                actor.toRewardedKafka().toString(),
                topicProperties.actorCreationNotification,
            )
        ).thenReturn(actor)
            .doOnSubscribe { logger.debug("Sending actor creation notification for actor ${actor.id}") }
            .doOnSuccess { logger.info("Sent actor creation notification for actor ${actor.id}") }
            .doOnError { logger.warn("Failed to send actor creation notification for actor ${actor.id}") }
    }
}