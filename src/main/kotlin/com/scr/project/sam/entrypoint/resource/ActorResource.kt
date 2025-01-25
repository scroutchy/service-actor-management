package com.scr.project.sam.entrypoint.resource

import com.scr.project.sam.domains.actor.service.ActorService
import com.scr.project.sam.entrypoint.mapper.toApiDto
import com.scr.project.sam.entrypoint.mapper.toEntity
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.ActorResource.Companion.ACTOR_PATH
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ACTOR_PATH)
class ActorResource(val actorService: ActorService) {

    companion object {
        const val ACTOR_PATH = "api/actors"
    }

    private val logger: Logger = LoggerFactory.getLogger(ActorResource::class.java)

    @PostMapping
    fun create(request: ActorApiDto): Mono<ActorApiDto> {
        return actorService.create(request.toEntity())
            .map { it.toApiDto() }
            .doOnSubscribe { logger.debug("Creation request received") }
            .doOnSubscribe { logger.info("Creation request successfully handled") }
    }
}