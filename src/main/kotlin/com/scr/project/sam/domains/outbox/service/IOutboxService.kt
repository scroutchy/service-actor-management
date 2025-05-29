package com.scr.project.sam.domains.outbox.service

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import reactor.core.publisher.Mono

fun interface IOutboxService {

    fun send(outbox: Outbox): Mono<Outbox>
}