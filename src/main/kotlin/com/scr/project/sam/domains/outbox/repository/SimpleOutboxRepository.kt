package com.scr.project.sam.domains.outbox.repository

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import com.scr.project.sam.domains.outbox.model.entity.OutboxStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SimpleOutboxRepository : ReactiveMongoRepository<Outbox, ObjectId> {

    fun findAllByStatus(status: OutboxStatus): Flux<Outbox>
}