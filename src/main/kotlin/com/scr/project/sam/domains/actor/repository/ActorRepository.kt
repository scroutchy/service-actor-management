package com.scr.project.sam.domains.actor.repository

import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ActorRepository {

    fun update(updateRequest: ActorUpdateRequest): Mono<Actor>

    fun findAll(includeDead: Boolean, pageable: Pageable): Flux<Actor>
}