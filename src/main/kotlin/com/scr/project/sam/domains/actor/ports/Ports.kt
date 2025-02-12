package com.scr.project.sam.domains.actor.ports

import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ActorPort {

    fun create(actor: Actor): Mono<Actor>

    fun findById(id: ObjectId): Mono<Actor>

    fun update(updateRequest: ActorUpdateRequest): Mono<Actor>

    fun findAll(includeDead: Boolean, pageable: Pageable): Flux<Actor>
}