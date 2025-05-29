package com.scr.project.sam.domains.outbox.service

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import reactor.core.publisher.Mono

/** * Interface for Outbox Service
 *
 * This service is responsible for sending outbox messages. Depending on configuration, it may be implemented
 * by a service that persists messages in outbox collection or by a service that bypasses the outbox pattern
 *
 */
fun interface IOutboxService {

    fun send(outbox: Outbox): Mono<Outbox>
}