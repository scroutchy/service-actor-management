package com.scr.project.sam.domains.actor.config

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Index

@Configuration(proxyBeanMethods = false)
class ActorMongoIndexConfiguration(mongoTemplate: ReactiveMongoTemplate) {

    init {
        actorIndexes(mongoTemplate)
    }

    private fun actorIndexes(mongoTemplate: ReactiveMongoTemplate) {
        mongoTemplate.indexOps(Actor::class.java)
            .ensureIndex(
                Index().on(Actor::surname.name, ASC)
                    .on(Actor::name.name, ASC)
                    .unique()
            ).subscribe()
    }
}