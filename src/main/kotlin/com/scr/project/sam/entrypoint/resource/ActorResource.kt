package com.scr.project.sam.entrypoint.resource

import com.scr.project.sam.domains.actor.service.ActorService
import com.scr.project.sam.entrypoint.mapper.toApiDto
import com.scr.project.sam.entrypoint.mapper.toEntity
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.ApiConstants.ACTOR_PATH
import com.scr.project.sam.entrypoint.resource.ApiConstants.ID_PATH
import com.scr.project.sam.entrypoint.resource.validation.ValidationGroups.ActorRequest
import jakarta.validation.groups.Default
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ACTOR_PATH)
class ActorResource(val actorService: ActorService) {

    private val logger: Logger = LoggerFactory.getLogger(ActorResource::class.java)

    @PostMapping
    fun create(@RequestBody @Validated(Default::class, ActorRequest::class) request: ActorApiDto): Mono<ActorApiDto> {
        return actorService.create(request.toEntity())
            .map { it.toApiDto() }
            .doOnSubscribe { logger.debug("Creation request received") }
            .doOnSuccess { logger.info("Creation request successfully handled") }
    }

    @GetMapping(ID_PATH)
    fun find(@PathVariable id: ObjectId): Mono<ActorApiDto> {
        return actorService.findById(id)
            .map { it.toApiDto() }
            .doOnSubscribe { logger.debug("Find request received") }
            .doOnSuccess { logger.info("Finding actor request with id {${it.id}} successfully handled") }
    }
}