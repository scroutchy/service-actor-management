package com.scr.project.sam.domains.outbox.repository

import com.scr.project.sam.config.Properties.KAFKA_ENABLING_PROPERTY
import com.scr.project.sam.domains.outbox.model.entity.Outbox
import com.scr.project.sam.domains.outbox.model.entity.OutboxStatus
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.core.FindAndModifyOptions.options
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import org.springframework.data.mongodb.core.update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
@ConditionalOnProperty(name = [KAFKA_ENABLING_PROPERTY], havingValue = "true", matchIfMissing = false)
class OutboxRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) : OutboxRepository {

    override fun updateStatus(id: ObjectId, status: OutboxStatus): Mono<Outbox> {
        val update = Update().set(Outbox::status.name, status)

        return mongoTemplate.update<Outbox>()
            .matching(where(Outbox::id).isEqualTo(id))
            .apply(update)
            .withOptions(options().returnNew(true))
            .findAndModify()
    }
}