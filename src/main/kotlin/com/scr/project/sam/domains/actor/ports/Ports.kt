package com.scr.project.sam.domains.actor.ports

import com.scr.project.sam.domains.actor.model.entity.Actor
import reactor.core.publisher.Mono

interface ActorPort {

    fun create(actor: Actor): Mono<Actor>
}