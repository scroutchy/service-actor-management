package com.scr.project.sam.domains.actor.messaging.v1

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.service.IOutboxService
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
class RewardedMessagingV1(private val producer: IOutboxService, private val topicProperties: TopicProperties) {

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
            .doOnSubscribe { logger.debug("Handling process of actor creation notification for actor with id ${actor.id}") }
            .doOnSuccess { logger.info("Notification creation process if any was successfully handled for actor with id  ${actor.id}") }
            .doOnError { logger.warn("Failed to handle the process of actor creation notification for actor with id ${actor.id}") }
    }
}