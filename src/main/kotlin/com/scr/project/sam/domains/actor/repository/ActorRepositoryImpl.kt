package com.scr.project.sam.domains.actor.repository

import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import org.springframework.data.mongodb.core.FindAndModifyOptions.options
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import org.springframework.data.mongodb.core.update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class ActorRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) : ActorRepository {

    override fun update(updateRequest: ActorUpdateRequest): Mono<Actor> {
        val update = Update().set(Actor::deathDate.name, updateRequest.deathDate)

        return mongoTemplate.update<Actor>()
            .matching(where(Actor::id).isEqualTo(updateRequest.id))
            .apply(update)
            .withOptions(options().returnNew(true))
            .findAndModify()
    }
}