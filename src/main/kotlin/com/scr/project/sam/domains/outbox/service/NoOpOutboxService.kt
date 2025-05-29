package com.scr.project.sam.domains.outbox.service

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
@Profile("!kafka")
class NoOpOutboxService : IOutboxService {

    private val logger: Logger = LoggerFactory.getLogger(NoOpOutboxService::class.java)

    override fun send(outbox: Outbox): Mono<Outbox> {
        return outbox.toMono()
            .doOnSubscribe { logger.debug("Kafka deactived, no message sent") }
    }
}