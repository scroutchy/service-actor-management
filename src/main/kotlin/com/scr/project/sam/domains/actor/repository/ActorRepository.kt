package com.scr.project.sam.domains.actor.repository

import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import reactor.core.publisher.Mono

fun interface ActorRepository {

    fun update(updateRequest: ActorUpdateRequest): Mono<Actor>
}