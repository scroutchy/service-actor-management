package com.scr.project.sam.domains.outbox.service

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import com.scr.project.sam.domains.outbox.repository.SimpleOutboxRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@Profile("kafka")
class OutboxService(val simpleOutboxRepository: SimpleOutboxRepository) : IOutboxService {

    private val logger: Logger = LoggerFactory.getLogger(OutboxService::class.java)

    override fun send(outbox: Outbox): Mono<Outbox> {
        return simpleOutboxRepository.insert(outbox)
            .doOnSubscribe { logger.debug("Persisting outbox record: ${outbox.payload}") }
            .doOnSuccess { logger.info("Persisted outbox record: ${outbox.payload}") }
            .doOnError { logger.warn("Error persisting outbox record: ${outbox.payload}") }
    }
}