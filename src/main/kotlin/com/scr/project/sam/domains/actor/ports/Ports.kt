package com.scr.project.sam.domains.actor.ports

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.bson.types.ObjectId
import reactor.core.publisher.Mono

interface ActorPort {

    fun create(actor: Actor): Mono<Actor>

    fun findById(id: ObjectId): Mono<Actor>
}