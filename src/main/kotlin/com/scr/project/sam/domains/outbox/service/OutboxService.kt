package com.scr.project.sam.domains.outbox.service

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import com.scr.project.sam.domains.outbox.repository.OutboxRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OutboxService(val outboxRepository: OutboxRepository) {

    private val logger: Logger = LoggerFactory.getLogger(OutboxService::class.java)

    fun send(outbox: Outbox): Mono<Outbox> {
        return outboxRepository.insert(outbox)
            .doOnSubscribe { logger.debug("Persisting outbox record: ${outbox.payload}") }
            .doOnSuccess { logger.info("Persisted outbox record: ${outbox.payload}") }
            .doOnError { logger.warn("Error persisting outbox record: ${outbox.payload}") }
    }
}