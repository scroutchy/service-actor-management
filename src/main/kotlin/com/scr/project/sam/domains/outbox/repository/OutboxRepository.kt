package com.scr.project.sam.domains.outbox.repository

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import com.scr.project.sam.domains.outbox.model.entity.OutboxStatus
import org.bson.types.ObjectId
import reactor.core.publisher.Mono

fun interface OutboxRepository {

    fun updateStatus(id: ObjectId, status: OutboxStatus): Mono<Outbox>
}